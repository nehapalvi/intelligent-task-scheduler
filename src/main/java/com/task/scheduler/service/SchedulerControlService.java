package com.task.scheduler.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.task.scheduler.dto.SchedulerStatusResponse;
import com.task.scheduler.entity.TaskEntity;
import com.task.scheduler.model.Task;
import com.task.scheduler.model.TaskStatus;
import com.task.scheduler.repository.TaskRepository;
import com.task.scheduler.scheduler.FCFSScheduler;
import com.task.scheduler.scheduler.PriorityScheduler;
import com.task.scheduler.scheduler.RoundRobinScheduler;
import com.task.scheduler.scheduler.SchedulingAlgorithm;
import com.task.scheduler.scheduler.SchedulingAlgorithmType;
import com.task.scheduler.scheduler.SJFScheduler;
import com.task.scheduler.worker.TaskWorker;
import com.task.scheduler.worker.TaskWorkerPool;

@Service
public class SchedulerControlService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerControlService.class);
    private static final long DISPATCH_INTERVAL_MILLIS = 100;

    private final TaskRepository taskRepository;
    private final TaskWorkerPool workerPool;
    private final ScheduledExecutorService dispatchExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "scheduler-dispatcher");
        thread.setDaemon(true);
        return thread;
    });
    private final Set<Long> inFlightTaskIds = ConcurrentHashMap.newKeySet();

    private volatile boolean running;
    private volatile SchedulingAlgorithmType algorithm = SchedulingAlgorithmType.FCFS;
    private volatile int workerCount = 3;
    private volatile int timeQuantum = 2;
    private ScheduledFuture<?> dispatchTask;

    public SchedulerControlService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
        this.workerPool = new TaskWorkerPool(workerCount);
    }

    public synchronized SchedulerStatusResponse start() {
        if (!running) {
            running = true;
            dispatchTask = dispatchExecutor.scheduleWithFixedDelay(
                    this::dispatchWaitingTasks,
                    0,
                    DISPATCH_INTERVAL_MILLIS,
                    TimeUnit.MILLISECONDS
            );
        }
        return getStatus();
    }

    public synchronized SchedulerStatusResponse stop() {
        running = false;
        if (dispatchTask != null) {
            dispatchTask.cancel(false);
            dispatchTask = null;
        }
        return getStatus();
    }

    public synchronized SchedulerStatusResponse getStatus() {
        return new SchedulerStatusResponse(
                running ? "RUNNING" : "STOPPED",
                algorithm,
                workerCount,
                timeQuantum
        );
    }

    public synchronized SchedulerStatusResponse changeAlgorithm(SchedulingAlgorithmType algorithm) {
        if (algorithm == null) {
            throw new IllegalArgumentException("Algorithm is required");
        }
        this.algorithm = algorithm;
        return getStatus();
    }

    public synchronized SchedulerStatusResponse configureWorkers(int workerCount) {
        if (workerCount <= 0) {
            throw new IllegalArgumentException("Worker count must be greater than zero");
        }
        workerPool.setWorkerCount(workerCount);
        this.workerCount = workerCount;
        return getStatus();
    }

    public synchronized SchedulerStatusResponse configureTimeQuantum(int timeQuantum) {
        if (timeQuantum <= 0) {
            throw new IllegalArgumentException("Time quantum must be greater than zero");
        }
        this.timeQuantum = timeQuantum;
        return getStatus();
    }

    private void dispatchWaitingTasks() {
        if (!running) {
            return;
        }

        try {
            List<TaskEntity> waitingTasks = taskRepository.findAll().stream()
                    .filter(task -> task.getStatus() == TaskStatus.WAITING && task.getId() != null)
                    .filter(task -> !inFlightTaskIds.contains(task.getId()))
                    .toList();
            if (waitingTasks.isEmpty()) {
                return;
            }

            Map<Long, TaskEntity> entitiesById = new LinkedHashMap<>();
            List<Task> tasks = new ArrayList<>();
            for (TaskEntity entity : waitingTasks) {
                entitiesById.put(entity.getId(), entity);
                tasks.add(new Task(
                        entity.getId(),
                        entity.getName(),
                        entity.getPriority(),
                        entity.getBurstTime(),
                        entity.getArrivalTime(),
                        entity.getStatus()
                ));
            }

            SchedulingAlgorithmType currentAlgorithm = algorithm;
            SchedulingAlgorithm scheduler = createAlgorithm(currentAlgorithm);
            List<Task> scheduledTasks = scheduler.schedule(tasks);
            if (currentAlgorithm == SchedulingAlgorithmType.ROUND_ROBIN) {
                dispatchRoundRobinSlices(scheduledTasks, entitiesById);
            } else {
                dispatchWholeTasks(scheduledTasks);
            }
        } catch (RuntimeException exception) {
            logger.error("Could not dispatch waiting tasks", exception);
        }
    }

    private SchedulingAlgorithm createAlgorithm(SchedulingAlgorithmType type) {
        return switch (type) {
            case FCFS -> new FCFSScheduler();
            case PRIORITY -> new PriorityScheduler();
            case SJF -> new SJFScheduler();
            case ROUND_ROBIN -> new RoundRobinScheduler(timeQuantum);
        };
    }

    private void dispatchWholeTasks(List<Task> scheduledTasks) {
        int availableWorkers = Math.max(
                0,
                workerCount - workerPool.getActiveCount() - workerPool.getQueueSize()
        );
        int submitted = 0;
        for (Task task : scheduledTasks) {
            if (submitted >= availableWorkers) {
                break;
            }
            Long taskId = task.getId();
            if (taskId == null || !inFlightTaskIds.add(taskId)) {
                continue;
            }

            submitted++;
            CompletableFuture.runAsync(
                    () -> executeSlice(taskId, task.getBurstTime(), true),
                    workerPool
            ).whenComplete((ignored, failure) -> inFlightTaskIds.remove(taskId));
        }
    }

    private void dispatchRoundRobinSlices(List<Task> scheduledSlices, Map<Long, TaskEntity> entitiesById) {
        Set<Long> selectedTaskIds = ConcurrentHashMap.newKeySet();
        for (Task slice : scheduledSlices) {
            if (slice.getId() != null && entitiesById.containsKey(slice.getId())) {
                selectedTaskIds.add(slice.getId());
            }
        }

        selectedTaskIds.removeIf(taskId -> !inFlightTaskIds.add(taskId));
        Map<Long, Integer> remainingSlices = new LinkedHashMap<>();
        for (Task slice : scheduledSlices) {
            if (selectedTaskIds.contains(slice.getId())) {
                remainingSlices.merge(slice.getId(), 1, Integer::sum);
            }
        }

        Map<Long, CompletableFuture<Void>> chains = new LinkedHashMap<>();
        for (Task slice : scheduledSlices) {
            Long taskId = slice.getId();
            if (!selectedTaskIds.contains(taskId)) {
                continue;
            }

            int slicesLeft = remainingSlices.compute(taskId, (ignored, count) -> count - 1);
            boolean finalSlice = slicesLeft == 0;
            CompletableFuture<Void> previous = chains.getOrDefault(taskId, CompletableFuture.completedFuture(null));
            chains.put(taskId, previous.thenRunAsync(
                    () -> executeSlice(taskId, slice.getBurstTime(), finalSlice),
                    workerPool
            ));
        }

        chains.forEach((taskId, chain) -> chain.whenComplete((ignored, failure) -> inFlightTaskIds.remove(taskId)));
    }

    private void executeSlice(Long taskId, int burstTime, boolean finalSlice) {
        TaskEntity entity = taskRepository.findById(taskId).orElse(null);
        if (entity == null || (entity.getStatus() != TaskStatus.WAITING && entity.getStatus() != TaskStatus.RUNNING)) {
            return;
        }

        String workerId = Thread.currentThread().getName();
        try {
            if (entity.getStatus() == TaskStatus.WAITING) {
                entity.startExecution();
            }
            entity.setWorkerId(workerId);
            taskRepository.save(entity);

            Task executionSlice = new Task(
                    entity.getId(),
                    entity.getName(),
                    entity.getPriority(),
                    burstTime,
                    entity.getArrivalTime(),
                    TaskStatus.WAITING
            );
            new TaskWorker(executionSlice).call();

            if (finalSlice) {
                TaskEntity completedTask = taskRepository.findById(taskId).orElseThrow();
                if (completedTask.getStatus() == TaskStatus.RUNNING) {
                    completedTask.complete();
                    taskRepository.save(completedTask);
                }
            }
        } catch (Exception exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            failRunningTask(taskId);
            throw new CompletionException(exception);
        }
    }

    private void failRunningTask(Long taskId) {
        taskRepository.findById(taskId).ifPresent(task -> {
            if (task.getStatus() == TaskStatus.RUNNING) {
                task.fail();
                taskRepository.save(task);
            }
        });
    }

    @PreDestroy
    public synchronized void shutdown() {
        running = false;
        if (dispatchTask != null) {
            dispatchTask.cancel(false);
        }
        dispatchExecutor.shutdownNow();
        workerPool.shutdown();
    }
}
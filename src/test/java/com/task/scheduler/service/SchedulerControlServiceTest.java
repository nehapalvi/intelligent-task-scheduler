package com.task.scheduler.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

import com.task.scheduler.dto.SchedulerStatusResponse;
import com.task.scheduler.entity.TaskEntity;
import com.task.scheduler.model.TaskStatus;
import com.task.scheduler.repository.TaskRepository;
import com.task.scheduler.scheduler.SchedulingAlgorithmType;

class SchedulerControlServiceTest {

    @Test
    void shouldStartStopAndReportSchedulerConfiguration() {
        SchedulerControlService service = new SchedulerControlService(emptyRepository());

        SchedulerStatusResponse initial = service.getStatus();
        assertEquals("STOPPED", initial.getStatus());
        assertEquals(SchedulingAlgorithmType.FCFS, initial.getAlgorithm());
        assertEquals(3, initial.getWorkerCount());
        assertEquals(2, initial.getTimeQuantum());

        service.changeAlgorithm(SchedulingAlgorithmType.PRIORITY);
        service.configureWorkers(5);
        service.configureTimeQuantum(4);

        SchedulerStatusResponse started = service.start();
        assertEquals("RUNNING", started.getStatus());
        assertEquals(SchedulingAlgorithmType.PRIORITY, started.getAlgorithm());
        assertEquals(5, started.getWorkerCount());
        assertEquals(4, started.getTimeQuantum());

        assertEquals("STOPPED", service.stop().getStatus());
        service.shutdown();
    }

    @Test
    void shouldRejectInvalidConfiguration() {
        SchedulerControlService service = new SchedulerControlService(emptyRepository());

        assertThrows(IllegalArgumentException.class, () -> service.changeAlgorithm(null));
        assertThrows(IllegalArgumentException.class, () -> service.configureWorkers(0));
        assertThrows(IllegalArgumentException.class, () -> service.configureTimeQuantum(0));

        assertEquals(3, service.getStatus().getWorkerCount());
        assertEquals(2, service.getStatus().getTimeQuantum());
        service.shutdown();
    }

    @Test
    void shouldPersistTaskLifecycleWhenSchedulerRuns() throws Exception {
        TaskRepository repository = mock(TaskRepository.class);
        TaskEntity task = new TaskEntity(
                "Persisted task",
                4,
                1,
                LocalDateTime.now().minusSeconds(1),
                TaskStatus.WAITING
        );
        task.setId(1L);
        CountDownLatch completed = new CountDownLatch(1);
        when(repository.findAll()).thenReturn(List.of(task));
        when(repository.findById(1L)).thenReturn(Optional.of(task));
        when(repository.save(any(TaskEntity.class))).thenAnswer(invocation -> {
            TaskEntity savedTask = invocation.getArgument(0);
            if (savedTask.getStatus() == TaskStatus.COMPLETED) {
                completed.countDown();
            }
            return savedTask;
        });
        SchedulerControlService service = new SchedulerControlService(repository);

        try {
            service.start();

            assertTrue(completed.await(3, TimeUnit.SECONDS));
            assertEquals(TaskStatus.COMPLETED, task.getStatus());
            assertTrue(task.getStartedAt() != null);
            assertTrue(task.getCompletedAt() != null);
            assertTrue(task.getWorkerId().startsWith("scheduler-worker-"));
        } finally {
            service.stop();
            service.shutdown();
        }
    }

    @Test
    void shouldDispatchHigherPriorityTaskFirst() throws Exception {
        TaskRepository repository = mock(TaskRepository.class);
        TaskEntity lowPriority = task("Low", 1, 1L);
        TaskEntity highPriority = task("High", 9, 2L);
        List<String> startedNames = new CopyOnWriteArrayList<>();
        CountDownLatch completed = new CountDownLatch(2);
        when(repository.findAll()).thenReturn(List.of(lowPriority, highPriority));
        when(repository.findById(1L)).thenReturn(Optional.of(lowPriority));
        when(repository.findById(2L)).thenReturn(Optional.of(highPriority));
        when(repository.save(any(TaskEntity.class))).thenAnswer(invocation -> {
            TaskEntity savedTask = invocation.getArgument(0);
            if (savedTask.getStatus() == TaskStatus.RUNNING && savedTask.getStartedAt() != null) {
                startedNames.add(savedTask.getName());
            } else if (savedTask.getStatus() == TaskStatus.COMPLETED) {
                completed.countDown();
            }
            return savedTask;
        });
        SchedulerControlService service = new SchedulerControlService(repository);

        try {
            service.configureWorkers(1);
            service.changeAlgorithm(SchedulingAlgorithmType.PRIORITY);
            service.start();

            assertTrue(completed.await(3, TimeUnit.SECONDS));
            assertEquals(List.of("High", "Low"), startedNames);
        } finally {
            service.stop();
            service.shutdown();
        }
    }

    @Test
    void shouldRotateRoundRobinSlicesAcrossTasks() throws Exception {
        TaskRepository repository = mock(TaskRepository.class);
        TaskEntity taskA = task("A", 1, 1L);
        TaskEntity taskB = task("B", 1, 2L);
        taskA.setBurstTime(2);
        taskB.setBurstTime(2);
        List<String> startedNames = new CopyOnWriteArrayList<>();
        CountDownLatch completed = new CountDownLatch(2);
        when(repository.findAll()).thenReturn(List.of(taskA, taskB));
        when(repository.findById(1L)).thenReturn(Optional.of(taskA));
        when(repository.findById(2L)).thenReturn(Optional.of(taskB));
        when(repository.save(any(TaskEntity.class))).thenAnswer(invocation -> {
            TaskEntity savedTask = invocation.getArgument(0);
            if (savedTask.getStatus() == TaskStatus.RUNNING) {
                startedNames.add(savedTask.getName());
            } else if (savedTask.getStatus() == TaskStatus.COMPLETED) {
                completed.countDown();
            }
            return savedTask;
        });
        SchedulerControlService service = new SchedulerControlService(repository);

        try {
            service.configureWorkers(1);
            service.configureTimeQuantum(1);
            service.changeAlgorithm(SchedulingAlgorithmType.ROUND_ROBIN);
            service.start();

            assertTrue(completed.await(3, TimeUnit.SECONDS));
            assertEquals(List.of("A", "B", "A", "B"), startedNames);
        } finally {
            service.stop();
            service.shutdown();
        }
    }

    private TaskEntity task(String name, int priority, Long id) {
        TaskEntity task = new TaskEntity(
                name,
                priority,
                1,
                LocalDateTime.now().minusSeconds(1),
                TaskStatus.WAITING
        );
        task.setId(id);
        return task;
    }

    private TaskRepository emptyRepository() {
        TaskRepository repository = mock(TaskRepository.class);
        when(repository.findAll()).thenReturn(List.of());
        return repository;
    }
}
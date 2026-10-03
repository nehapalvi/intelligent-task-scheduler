package com.task.scheduler.worker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.task.scheduler.model.Task;

public class TaskWorkerPool implements Executor {

    private final ThreadPoolExecutor executorService;

    public TaskWorkerPool(int workerCount) {
        if (workerCount <= 0) {
            throw new IllegalArgumentException("Worker count must be greater than zero");
        }
        AtomicInteger workerNumber = new AtomicInteger();
        ThreadFactory threadFactory = runnable -> new Thread(
            runnable,
            "scheduler-worker-" + workerNumber.incrementAndGet()
        );
        this.executorService = new ThreadPoolExecutor(
            workerCount,
            workerCount,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(),
            threadFactory
        );
    }

    public Future<Task> submit(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        return executorService.submit(new TaskWorker(task));
    }

    public List<Future<Task>> submitAll(List<Task> tasks) {
        if (tasks == null) {
            return new ArrayList<>();
        }

        List<Future<Task>> futures = new ArrayList<>();
        for (Task task : tasks) {
            if (task != null) {
                futures.add(submit(task));
            }
        }
        return futures;
    }

    @Override
    public void execute(Runnable command) {
        executorService.execute(command);
    }

    public synchronized void setWorkerCount(int workerCount) {
        if (workerCount <= 0) {
            throw new IllegalArgumentException("Worker count must be greater than zero");
        }
        if (executorService.isShutdown()) {
            throw new IllegalStateException("Worker pool is shut down");
        }

        int currentCount = executorService.getMaximumPoolSize();
        if (workerCount > currentCount) {
            executorService.setMaximumPoolSize(workerCount);
            executorService.setCorePoolSize(workerCount);
        } else {
            executorService.setCorePoolSize(workerCount);
            executorService.setMaximumPoolSize(workerCount);
        }
    }

    public int getActiveCount() {
        return executorService.getActiveCount();
    }

    public int getWorkerCount() {
        return executorService.getMaximumPoolSize();
    }

    public int getQueueSize() {
        return executorService.getQueue().size();
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public boolean isShutdown() {
        return executorService.isShutdown();
    }

    public void shutdownNow() {
        executorService.shutdownNow();
    }
}

package com.task.scheduler.worker;

import java.util.concurrent.Callable;

import com.task.scheduler.model.Task;

public class TaskWorker implements Callable<Task> {

    private final Task task;

    public TaskWorker(Task task) {
        this.task = task;
    }

    @Override
    public Task call() throws Exception {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }

        task.startExecution();
        Thread.sleep(task.getBurstTime() * 100L);
        task.complete();
        return task;
    }
}

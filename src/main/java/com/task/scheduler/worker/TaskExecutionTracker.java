package com.task.scheduler.worker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.task.scheduler.model.Task;

public class TaskExecutionTracker {

    private final Map<String, Task> runningTasks = new ConcurrentHashMap<>();

    public boolean tryStart(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }

        String key = taskKey(task);
        return runningTasks.putIfAbsent(key, task) == null;
    }

    public boolean finish(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }

        String key = taskKey(task);
        return runningTasks.remove(key, task);
    }

    public boolean isRunning(Task task) {
        if (task == null) {
            return false;
        }
        return runningTasks.containsKey(taskKey(task));
    }

    private String taskKey(Task task) {
        if (task.getId() != null) {
            return "id:" + task.getId();
        }
        return "name:" + task.getName();
    }

    public int size() {
        return runningTasks.size();
    }
}

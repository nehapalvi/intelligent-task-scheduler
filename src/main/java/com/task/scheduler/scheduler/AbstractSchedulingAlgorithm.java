package com.task.scheduler.scheduler;

import java.util.ArrayList;
import java.util.List;

import com.task.scheduler.model.Task;

public abstract class AbstractSchedulingAlgorithm implements SchedulingAlgorithm {

    protected List<Task> sanitizeTasks(List<Task> tasks) {
        if (tasks == null) {
            return new ArrayList<>();
        }

        List<Task> cleanTasks = new ArrayList<>();
        for (Task task : tasks) {
            if (task != null) {
                cleanTasks.add(task);
            }
        }
        return cleanTasks;
    }
}

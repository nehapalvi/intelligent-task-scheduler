package com.task.scheduler.scheduler;

import java.util.List;

import com.task.scheduler.model.Task;

public interface SchedulingAlgorithm {
    List<Task> schedule(List<Task> tasks);
}

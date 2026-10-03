package com.task.scheduler.scheduler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import com.task.scheduler.model.Task;

public class FCFSScheduler extends AbstractSchedulingAlgorithm {

    @Override
    public List<Task> schedule(List<Task> tasks) {
        List<Task> cleanTasks = sanitizeTasks(tasks);
        if (cleanTasks.isEmpty()) {
            return new ArrayList<>();
        }

        Queue<Task> queue = new ArrayDeque<>();
        for (Task task : cleanTasks) {
            queue.offer(task);
        }

        List<Task> orderedTasks = new ArrayList<>();
        while (!queue.isEmpty()) {
            orderedTasks.add(queue.poll());
        }

        return orderedTasks;
    }
}

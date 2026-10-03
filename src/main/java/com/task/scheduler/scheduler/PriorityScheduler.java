package com.task.scheduler.scheduler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import com.task.scheduler.model.Task;

public class PriorityScheduler extends AbstractSchedulingAlgorithm {

    @Override
    public List<Task> schedule(List<Task> tasks) {
        List<Task> cleanTasks = sanitizeTasks(tasks);
        if (cleanTasks.isEmpty()) {
            return new ArrayList<>();
        }

        PriorityQueue<Task> priorityQueue = new PriorityQueue<>(
                Comparator.comparingInt(Task::getPriority).reversed()
        );

        for (Task task : cleanTasks) {
            priorityQueue.offer(task);
        }

        List<Task> orderedTasks = new ArrayList<>();
        while (!priorityQueue.isEmpty()) {
            orderedTasks.add(priorityQueue.poll());
        }

        return orderedTasks;
    }
}

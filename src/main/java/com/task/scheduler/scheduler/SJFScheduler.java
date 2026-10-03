package com.task.scheduler.scheduler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import com.task.scheduler.model.Task;

public class SJFScheduler extends AbstractSchedulingAlgorithm {

    @Override
    public List<Task> schedule(List<Task> tasks) {
        List<Task> cleanTasks = sanitizeTasks(tasks);
        if (cleanTasks.isEmpty()) {
            return new ArrayList<>();
        }

        PriorityQueue<Task> minHeap = new PriorityQueue<>(
                Comparator.comparingInt(Task::getBurstTime)
        );

        for (Task task : cleanTasks) {
            minHeap.offer(task);
        }

        List<Task> orderedTasks = new ArrayList<>();
        while (!minHeap.isEmpty()) {
            orderedTasks.add(minHeap.poll());
        }

        return orderedTasks;
    }
}

package com.task.scheduler.scheduler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.task.scheduler.model.Task;

public class RoundRobinScheduler extends AbstractSchedulingAlgorithm {

    private record QueueEntry(Task task, int remainingBurstTime) {
    }

    private final int timeQuantum;

    public RoundRobinScheduler(int timeQuantum) {
        if (timeQuantum <= 0) {
            throw new IllegalArgumentException("Time quantum must be greater than zero");
        }
        this.timeQuantum = timeQuantum;
    }

    @Override
    public List<Task> schedule(List<Task> tasks) {
        List<Task> cleanTasks = sanitizeTasks(tasks);
        if (cleanTasks.isEmpty()) {
            return new ArrayList<>();
        }

        Deque<QueueEntry> queue = new ArrayDeque<>();
        for (Task task : cleanTasks) {
            queue.offer(new QueueEntry(task, task.getBurstTime()));
        }

        List<Task> executionOrder = new ArrayList<>();
        while (!queue.isEmpty()) {
            QueueEntry currentEntry = queue.poll();
            Task currentTask = currentEntry.task();
            int sliceTime = Math.min(timeQuantum, currentEntry.remainingBurstTime());
            int remainingBurst = currentEntry.remainingBurstTime() - sliceTime;
            executionOrder.add(new Task(
                    currentTask.getId(),
                    currentTask.getName(),
                    currentTask.getPriority(),
                    sliceTime,
                    currentTask.getArrivalTime(),
                    currentTask.getStatus()
            ));

            if (remainingBurst > 0) {
                queue.offer(new QueueEntry(currentTask, remainingBurst));
            }
        }

        return executionOrder;
    }

    public int getTimeQuantum() {
        return timeQuantum;
    }
}

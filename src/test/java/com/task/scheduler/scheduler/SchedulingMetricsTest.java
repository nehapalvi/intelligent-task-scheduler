package com.task.scheduler.scheduler;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.task.scheduler.model.Task;
import com.task.scheduler.model.TaskExecutionMetrics;

class SchedulingMetricsTest {

    @Test
    void shouldCalculateWaitingAndTurnaroundTime() {
        Task task = new Task("A", 5, 3);
        TaskExecutionMetrics metrics = new TaskExecutionMetrics(task, 0, 2, 7, 3);

        assertEquals(7 - 0, metrics.getTurnaroundTime());
        assertEquals((7 - 0) - 3, metrics.getWaitingTime());
        assertEquals(2 - 0, metrics.getResponseTime());
    }

    @Test
    void shouldCalculateAverageMetricsForMultipleTasks() {
        Task taskA = new Task("A", 5, 3);
        Task taskB = new Task("B", 2, 2);
        Task taskC = new Task("C", 7, 4);

        List<TaskExecutionMetrics> metrics = List.of(
                new TaskExecutionMetrics(taskA, 0, 0, 5, 3),
                new TaskExecutionMetrics(taskB, 1, 1, 7, 2),
                new TaskExecutionMetrics(taskC, 2, 3, 12, 4)
        );

        assertEquals(4.0, SchedulingMetrics.calculateAverageWaitingTime(metrics), 0.0001);
        assertEquals(7.0, SchedulingMetrics.calculateAverageTurnaroundTime(metrics), 0.0001);
        assertEquals(0.333333, SchedulingMetrics.calculateAverageResponseTime(metrics), 0.0001);
    }

    @Test
    void shouldCalculateThroughput() {
        Task taskA = new Task("A", 5, 3);
        Task taskB = new Task("B", 2, 2);

        List<TaskExecutionMetrics> metrics = List.of(
                new TaskExecutionMetrics(taskA, 0, 0, 3, 3),
                new TaskExecutionMetrics(taskB, 3, 3, 5, 2)
        );

        assertEquals(0.2, SchedulingMetrics.calculateThroughput(metrics, 10), 0.0001);
    }
}

package com.task.scheduler.scheduler;

import java.util.List;

import com.task.scheduler.model.TaskExecutionMetrics;

public final class SchedulingMetrics {

    private SchedulingMetrics() {
    }

    public static double calculateAverageWaitingTime(List<TaskExecutionMetrics> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return 0.0;
        }

        return metrics.stream()
                .mapToDouble(TaskExecutionMetrics::getWaitingTime)
                .average()
                .orElse(0.0);
    }

    public static double calculateAverageTurnaroundTime(List<TaskExecutionMetrics> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return 0.0;
        }

        return metrics.stream()
                .mapToDouble(TaskExecutionMetrics::getTurnaroundTime)
                .average()
                .orElse(0.0);
    }

    public static double calculateAverageResponseTime(List<TaskExecutionMetrics> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return 0.0;
        }

        return metrics.stream()
                .mapToDouble(TaskExecutionMetrics::getResponseTime)
                .average()
                .orElse(0.0);
    }

    public static double calculateThroughput(List<TaskExecutionMetrics> metrics, int totalTimeUnits) {
        if (metrics == null || metrics.isEmpty() || totalTimeUnits <= 0) {
            return 0.0;
        }

        return (double) metrics.size() / totalTimeUnits;
    }
}

package com.task.scheduler.dto;

public class StatisticsResponse {

    private final double averageWaitingTime;
    private final double averageTurnaroundTime;
    private final double averageResponseTime;
    private final double throughput;
    private final long completedTasks;
    private final long failedTasks;

    public StatisticsResponse(
            double averageWaitingTime,
            double averageTurnaroundTime,
            double averageResponseTime,
            double throughput,
            long completedTasks,
            long failedTasks) {
        this.averageWaitingTime = averageWaitingTime;
        this.averageTurnaroundTime = averageTurnaroundTime;
        this.averageResponseTime = averageResponseTime;
        this.throughput = throughput;
        this.completedTasks = completedTasks;
        this.failedTasks = failedTasks;
    }

    public double getAverageWaitingTime() {
        return averageWaitingTime;
    }

    public double getAverageTurnaroundTime() {
        return averageTurnaroundTime;
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    public double getThroughput() {
        return throughput;
    }

    public long getCompletedTasks() {
        return completedTasks;
    }

    public long getFailedTasks() {
        return failedTasks;
    }
}
package com.task.scheduler.model;

public class TaskExecutionMetrics {

    private final Task task;
    private final int arrivalTime;
    private final int startTime;
    private final int completionTime;
    private final int burstTime;

    public TaskExecutionMetrics(Task task, int arrivalTime, int startTime, int completionTime, int burstTime) {
        this.task = task;
        this.arrivalTime = arrivalTime;
        this.startTime = startTime;
        this.completionTime = completionTime;
        this.burstTime = burstTime;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getCompletionTime() {
        return completionTime;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public int getWaitingTime() {
        return getTurnaroundTime() - burstTime;
    }

    public int getTurnaroundTime() {
        return completionTime - arrivalTime;
    }

    public int getResponseTime() {
        return startTime - arrivalTime;
    }

    public Task getTask() {
        return task;
    }
}

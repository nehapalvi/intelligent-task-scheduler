package com.task.scheduler.dto;

import com.task.scheduler.scheduler.SchedulingAlgorithmType;

public class SchedulerStatusResponse {

    private final String status;
    private final SchedulingAlgorithmType algorithm;
    private final int workerCount;
    private final int timeQuantum;

    public SchedulerStatusResponse(String status, SchedulingAlgorithmType algorithm, int workerCount, int timeQuantum) {
        this.status = status;
        this.algorithm = algorithm;
        this.workerCount = workerCount;
        this.timeQuantum = timeQuantum;
    }

    public String getStatus() {
        return status;
    }

    public SchedulingAlgorithmType getAlgorithm() {
        return algorithm;
    }

    public int getWorkerCount() {
        return workerCount;
    }

    public int getTimeQuantum() {
        return timeQuantum;
    }
}
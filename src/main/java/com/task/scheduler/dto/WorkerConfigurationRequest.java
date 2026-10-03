package com.task.scheduler.dto;

import jakarta.validation.constraints.Min;

public class WorkerConfigurationRequest {

    @Min(value = 1, message = "Worker count must be greater than zero")
    private int workerCount;

    public int getWorkerCount() {
        return workerCount;
    }

    public void setWorkerCount(int workerCount) {
        this.workerCount = workerCount;
    }
}
package com.task.scheduler.dto;

import com.task.scheduler.scheduler.SchedulingAlgorithmType;

import jakarta.validation.constraints.NotNull;

public class ChangeAlgorithmRequest {

    @NotNull(message = "Algorithm is required")
    private SchedulingAlgorithmType algorithm;

    public SchedulingAlgorithmType getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(SchedulingAlgorithmType algorithm) {
        this.algorithm = algorithm;
    }
}
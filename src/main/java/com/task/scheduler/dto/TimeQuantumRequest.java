package com.task.scheduler.dto;

import jakarta.validation.constraints.Min;

public class TimeQuantumRequest {

    @Min(value = 1, message = "Time quantum must be greater than zero")
    private int timeQuantum;

    public int getTimeQuantum() {
        return timeQuantum;
    }

    public void setTimeQuantum(int timeQuantum) {
        this.timeQuantum = timeQuantum;
    }
}
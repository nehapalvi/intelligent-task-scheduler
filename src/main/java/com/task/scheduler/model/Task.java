package com.task.scheduler.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Task {

    private final Long id;
    private final String name;
    private final int priority;
    private final int burstTime;
    private final LocalDateTime createdAt;
    private LocalDateTime arrivalTime;
    private TaskStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public Task(String name, int priority, int burstTime) {
        this(null, name, priority, burstTime, LocalDateTime.now(), TaskStatus.WAITING);
    }

    public Task(Long id, String name, int priority, int burstTime, LocalDateTime arrivalTime, TaskStatus status) {
        this.id = id;
        this.name = name;
        this.priority = priority;
        this.burstTime = burstTime;
        this.arrivalTime = arrivalTime;
        this.createdAt = LocalDateTime.now();
        this.status = status;
    }

    public void startExecution() {
        this.status = status.transitionTo(TaskStatus.RUNNING);
        this.startedAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = status.transitionTo(TaskStatus.COMPLETED);
        this.completedAt = LocalDateTime.now();
    }

    public void fail() {
        this.status = status.transitionTo(TaskStatus.FAILED);
        this.completedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = status.transitionTo(TaskStatus.CANCELLED);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Task task = (Task) obj;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

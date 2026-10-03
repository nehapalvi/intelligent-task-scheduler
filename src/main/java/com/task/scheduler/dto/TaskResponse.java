package com.task.scheduler.dto;

import java.time.LocalDateTime;

import com.task.scheduler.model.TaskStatus;

public class TaskResponse {

    private Long id;
    private String name;
    private int priority;
    private int burstTime;
    private LocalDateTime arrivalTime;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String workerId;
    private TaskStatus status;

    public TaskResponse() {
    }

    public TaskResponse(Long id, String name, int priority, int burstTime, LocalDateTime arrivalTime, TaskStatus status) {
        this(id, name, priority, burstTime, arrivalTime, null, null, null, status);
    }

    public TaskResponse(
            Long id,
            String name,
            int priority,
            int burstTime,
            LocalDateTime arrivalTime,
            LocalDateTime startedAt,
            LocalDateTime completedAt,
            String workerId,
            TaskStatus status) {
        this.id = id;
        this.name = name;
        this.priority = priority;
        this.burstTime = burstTime;
        this.arrivalTime = arrivalTime;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.workerId = workerId;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public void setBurstTime(int burstTime) {
        this.burstTime = burstTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}

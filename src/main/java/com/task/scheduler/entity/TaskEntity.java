package com.task.scheduler.entity;

import java.time.LocalDateTime;

import com.task.scheduler.model.TaskStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tasks")
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int priority;

    @Column(nullable = false)
    private int burstTime;

    @Column(nullable = false)
    private LocalDateTime arrivalTime;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private String workerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    public TaskEntity() {
    }

    public TaskEntity(String name, int priority, int burstTime, LocalDateTime arrivalTime, TaskStatus status) {
        this.name = name;
        this.priority = priority;
        this.burstTime = burstTime;
        this.arrivalTime = arrivalTime;
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

    public TaskStatus getStatus() {
        return status;
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
}

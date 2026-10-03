package com.task.scheduler.model;

public enum TaskStatus {
    WAITING,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED;

    public TaskStatus transitionTo(TaskStatus nextStatus) {
        boolean allowed = switch (this) {
            case WAITING -> nextStatus == RUNNING || nextStatus == CANCELLED;
            case RUNNING -> nextStatus == COMPLETED || nextStatus == FAILED;
            case COMPLETED, FAILED, CANCELLED -> false;
        };

        if (!allowed) {
            throw new IllegalStateException("Invalid state transition: " + this + " -> " + nextStatus);
        }

        return nextStatus;
    }
}

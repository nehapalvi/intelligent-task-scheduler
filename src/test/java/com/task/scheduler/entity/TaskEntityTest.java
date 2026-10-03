package com.task.scheduler.entity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.task.scheduler.model.TaskStatus;

class TaskEntityTest {

    @Test
    void shouldSupportAllowedLifecycleTransitions() {
        TaskEntity completedTask = newWaitingTask();
        completedTask.startExecution();
        completedTask.complete();

        assertEquals(TaskStatus.COMPLETED, completedTask.getStatus());

        TaskEntity failedTask = newWaitingTask();
        failedTask.startExecution();
        failedTask.fail();

        assertEquals(TaskStatus.FAILED, failedTask.getStatus());

        TaskEntity cancelledTask = newWaitingTask();
        cancelledTask.cancel();

        assertEquals(TaskStatus.CANCELLED, cancelledTask.getStatus());
    }

    @Test
    void shouldRejectInvalidLifecycleTransitions() {
        TaskEntity waitingTask = newWaitingTask();
        assertThrows(IllegalStateException.class, waitingTask::complete);
        assertThrows(IllegalStateException.class, waitingTask::fail);

        waitingTask.cancel();
        assertThrows(IllegalStateException.class, waitingTask::startExecution);

        TaskEntity runningTask = newWaitingTask();
        runningTask.startExecution();
        assertThrows(IllegalStateException.class, runningTask::cancel);
    }

    private TaskEntity newWaitingTask() {
        return new TaskEntity("Test Task", 1, 1, LocalDateTime.now(), TaskStatus.WAITING);
    }
}
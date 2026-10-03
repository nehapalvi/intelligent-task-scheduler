package com.task.scheduler.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class TaskTest {

    @Test
    void shouldCreateTaskInWaitingState() {
        Task task = new Task("Generate Report", 8, 5);

        assertEquals("Generate Report", task.getName());
        assertEquals(8, task.getPriority());
        assertEquals(5, task.getBurstTime());
        assertEquals(TaskStatus.WAITING, task.getStatus());
    }

    @Test
    void shouldStartTaskFromWaitingState() {
        Task task = new Task("Send Email", 3, 2);

        task.startExecution();

        assertEquals(TaskStatus.RUNNING, task.getStatus());
    }

    @Test
    void shouldCancelTaskFromWaitingState() {
        Task task = new Task("Database Backup", 6, 7);

        task.cancel();

        assertEquals(TaskStatus.CANCELLED, task.getStatus());
    }

    @Test
    void shouldCompleteTaskFromRunningState() {
        Task task = new Task("Process Image", 4, 6);
        task.startExecution();

        task.complete();

        assertEquals(TaskStatus.COMPLETED, task.getStatus());
    }

    @Test
    void shouldFailTaskFromRunningState() {
        Task task = new Task("Analyze Data", 9, 10);
        task.startExecution();

        task.fail();

        assertEquals(TaskStatus.FAILED, task.getStatus());
    }

    @Test
    void shouldRejectInvalidStateTransition() {
        Task task = new Task("Generate PDF", 2, 3);
        task.startExecution();
        task.complete();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> task.startExecution());
        assertTrue(exception.getMessage().contains("Invalid state transition"));
    }
}

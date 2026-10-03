package com.task.scheduler.worker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.task.scheduler.model.Task;
import com.task.scheduler.model.TaskStatus;

class TaskWorkerTest {

    @Test
    void shouldExecuteTaskAndMarkItCompleted() throws Exception {
        Task task = new Task("Generate Report", 5, 1);

        Task result = new TaskWorker(task).call();

        assertEquals(task, result);
        assertEquals(TaskStatus.COMPLETED, result.getStatus());
    }

    @Test
    void shouldRejectNullTask() {
        TaskWorker worker = new TaskWorker(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> worker.call());
        assertEquals("Task cannot be null", exception.getMessage());
    }
}

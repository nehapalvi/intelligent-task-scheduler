package com.task.scheduler.worker;

import java.util.List;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.task.scheduler.model.Task;
import com.task.scheduler.model.TaskStatus;

class TaskWorkerPoolTest {

    @Test
    void shouldSubmitTasksAndCompleteThem() throws Exception {
        TaskWorkerPool pool = new TaskWorkerPool(2);

        Future<Task> future = pool.submit(new Task("Generate Report", 5, 1));
        Task result = future.get();

        pool.shutdown();

        assertEquals(TaskStatus.COMPLETED, result.getStatus());
    }

    @Test
    void shouldRejectInvalidWorkerCount() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new TaskWorkerPool(0));

        assertEquals("Worker count must be greater than zero", exception.getMessage());
    }

    @Test
    void shouldShutdownExecutor() {
        TaskWorkerPool pool = new TaskWorkerPool(2);

        pool.shutdown();

        assertTrue(pool.isShutdown());
    }

    @Test
    void shouldResizeWorkerPool() {
        TaskWorkerPool pool = new TaskWorkerPool(2);

        pool.setWorkerCount(4);

        assertEquals(4, pool.getWorkerCount());
        pool.shutdown();
    }

    @Test
    void shouldSubmitMultipleTasks() throws Exception {
        TaskWorkerPool pool = new TaskWorkerPool(2);

        List<Future<Task>> futures = pool.submitAll(List.of(
                new Task("A", 1, 1),
                new Task("B", 2, 1),
                new Task("C", 3, 1)
        ));

        for (Future<Task> future : futures) {
            assertEquals(TaskStatus.COMPLETED, future.get().getStatus());
        }

        pool.shutdown();
        assertFalse(pool.isShutdown() == false);
    }
}

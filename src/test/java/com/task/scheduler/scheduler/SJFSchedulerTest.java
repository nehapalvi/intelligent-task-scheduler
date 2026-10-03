package com.task.scheduler.scheduler;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.task.scheduler.model.Task;

class SJFSchedulerTest {

    @Test
    void shouldReturnEmptyListForEmptyInput() {
        SJFScheduler scheduler = new SJFScheduler();

        List<Task> result = scheduler.schedule(List.of());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldScheduleShorterBurstTimeFirst() {
        SJFScheduler scheduler = new SJFScheduler();
        Task taskA = new Task("A", 5, 10);
        Task taskB = new Task("B", 2, 3);
        Task taskC = new Task("C", 10, 5);
        Task taskD = new Task("D", 7, 2);

        List<Task> result = scheduler.schedule(List.of(taskA, taskB, taskC, taskD));

        assertEquals(List.of(taskD, taskB, taskC, taskA), result);
    }

    @Test
    void shouldHandleSameBurstTimes() {
        SJFScheduler scheduler = new SJFScheduler();
        Task taskA = new Task("A", 5, 4);
        Task taskB = new Task("B", 2, 4);
        Task taskC = new Task("C", 10, 4);

        List<Task> result = scheduler.schedule(List.of(taskA, taskB, taskC));

        assertEquals(List.of(taskA, taskB, taskC), result);
    }
}

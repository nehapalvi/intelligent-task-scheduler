package com.task.scheduler.scheduler;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.task.scheduler.model.Task;

class PrioritySchedulerTest {

    @Test
    void shouldReturnEmptyListForEmptyInput() {
        PriorityScheduler scheduler = new PriorityScheduler();

        List<Task> result = scheduler.schedule(List.of());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldScheduleHigherPriorityTasksFirst() {
        PriorityScheduler scheduler = new PriorityScheduler();
        Task taskA = new Task("A", 5, 10);
        Task taskB = new Task("B", 2, 3);
        Task taskC = new Task("C", 10, 5);
        Task taskD = new Task("D", 7, 2);

        List<Task> result = scheduler.schedule(List.of(taskA, taskB, taskC, taskD));

        assertEquals(List.of(taskC, taskD, taskA, taskB), result);
    }

    @Test
    void shouldHandleSamePriorityUsingInsertionOrder() {
        PriorityScheduler scheduler = new PriorityScheduler();
        Task taskA = new Task("A", 5, 10);
        Task taskB = new Task("B", 5, 3);
        Task taskC = new Task("C", 5, 4);

        List<Task> result = scheduler.schedule(List.of(taskA, taskB, taskC));

        assertEquals(List.of(taskA, taskB, taskC), result);
    }
}

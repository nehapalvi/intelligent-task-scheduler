package com.task.scheduler.scheduler;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.task.scheduler.model.Task;

class FCFSSchedulerTest {

    @Test
    void shouldReturnEmptyListForEmptyInput() {
        FCFSScheduler scheduler = new FCFSScheduler();

        List<Task> result = scheduler.schedule(List.of());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldScheduleTasksInArrivalOrder() {
        FCFSScheduler scheduler = new FCFSScheduler();
        Task taskA = new Task("A", 5, 10);
        Task taskB = new Task("B", 2, 3);
        Task taskC = new Task("C", 10, 5);

        List<Task> result = scheduler.schedule(List.of(taskA, taskB, taskC));

        assertEquals(List.of(taskA, taskB, taskC), result);
    }

    @Test
    void shouldIgnoreNullEntries() {
        FCFSScheduler scheduler = new FCFSScheduler();
        Task taskA = new Task("A", 4, 8);
        Task taskB = new Task("B", 1, 2);

        List<Task> input = new ArrayList<>();
        input.add(taskA);
        input.add(null);
        input.add(taskB);

        List<Task> result = scheduler.schedule(input);

        assertEquals(List.of(taskA, taskB), result);
    }
}

package com.task.scheduler.scheduler;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.task.scheduler.model.Task;

class RoundRobinSchedulerTest {

    @Test
    void shouldReturnEmptyListForEmptyInput() {
        RoundRobinScheduler scheduler = new RoundRobinScheduler(2);

        List<Task> result = scheduler.schedule(List.of());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRotateTasksBasedOnTimeQuantum() {
        RoundRobinScheduler scheduler = new RoundRobinScheduler(2);
        Task taskA = new Task("A", 5, 5);
        Task taskB = new Task("B", 2, 3);
        Task taskC = new Task("C", 10, 4);

        List<Task> result = scheduler.schedule(List.of(taskA, taskB, taskC));

        assertEquals(List.of("A", "B", "C", "A", "B", "C", "A"),
                result.stream().map(Task::getName).toList());
        assertEquals(List.of(2, 2, 2, 2, 1, 2, 1),
            result.stream().map(Task::getBurstTime).toList());
        assertEquals(5, taskA.getBurstTime());
    }
}

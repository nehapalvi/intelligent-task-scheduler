package com.task.scheduler.service;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.task.scheduler.dto.StatisticsResponse;
import com.task.scheduler.entity.TaskEntity;
import com.task.scheduler.model.TaskStatus;
import com.task.scheduler.repository.TaskRepository;

class StatisticsServiceTest {

    @Test
    void shouldCalculateStatisticsFromPersistedExecutionTimes() {
        TaskRepository repository = mock(TaskRepository.class);
        when(repository.findAll()).thenReturn(List.of(
                task(TaskStatus.COMPLETED, 10, 12, 17),
                task(TaskStatus.FAILED, 10, 11, 13)
        ));

        StatisticsResponse response = new StatisticsService(repository).getStatistics();

        assertEquals(4.9, response.getAverageWaitingTime(), 0.001);
        assertEquals(5.0, response.getAverageTurnaroundTime(), 0.001);
        assertEquals(1.5, response.getAverageResponseTime(), 0.001);
        assertEquals(1.0 / 7.0, response.getThroughput(), 0.001);
        assertEquals(1, response.getCompletedTasks());
        assertEquals(1, response.getFailedTasks());
    }

    @Test
    void shouldReturnZeroTimingMetricsWhenThereAreNoCompletedExecutions() {
        TaskRepository repository = mock(TaskRepository.class);
        when(repository.findAll()).thenReturn(List.of(
                task(TaskStatus.WAITING, 10, null, null),
                task(TaskStatus.CANCELLED, 10, null, null)
        ));

        StatisticsResponse response = new StatisticsService(repository).getStatistics();

        assertEquals(0.0, response.getAverageWaitingTime());
        assertEquals(0.0, response.getAverageTurnaroundTime());
        assertEquals(0.0, response.getAverageResponseTime());
        assertEquals(0.0, response.getThroughput());
        assertEquals(0, response.getCompletedTasks());
        assertEquals(0, response.getFailedTasks());
    }

    private TaskEntity task(TaskStatus status, int arrivalSecond, Integer startSecond, Integer completionSecond) {
        LocalDateTime baseTime = LocalDateTime.of(2026, 1, 1, 0, 0);
        TaskEntity task = new TaskEntity("Test Task", 1, 1, baseTime.plusSeconds(arrivalSecond), status);
        if (startSecond != null) {
            task.setStartedAt(baseTime.plusSeconds(startSecond));
        }
        if (completionSecond != null) {
            task.setCompletedAt(baseTime.plusSeconds(completionSecond));
        }
        return task;
    }
}
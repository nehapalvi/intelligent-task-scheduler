package com.task.scheduler.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.ToDoubleFunction;

import org.springframework.stereotype.Service;

import com.task.scheduler.dto.StatisticsResponse;
import com.task.scheduler.entity.TaskEntity;
import com.task.scheduler.model.TaskStatus;
import com.task.scheduler.repository.TaskRepository;

@Service
public class StatisticsService {

    private final TaskRepository taskRepository;

    public StatisticsService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public StatisticsResponse getStatistics() {
        List<TaskEntity> tasks = taskRepository.findAll();
        List<TaskEntity> timedTasks = tasks.stream()
                .filter(this::hasValidExecutionTimes)
                .toList();

        double averageWaitingTime = average(timedTasks,
                this::waitingTimeSeconds);
        double averageTurnaroundTime = average(timedTasks,
                task -> secondsBetween(task.getArrivalTime(), task.getCompletedAt()));
        double averageResponseTime = average(timedTasks,
                task -> secondsBetween(task.getArrivalTime(), task.getStartedAt()));

        double throughput = calculateThroughput(timedTasks);
        long completedTasks = countByStatus(tasks, TaskStatus.COMPLETED);
        long failedTasks = countByStatus(tasks, TaskStatus.FAILED);

        return new StatisticsResponse(
                averageWaitingTime,
                averageTurnaroundTime,
                averageResponseTime,
                throughput,
                completedTasks,
                failedTasks
        );
    }

    private boolean hasValidExecutionTimes(TaskEntity task) {
        if ((task.getStatus() != TaskStatus.COMPLETED && task.getStatus() != TaskStatus.FAILED)
                || task.getArrivalTime() == null
                || task.getStartedAt() == null
                || task.getCompletedAt() == null) {
            return false;
        }

        return !task.getArrivalTime().isAfter(task.getStartedAt())
                && !task.getStartedAt().isAfter(task.getCompletedAt());
    }

    private double average(List<TaskEntity> tasks, ToDoubleFunction<TaskEntity> metric) {
        return tasks.stream()
                .mapToDouble(metric)
                .average()
                .orElse(0.0);
    }

        private double waitingTimeSeconds(TaskEntity task) {
                double turnaroundTime = secondsBetween(task.getArrivalTime(), task.getCompletedAt());
                double burstDuration = task.getBurstTime() / 10.0;
                return Math.max(0.0, turnaroundTime - burstDuration);
        }

    private double calculateThroughput(List<TaskEntity> tasks) {
        if (tasks.isEmpty()) {
            return 0.0;
        }

        LocalDateTime firstArrival = tasks.stream()
                .map(TaskEntity::getArrivalTime)
                .min(LocalDateTime::compareTo)
                .orElseThrow();
        LocalDateTime lastCompletion = tasks.stream()
                .map(TaskEntity::getCompletedAt)
                .max(LocalDateTime::compareTo)
                .orElseThrow();
        double elapsedSeconds = secondsBetween(firstArrival, lastCompletion);

        if (elapsedSeconds <= 0.0) {
            return 0.0;
        }

        long completedExecutions = tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                .count();
        return completedExecutions / elapsedSeconds;
    }

    private long countByStatus(List<TaskEntity> tasks, TaskStatus status) {
        return tasks.stream()
                .filter(task -> task.getStatus() == status)
                .count();
    }

    private double secondsBetween(LocalDateTime start, LocalDateTime end) {
        return Duration.between(start, end).toMillis() / 1000.0;
    }
}
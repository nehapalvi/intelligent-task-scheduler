package com.task.scheduler.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.task.scheduler.dto.CreateTaskRequest;
import com.task.scheduler.dto.TaskResponse;
import com.task.scheduler.entity.TaskEntity;
import com.task.scheduler.exception.TaskNotFoundException;
import com.task.scheduler.model.TaskStatus;
import com.task.scheduler.repository.TaskRepository;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public TaskResponse createTask(CreateTaskRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        TaskEntity taskEntity = new TaskEntity(
                request.getName(),
                request.getPriority(),
                request.getBurstTime(),
                LocalDateTime.now(),
                TaskStatus.WAITING
        );

        TaskEntity savedTask = taskRepository.save(taskEntity);
        return toResponse(savedTask);
    }

    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public TaskResponse getTaskById(Long taskId) {
        TaskEntity taskEntity = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        return toResponse(taskEntity);
    }

    public TaskResponse cancelTask(Long taskId) {
        TaskEntity taskEntity = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        taskEntity.cancel();
        TaskEntity savedTask = taskRepository.save(taskEntity);
        return toResponse(savedTask);
    }

    public void deleteTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new TaskNotFoundException(taskId);
        }
        taskRepository.deleteById(taskId);
    }

    private TaskResponse toResponse(TaskEntity taskEntity) {
        return new TaskResponse(
                taskEntity.getId(),
                taskEntity.getName(),
                taskEntity.getPriority(),
                taskEntity.getBurstTime(),
                taskEntity.getArrivalTime(),
                taskEntity.getStartedAt(),
                taskEntity.getCompletedAt(),
                taskEntity.getWorkerId(),
                taskEntity.getStatus()
        );
    }
}

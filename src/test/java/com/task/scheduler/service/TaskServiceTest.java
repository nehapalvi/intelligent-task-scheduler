package com.task.scheduler.service;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.task.scheduler.dto.CreateTaskRequest;
import com.task.scheduler.dto.TaskResponse;
import com.task.scheduler.entity.TaskEntity;
import com.task.scheduler.exception.TaskNotFoundException;
import com.task.scheduler.model.TaskStatus;
import com.task.scheduler.repository.TaskRepository;

class TaskServiceTest {

    @Test
    void shouldCreateTask() {
        TaskRepository repository = mock(TaskRepository.class);
        TaskEntity entity = new TaskEntity("Generate Report", 8, 5, LocalDateTime.now(), TaskStatus.WAITING);
        entity.setId(1L);

        when(repository.save(any(TaskEntity.class))).thenReturn(entity);
        TaskService service = new TaskService(repository);

        TaskResponse response = service.createTask(new CreateTaskRequest("Generate Report", 8, 5));

        assertEquals("Generate Report", response.getName());
        assertEquals(8, response.getPriority());
        assertEquals(5, response.getBurstTime());
        assertEquals(TaskStatus.WAITING, response.getStatus());
        verify(repository).save(any(TaskEntity.class));
    }

    @Test
    void shouldCancelWaitingTask() {
        TaskRepository repository = mock(TaskRepository.class);
        TaskEntity entity = new TaskEntity("Send Email", 3, 2, LocalDateTime.now(), TaskStatus.WAITING);
        entity.setId(10L);

        when(repository.findById(10L)).thenReturn(Optional.of(entity));
        when(repository.save(any(TaskEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskService service = new TaskService(repository);
        TaskResponse cancelled = service.cancelTask(10L);

        assertEquals(TaskStatus.CANCELLED, cancelled.getStatus());
        verify(repository).save(any(TaskEntity.class));
    }

    @Test
    void shouldRejectCancellationWhenTaskIsRunning() {
        TaskRepository repository = mock(TaskRepository.class);
        TaskEntity entity = new TaskEntity("Send Email", 3, 2, LocalDateTime.now(), TaskStatus.WAITING);
        entity.startExecution();

        when(repository.findById(10L)).thenReturn(Optional.of(entity));

        TaskService service = new TaskService(repository);

        assertThrows(IllegalStateException.class, () -> service.cancelTask(10L));
        verify(repository, org.mockito.Mockito.never()).save(any(TaskEntity.class));
    }

    @Test
    void shouldThrowWhenTaskDoesNotExist() {
        TaskRepository repository = mock(TaskRepository.class);
        when(repository.findById(999L)).thenReturn(Optional.empty());

        TaskService service = new TaskService(repository);

        assertThrows(TaskNotFoundException.class, () -> service.getTaskById(999L));
    }
}

package com.task.scheduler.worker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.task.scheduler.model.Task;

class TaskExecutionTrackerTest {

    @Test
    void shouldTrackRunningTasksWithoutDuplicates() {
        TaskExecutionTracker tracker = new TaskExecutionTracker();
        Task task = new Task("Process Image", 3, 2);

        assertTrue(tracker.tryStart(task));
        assertFalse(tracker.tryStart(task));
        assertTrue(tracker.isRunning(task));
        assertEquals(1, tracker.size());
    }

    @Test
    void shouldFinishTrackedTask() {
        TaskExecutionTracker tracker = new TaskExecutionTracker();
        Task task = new Task("Send Email", 4, 1);

        tracker.tryStart(task);
        assertTrue(tracker.finish(task));
        assertFalse(tracker.isRunning(task));
        assertEquals(0, tracker.size());
    }
}

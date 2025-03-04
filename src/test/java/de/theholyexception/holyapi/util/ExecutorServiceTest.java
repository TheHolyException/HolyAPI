package de.theholyexception.holyapi.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

public class ExecutorServiceTest {

    private ExecutorHandler executorHandler;
    private ExecutorTask executorTask;

    @BeforeEach
    public void setUp() {
        executorHandler = new ExecutorHandler(Executors.newFixedThreadPool(1));
        executorTask = new ExecutorTask(() -> {
            try { Thread.sleep(100); } catch ( Exception ex ) { ex.printStackTrace(); }
            System.out.println("Task executed");
        });
    }

    @Test
    public void testSubmitTask() {
        // Submit the task
        executorHandler.putTask(executorTask, 1);
        executorHandler.awaitGroup(1);

        // Verify that the task was executed
        assertTrue(executorTask.isCompleted());
    }

    @Test
    public void testCancelTask() {
        // Submit the task
        executorHandler.putTask(executorTask);

        // Cancel the task
        executorHandler.abortTask(executorTask);

        // Verify that the task was cancelled
        assertFalse(executorTask.isCompleted());
    }
}
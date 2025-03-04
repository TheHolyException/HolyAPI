package de.theholyexception.holyapi.util;

import de.theholyexception.holyapi.util.logger.LogLevel;
import de.theholyexception.holyapi.util.logger.LoggerProxy;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class ExecutorHandler {

    @Getter
    private int lastTaskIdentifier = 0;
    @Getter
    private final List<ExecutorTask> taskList;
    @Getter
    private ExecutorService executorService;

    public ExecutorHandler(ExecutorService executorService) {
        this.executorService = executorService;
        this.taskList = new ArrayList<>();
    }

    public int getNewTaskIdentifier() {
        return ++lastTaskIdentifier;
    }

    public void putTask(ExecutorTask task, int groupID) {
        if (task == null) throw new IllegalArgumentException("Task cannot be null");
        task.setGroupId(groupID);
        task.setTaskId(getNewTaskIdentifier());
        taskList.add(task);
        executorService.execute(task);
    }

    public void putTask(ExecutorTask task) {
        if (task == null) throw new IllegalArgumentException("Task cannot be null");
        putTask(task, -1);
    }

    public boolean abortTask(ExecutorTask task) {
        if (task.isCompleted() || task.isRunning()) return false;
        task.setAborted(true);
        return true;
    }

    /**
     * Attempts to abort a task with the specified task ID.
     *
     * @param taskId the ID of the task to be aborted
     * @return true if the task was successfully aborted, false otherwise
     */
    public boolean abortTask(int taskId) {
        // Iterate over the list of tasks
        for (ExecutorTask task : taskList) {
            // Check if the current task has the specified task ID
            if (task.getTaskId() == taskId) {
                // Attempt to abort the task and return the result
                return abortTask(task);
            }
        }
        // Return false if no task with the specified ID is found
        return false;
    }

    public boolean hasGroupRunningThreads(int groupID) {
        return taskList.stream()
                .anyMatch(t -> (t.getGroupId() == groupID) && !t.isCompleted());
    }

    public boolean areThreadsRunning() {
        return taskList.stream().anyMatch(t -> !t.isCompleted());
    }

    public void awaitGroup(int groupID, long checkInterval) {
        if (checkInterval <= 0) throw new IllegalArgumentException("Check interval must be greater than 0");
        while (hasGroupRunningThreads(groupID)) {
            try {
                Thread.sleep(checkInterval);
            } catch (InterruptedException ex) {
                LoggerProxy.log(LogLevel.ERROR, "InterruptedException", ex);
            }
        }
    }

    public void awaitGroup(int groupID) {
        awaitGroup(groupID, 100);
    }

    public void closeAfterExecution() {
        while (taskList.stream().anyMatch(t -> !t.isCompleted())) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                LoggerProxy.log(LogLevel.ERROR, "InterruptedException", ex);
            }
        }
        executorService.shutdown();
    }

    public void closeForce() {
        if (executorService.isShutdown()) throw new IllegalStateException("ExecutorService is already shutdown");
        executorService.shutdownNow();
    }

    public void updateExecutorService(ExecutorService executorService) {
        if (areThreadsRunning()) throw new IllegalStateException("There are still threads running, close them with closeForce()");
        this.executorService = executorService;
    }
}

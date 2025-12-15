package de.theholyexception.holyapi.util;

import de.theholyexception.holyapi.util.logger.LogLevel;
import de.theholyexception.holyapi.util.logger.LoggerProxy;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class ExecutorHandler {

    @Getter
    private int lastTaskIdentifier = 0;
    @Getter
    private final List<ExecutorTask> taskList;
    @Getter
    private ExecutorService executorService;
    private Function<Integer, String> threadNameFactory;
    @Getter
    private List<Thread> threadList = new ArrayList<>();
    public static final int DEFAULT_GROUP = -1;

    public ExecutorHandler(ExecutorService executorService) {
        this.executorService = executorService;
        this.taskList = new ArrayList<>();
        applyThreadNameFactory();
    }

    public int getNewTaskIdentifier() {
        return ++lastTaskIdentifier;
    }

    public ExecutorTask putTask(ExecutorTask task, int groupID) {
        if (task == null) throw new IllegalArgumentException("Task cannot be null");
        task.setGroupId(groupID);
        task.setTaskId(getNewTaskIdentifier());
        synchronized (taskList) {
            taskList.add(task);
        }
        executorService.execute(task);
        return task;
    }

    public ExecutorTask putTask(Runnable runnable, int groupID) {
        if (runnable == null) throw new IllegalArgumentException("Runnable cannot be null");
        return putTask(new ExecutorTask(runnable, groupID));
    }

    public ExecutorTask putTask(ExecutorTask task) {
        if (task == null) throw new IllegalArgumentException("Task cannot be null");
        return putTask(task, DEFAULT_GROUP);
    }

    public ExecutorTask putTask(Runnable runnable) {
        if (runnable == null) throw new IllegalArgumentException("Runnable cannot be null");
        return putTask(new ExecutorTask(runnable));
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
        synchronized (taskList) {
            // Iterate over the list of tasks
            for (ExecutorTask task : taskList) {
                // Check if the current task has the specified task ID
                if (task.getTaskId() == taskId) {
                    // Attempt to abort the task and return the result
                    return abortTask(task);
                }
            }
        }
        // Return false if no task with the specified ID is found
        return false;
    }

    public boolean hasGroupRunningThreads(int groupID) {
        synchronized (taskList) {
            for (ExecutorTask task : taskList) {
                if (task.getGroupId() == groupID && !task.isCompleted())
                    return true;
            }
            return false;
        }
    }

    public boolean areThreadsRunning() {
        synchronized (taskList) {
            for (ExecutorTask task : taskList) {
                if (!task.isCompleted())
                    return true;
            }
            return false;
        }
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
        synchronized (taskList) {
            while (areThreadsRunning()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    LoggerProxy.log(LogLevel.ERROR, "InterruptedException", ex);
                }
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
        applyThreadNameFactory();
    }

    public void setThreadNameFactory(Function<Integer, String> function) {
        this.threadNameFactory = function;
        applyThreadNameFactory();
    }

    private void applyThreadNameFactory() {
        if (!(executorService instanceof ThreadPoolExecutor)) {
            LoggerProxy.log(LogLevel.WARN, "ExecutorService is not a ThreadPoolExecutor, can't apply thread name factory");
            return;
        }
        if (areThreadsRunning()) {
            LoggerProxy.log(LogLevel.ERROR, "Can't apply thread name factory, there are still threads running");
            return;
        }
        AtomicInteger counter = new AtomicInteger(0);
        threadList.clear();
        ((ThreadPoolExecutor)executorService).setThreadFactory(t -> {
            Thread thread;
            if (threadNameFactory != null)
                thread = new Thread(t, threadNameFactory.apply(counter.getAndIncrement()));
            else
                thread = new Thread(t);
            threadList.add(thread);
            return thread;
        });
    }

}

package de.theholyexception.holyapi.util;

import de.theholyexception.holyapi.util.logger.LogLevel;
import de.theholyexception.holyapi.util.logger.LoggerProxy;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

public class ExecutorTask implements Runnable {

    private final Runnable command;
    private Consumer<Exception> onError;
    private Runnable onComplete;

    @Getter
    private int taskId;
    @Getter @Setter
    private int groupId;
    @Getter
    private boolean completed;
    @Setter(AccessLevel.PROTECTED)
    private boolean aborted;
    @Getter
    private long startTime;
    @Getter
    private long runtime;

    public ExecutorTask(Runnable command) {
        this.command = command;
        this.groupId = -1;
        this.taskId = -1;
        this.completed = false;
        this.startTime = -1;
    }

    public ExecutorTask(Runnable command, int groupId) {
        this.command = command;
        this.groupId = groupId;
        this.taskId = -1;
        this.completed = false;
        this.startTime = -1;
    }

    public void setTaskId(int taskId) {
        if (taskId <= 0) throw new IllegalArgumentException("Task ID must be greater than 0");
        if (this.taskId == -1)
            this.taskId = taskId;
        else
            LoggerProxy.log(LogLevel.ERROR, "Cannot set task id, this task already has an task id ("+this.taskId+")");
    }

    public boolean isRunning() {
        return startTime != -1L && !completed;
    }

    public ExecutorTask onError(Consumer<Exception> onError) {
        this.onError = onError;
        return this;
    }

    public ExecutorTask onComplete(Runnable onComplete) {
        this.onComplete = onComplete;
        return this;
    }

    @Override
    public void run() {
        if (aborted) return;
        startTime = System.currentTimeMillis();
        try {
            command.run();
            runtime = System.currentTimeMillis()- startTime;
            if (onComplete != null)
                onComplete.run();
            completed = true;
        } catch (Exception ex) {
            if (onError != null)
                onError.accept(ex);
            else
                LoggerProxy.log(LogLevel.ERROR, "Failed to run task ", ex);
        }
    }

    @Override
    public String toString() {
        return String.format("ExecutorTask: {TaskID: %d, GroupID: %d, Completed: %b, Runtime: %dms}", taskId, groupId, completed, getRuntime());
    }
}

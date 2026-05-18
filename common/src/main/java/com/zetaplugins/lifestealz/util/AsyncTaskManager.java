package com.zetaplugins.lifestealz.util;

import com.zetaplugins.lifestealz.util.tasks.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Manages all running async tasks
 */
public final class AsyncTaskManager {
    private final List<CompletableFuture<Void>> runningTasks = new ArrayList<>();

    /**
     * Add a task to the list of running tasks
     * @param task The task to add
     */
    public void addTask(BukkitTask task) {
        runningTasks.add(task);
    }

    /**
     * Cancel all running tasks
     */
    public void cancelAllTasks() {
        for (BukkitTask task : runningTasks) {
            if (task.isCancelled()) continue;
            task.cancel();
        }
        runningTasks.clear();
    }
}

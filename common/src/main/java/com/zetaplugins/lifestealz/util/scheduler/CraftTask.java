package com.zetaplugins.lifestealz.util.scheduler;

import com.zetaplugins.lifestealz.LifeStealZ;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

public class CraftTask implements BukkitTask, Runnable { // Spigot
    @Getter @Setter private volatile CraftTask next = null;

    public static final int ERROR = 0;
    public static final int NO_REPEATING = -1;
    public static final int CANCEL = -2;
    public static final int PROCESS_FOR_FUTURE = -3;
    public static final int DONE_FOR_FUTURE = -4;
    /**
     * -1 means no repeating <br>
     * -2 means cancel <br>
     * -3 means processing for Future <br>
     * -4 means done for Future <br>
     * Never 0 <br>
     * >0 means number of ticks to wait between each execution
     */

    @Getter @Setter private volatile long period;
    @Getter @Setter private long nextRun;
    public final Consumer<BukkitTask> task;
    private final int id;
    @Getter private final long createdAt = System.nanoTime();

    CraftTask() {
        this(null, CraftTask.NO_REPEATING, CraftTask.NO_REPEATING);
    }

    CraftTask(final Consumer<BukkitTask> task) {
        this(task, CraftTask.NO_REPEATING, CraftTask.NO_REPEATING);
    }

    CraftTask(final Consumer<BukkitTask> task, final int id, final long period) {
        this.task = task;
        this.id = id;
        this.period = period;
    }

    @Override
    public final int getTaskId() {
        return this.id;
    }

    @Override
    public boolean isSync() {
        return true;
    }

    @Override
    public void run() {
        this.task.accept(this);
    }

    Class<?> getTaskClass() {
        return (this.task != null) ? this.task.getClass() : null;
    }

    @Override
    public boolean isCancelled() {
        return (this.period == CraftTask.CANCEL);
    }

    @Override
    public void cancel() {
        LifeStealZ.getScheduler().cancelTask(this.id);
    }

    /**
     * This method properly sets the status to cancelled, synchronizing when required.
     */
    void cancel0() {
        this.setPeriod(CraftTask.CANCEL);
    }
}
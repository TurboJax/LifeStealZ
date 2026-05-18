package com.zetaplugins.lifestealz.util.scheduler;

import com.zetaplugins.lifestealz.LifeStealZ;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;

/**
 * The fundamental concepts for this implementation:
 * <ul>
 * <li>Main thread owns {@link #head} and {@link #currentTick}, but it may be read from any thread</li>
 * <li>Main thread exclusively controls {@link #temp} and {@link #pending}.
 *     They are never to be accessed outside of the main thread; alternatives exist to prevent locking.</li>
 * <li>{@link #head} to {@link #tail} act as a linked list/queue, with 1 consumer and infinite producers.
 *     Adding to the tail is atomic and very efficient; utility method is {@link #handle(CraftTask, long)} or {@link #addTask(CraftTask)}. </li>
 * <li>Changing the period on a task is delicate.
 *     Any future task needs to notify waiting threads.
 *     Async tasks must be synchronized to make sure that any thread that's finishing will remove itself from {@link #runners}.
 *     Another utility method is provided for this, {@link #cancelTask(int)}</li>
 * <li>{@link #runners} provides a moderately up-to-date view of active tasks.
 *     If the linked head to tail set is read, all remaining tasks that were active at the time execution started will be located in runners.</li>
 * <li>Async tasks are responsible for removing themselves from runners</li>
 * <li>Sync tasks are only to be removed from runners on the main thread when coupled with a removal from pending and temp.</li>
 * <li>Most of the design in this scheduler relies on queuing special tasks to perform any data changes on the main thread.
 *     When executed from inside a synchronous method, the scheduler will be updated before next execution by virtue of the frequent {@link #parsePending()} calls.</li>
 * </ul>
 */
public class CraftScheduler implements BukkitScheduler {
    /**
     * The start ID for the counter.
     */
    private static final int START_ID = 1;
    /**
     * Increment the {@link #ids} field and reset it to the {@link #START_ID} if it reaches {@link Integer#MAX_VALUE}
     */
    private static final IntUnaryOperator INCREMENT_IDS = previous -> {
        // We reached the end, go back to the start!
        if (previous == Integer.MAX_VALUE) {
            return CraftScheduler.START_ID;
        }
        return previous + 1;
    };
    /**
     * Counter for IDs. Order doesn't matter, only uniqueness.
     */
    private final AtomicInteger ids = new AtomicInteger(CraftScheduler.START_ID);
    /**
     * Current head of linked-list. This reference is always stale, {@link CraftTask#getNext} is the live reference.
     */
    private volatile CraftTask head = new CraftTask();
    /**
     * Tail of a linked-list. AtomicReference only matters when adding to queue
     */
    private final AtomicReference<CraftTask> tail = new AtomicReference<CraftTask>(this.head);
    /**
     * Main thread logic only
     */
    final PriorityQueue<CraftTask> pending = new PriorityQueue<CraftTask>(10, // Paper
            new Comparator<CraftTask>() {
                @Override
                public int compare(final CraftTask o1, final CraftTask o2) {
                    int value = Long.compare(o1.getNextRun(), o2.getNextRun());

                    // If the tasks should run on the same tick they should be run FIFO
                    return value != 0 ? value : Long.compare(o1.getCreatedAt(), o2.getCreatedAt());
                }
            });
    /**
     * Main thread logic only
     */
    private final List<CraftTask> temp = new ArrayList<CraftTask>();
    /**
     * These are tasks that are currently active. It's provided for 'viewing' the current state.
     */
    final ConcurrentHashMap<Integer, CraftTask> runners = new ConcurrentHashMap<Integer, CraftTask>(); // Paper
    /**
     * The sync task that is currently running on the main thread.
     */
    private volatile CraftTask currentTask = null;
    // Paper start - Improved Async Task Scheduler
    volatile int currentTick = -1;/*
    private final Executor executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("Craft Scheduler Thread - %d").build());
    private CraftAsyncDebugger debugHead = new CraftAsyncDebugger(-1, null, null) {
        @Override
        StringBuilder debugTo(StringBuilder string) {
            return string;
        }
    };
    private CraftAsyncDebugger debugTail = this.debugHead;

    */ // Paper end
    private static final int RECENT_TICKS;

    static {
        RECENT_TICKS = 30;
    }

    // Paper start
    private final CraftScheduler asyncScheduler;
    private final boolean isAsyncScheduler;
    public CraftScheduler() {
        this(false);
    }

    public CraftScheduler(boolean isAsync) {
        this.isAsyncScheduler = isAsync;
        if (isAsync) {
            this.asyncScheduler = this;
        } else {
            this.asyncScheduler = new CraftAsyncScheduler();
        }
    }
    // Paper end

    @Override
    public int scheduleSyncTask(final @NonNull Runnable task) {
        return this.scheduleSyncTask(task, 0L);
    }

    @NonNull
    @Override
    public BukkitTask runTask(@NonNull Consumer<BukkitTask> runnable) {
        return this.runTaskLater(runnable, 0L);
    }

    @NonNull
    @Override
    public BukkitTask runTaskAsync(@NonNull Consumer<BukkitTask> runnable) {
        return this.runTaskLaterAsync(runnable, 0L);
    }

    @Override
    public int scheduleSyncTask(final @NonNull Runnable task, final long delay) {
        return this.scheduleSyncRepeatingTask(task, delay, CraftTask.NO_REPEATING);
    }

    @NonNull
    @Override
    public BukkitTask runTaskLater(@NonNull Consumer<BukkitTask> runnable, long delay) {
        return this.runTaskTimer(runnable, delay, CraftTask.NO_REPEATING);
    }

    @NonNull
    @Override
    public BukkitTask runTaskLaterAsync(@NonNull Consumer<BukkitTask> runnable, long delay) {
        return this.runTaskTimerAsync(runnable, delay, CraftTask.NO_REPEATING);
    }

    @Override
    public int scheduleSyncRepeatingTask(final @NonNull Runnable runnable, long delay, long period) {
        return this.runTaskTimer0(_ -> runnable.run(), delay, period).getTaskId();
    }

    @NonNull
    @Override
    public BukkitTask runTaskTimer(@NonNull Consumer<BukkitTask> runnable, long delay, long period) {
        return this.runTaskTimer0(runnable, delay, period);
    }

    public BukkitTask runTaskTimer0(Consumer<BukkitTask> task, long delay, long period) {
        CraftScheduler.validate(task);
        if (delay < 0L) {
            delay = 0;
        }
        if (period == CraftTask.ERROR) {
            period = 1L;
        } else if (period < CraftTask.NO_REPEATING) {
            period = CraftTask.NO_REPEATING;
        }
        return this.handle(new CraftTask(task, this.nextId(), period), delay);
    }

    @NonNull
    @Override
    public BukkitTask runTaskTimerAsync(@NonNull Consumer<BukkitTask> task, long delay, long period) {
        return this.runTaskTimerAsynchronously(task, delay, period);
    }

    public BukkitTask runTaskTimerAsynchronously(Consumer<BukkitTask> task, long delay, long period) {
        CraftScheduler.validate(task);
        if (delay < 0L) {
            delay = 0;
        }
        if (period == CraftTask.ERROR) {
            period = 1L;
        } else if (period < CraftTask.NO_REPEATING) {
            period = CraftTask.NO_REPEATING;
        }
        return this.handle(new CraftAsyncTask(this.asyncScheduler.runners, task, this.nextId(), period), delay); // Paper
    }

    @Override
    public <T> Future<T> callSyncMethod(final @NonNull Callable<T> task) {
        CraftScheduler.validate(task);
        final CraftFuture<T> future = new CraftFuture<T>(task, this.nextId());
        this.handle(future, 0L);
        return future;
    }

    @Override
    public void cancelTask(final int taskId) {
        if (taskId <= 0) {
            return;
        }
        // Paper start
        if (!this.isAsyncScheduler) {
            this.asyncScheduler.cancelTask(taskId);
        }
        // Paper end
        CraftTask task = this.runners.get(taskId);
        if (task != null) {
            task.cancel0();
        }
        task = new CraftTask(
                new Consumer<>() {
                    @Override
                    public void accept(BukkitTask task) {
                        if (!this.check(CraftScheduler.this.temp)) {
                            this.check(CraftScheduler.this.pending);
                        }
                    }

                    private boolean check(final Iterable<CraftTask> collection) {
                        final Iterator<CraftTask> tasks = collection.iterator();
                        while (tasks.hasNext()) {
                            final CraftTask task = tasks.next();
                            if (task.getTaskId() == taskId) {
                                task.cancel0();
                                tasks.remove();
                                if (task.isSync()) {
                                    CraftScheduler.this.runners.remove(taskId);
                                }
                                return true;
                            }
                        }
                        return false;
                    }
                });
        this.handle(task, 0L);
        for (CraftTask taskPending = this.head.getNext(); taskPending != null; taskPending = taskPending.getNext()) {
            if (taskPending == task) {
                return;
            }
            if (taskPending.getTaskId() == taskId) {
                taskPending.cancel0();
            }
        }
    }

    @Override
    public void cancelTasks() {
        // Paper start
        if (!this.isAsyncScheduler) {
            this.asyncScheduler.cancelTasks();
        }
        // Paper end
        final CraftTask task = new CraftTask(
                new Consumer<BukkitTask>() {
                    @Override
                    public void accept(BukkitTask task) {
                        this.check(CraftScheduler.this.pending);
                        this.check(CraftScheduler.this.temp);
                    }

                    void check(final Iterable<CraftTask> collection) {
                        final Iterator<CraftTask> tasks = collection.iterator();
                        while (tasks.hasNext()) {
                            final CraftTask task = tasks.next();
                            task.cancel0();
                            tasks.remove();
                            if (task.isSync()) {
                                CraftScheduler.this.runners.remove(task.getTaskId());
                            }
                        }
                    }
                });
        this.handle(task, 0L);
        for (CraftTask taskPending = this.head.getNext(); taskPending != null; taskPending = taskPending.getNext()) {
            if (taskPending == task) {
                break;
            }
            taskPending.cancel0();
        }
        for (CraftTask runner : this.runners.values()) {
            runner.cancel0();
        }
    }

    @Override
    public boolean isCurrentlyRunning(final int taskId) {
        // Paper start
        if (!this.isAsyncScheduler) {
            if (this.asyncScheduler.isCurrentlyRunning(taskId)) {
                return true;
            }
        }
        // Paper end
        final CraftTask task = this.runners.get(taskId);
        if (task == null) {
            return false;
        }
        if (task.isSync()) {
            return (task == this.currentTask);
        }
        final CraftAsyncTask asyncTask = (CraftAsyncTask) task;
        synchronized (asyncTask.getWorkers()) {
            return !asyncTask.getWorkers().isEmpty();
        }
    }

    @Override
    public boolean isQueued(final int taskId) {
        if (taskId <= 0) {
            return false;
        }
        // Paper start
        if (!this.isAsyncScheduler && this.asyncScheduler.isQueued(taskId)) {
            return true;
        }
        // Paper end
        for (CraftTask task = this.head.getNext(); task != null; task = task.getNext()) {
            if (task.getTaskId() == taskId) {
                return task.getPeriod() >= CraftTask.NO_REPEATING; // The task will run
            }
        }
        CraftTask task = this.runners.get(taskId);
        return task != null && task.getPeriod() >= CraftTask.NO_REPEATING;
    }

    @Override
    public List<BukkitWorker> getActiveWorkers() {
        // Paper start
        if (!isAsyncScheduler) {
            //noinspection TailRecursion
            return this.asyncScheduler.getActiveWorkers();
        }
        // Paper end
        final ArrayList<BukkitWorker> workers = new ArrayList<BukkitWorker>();
        for (final CraftTask taskObj : this.runners.values()) {
            // Iterator will be a best-effort (may fail to grab very new values) if called from an async thread
            if (taskObj.isSync()) {
                continue;
            }
            final CraftAsyncTask task = (CraftAsyncTask) taskObj;
            synchronized (task.getWorkers()) {
                // This will never have an issue with stale threads; it's state-safe
                workers.addAll(task.getWorkers());
            }
        }
        return workers;
    }

    @Override
    public List<BukkitTask> getPendingTasks() {
        final ArrayList<CraftTask> truePending = new ArrayList<CraftTask>();
        for (CraftTask task = this.head.getNext(); task != null; task = task.getNext()) {
            if (task.getTaskId() != -1) {
                // -1 is special code
                truePending.add(task);
            }
        }

        final ArrayList<BukkitTask> pending = new ArrayList<BukkitTask>();
        for (CraftTask task : this.runners.values()) {
            if (task.getPeriod() >= CraftTask.NO_REPEATING) {
                pending.add(task);
            }
        }

        for (final CraftTask task : truePending) {
            if (task.getPeriod() >= CraftTask.NO_REPEATING && !pending.contains(task)) {
                pending.add(task);
            }
        }
        // Paper start
        if (!this.isAsyncScheduler) {
            pending.addAll(this.asyncScheduler.getPendingTasks());
        }
        // Paper end
        return pending;
    }

    /**
     * This method is designed to never block or wait for locks; an immediate execution of all current tasks.
     */
    public void mainThreadHeartbeat() {
        this.currentTick++;
        // Paper start
        if (!this.isAsyncScheduler) {
            this.asyncScheduler.mainThreadHeartbeat();
        }
        // Paper end
        final List<CraftTask> temp = this.temp;
        this.parsePending();
        while (this.isReady(this.currentTick)) {
            final CraftTask task = this.pending.remove();
            if (task.getPeriod() < CraftTask.NO_REPEATING) {
                if (task.isSync()) {
                    this.runners.remove(task.getTaskId(), task);
                }
                this.parsePending();
                continue;
            }
            if (task.isSync()) {
                this.currentTask = task;
                try {
                    task.run();
                } catch (final Throwable throwable) {
                    // Paper start
                    LifeStealZ.LOGGER.warn("Task #{} for LifeStealZ generated an exception", task.getTaskId(), throwable);
                    // Paper end
                } finally {
                    this.currentTask = null;
                }
                this.parsePending();
            } else {
                // this.debugTail = this.debugTail.setNext(new CraftAsyncDebugger(this.currentTick + CraftScheduler.RECENT_TICKS, task.getTaskClass())); // Paper
                LifeStealZ.LOGGER.error("Unexpected Async Task in the Sync Scheduler. Report this to Paper"); // Paper
                // We don't need to parse pending
                // (async tasks must live with race-conditions if they attempt to cancel between these few lines of code)
            }
            final long period = task.getPeriod(); // State consistency
            if (period > 0) {
                task.setNextRun(this.currentTick + period);
                temp.add(task);
            } else if (task.isSync()) {
                this.runners.remove(task.getTaskId());
            }
        }
        this.pending.addAll(temp);
        temp.clear();
        //this.debugHead = this.debugHead.getNextHead(this.currentTick); // Paper
    }

    protected void addTask(final CraftTask task) {
        final CraftTask tailTask = this.tail.getAndSet(task);
        tailTask.setNext(task);
    }

    protected CraftTask handle(final CraftTask task, final long delay) { // Paper
        // Paper start
        if (!this.isAsyncScheduler && !task.isSync()) {
            this.asyncScheduler.handle(task, delay);
            return task;
        }
        // Paper end
        task.setNextRun(this.currentTick + delay);
        this.addTask(task);
        return task;
    }

    private static void validate(final Object task) {
        if (!(task instanceof Runnable) && !(task instanceof Consumer) && !(task instanceof Callable)) {
            throw new IllegalArgumentException("Task must be Runnable, Consumer, or Callable");
        }
    }

    private int nextId() {
        if (runners.size() == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("There are already 2147483647 tasks scheduled! Cannot schedule more");
        }
        int id;
        do {
            id = this.ids.updateAndGet(CraftScheduler.INCREMENT_IDS);
        } while (this.runners.containsKey(id)); // Avoid generating duplicate IDs
        return id;
    }

    void parsePending() { // Paper
        CraftTask head = this.head;
        CraftTask task = head.getNext();
        CraftTask lastTask = head;
        for (; task != null; task = (lastTask = task).getNext()) {
            if (task.getTaskId() == -1) {
                task.run();
            } else if (task.getPeriod() >= CraftTask.NO_REPEATING) {
                this.pending.add(task);
                this.runners.put(task.getTaskId(), task);
            }
        }
        // We split this because of the way things are ordered for all of the async calls in CraftScheduler
        // (it prevents race-conditions)
        for (task = head; task != lastTask; task = head) {
            head = task.getNext();
            task.setNext(null);
        }
        this.head = lastTask;
    }

    private boolean isReady(final int currentTick) {
        return !this.pending.isEmpty() && this.pending.peek().getNextRun() <= currentTick;
    }

    @Override
    public String toString() {
        // Paper start
        return "";
        /*
        int debugTick = this.currentTick;
        StringBuilder string = new StringBuilder("Recent tasks from ").append(debugTick - CraftScheduler.RECENT_TICKS).append('-').append(debugTick).append('{');
        this.debugHead.debugTo(string);
        return string.append('}').toString();
        */
        // Paper end
    }
}
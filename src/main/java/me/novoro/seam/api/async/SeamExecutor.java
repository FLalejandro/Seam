package me.novoro.seam.api.async;

import java.util.concurrent.*;
import java.util.function.Supplier;

import net.minecraft.server.MinecraftServer;

/**
 * Handles async and scheduled execution for Seam.
 */
public class SeamExecutor {
    private final ScheduledExecutorService scheduler;

    protected SeamExecutor(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    public void runSync(MinecraftServer server, Runnable task) {
        ServerScheduler.runSync(server, task);
    }

    public <T> CompletableFuture<T> runSync(MinecraftServer server, Supplier<T> task) {
        return ServerScheduler.runSync(server, task);
    }

    public Future<?> runAsync(Runnable task) {
        return scheduler.submit(wrapSafe(task));
    }

    public <T> CompletableFuture<T> runAsync(Supplier<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.get();
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }, scheduler);
    }

    public ScheduledFuture<?> scheduleAsync(Runnable task, long delay, TimeUnit unit) {
        return scheduler.schedule(wrapSafe(task), delay, unit);
    }

    public ScheduledFuture<?> scheduleRepeatingAsync(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return scheduler.scheduleAtFixedRate(wrapSafe(task), initialDelay, period, unit);
    }

    private Runnable wrapSafe(Runnable task) {
        return () -> {
            try {
                task.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}

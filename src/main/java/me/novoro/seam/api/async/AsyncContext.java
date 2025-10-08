package me.novoro.seam.api.async;

import net.minecraft.server.MinecraftServer;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Helper for composing async and sync operations in a readable way.
 */
public final class AsyncContext {

    private AsyncContext() {}

    public static CompletableFuture<Void> runAsync(SeamExecutor executor, Runnable task) {
        return CompletableFuture.runAsync(task, executor::runAsync);
    }

    public static <T> CompletableFuture<T> supplyAsync(SeamExecutor executor, Supplier<T> task) {
        return executor.runAsync(task);
    }

    public static <T> CompletableFuture<T> thenRunSync(CompletableFuture<T> future, MinecraftServer server, Runnable syncTask) {
        return future.thenApply(result -> {
            server.execute(syncTask);
            return result;
        });
    }
}

package me.novoro.seam.api.async;

import net.minecraft.server.MinecraftServer;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Utility for safely executing tasks on the main server thread.
 */
public final class ServerScheduler {

    private ServerScheduler() {}

    /**
     * Runs a task synchronously on the Minecraft server thread.
     */
    public static void runSync(MinecraftServer server, Runnable runnable) {
        if (server.isOnThread()) runnable.run();
        else server.execute(runnable);
    }

    /**
     * Runs a task synchronously and returns a CompletableFuture for its result.
     */
    public static <T> CompletableFuture<T> runSync(MinecraftServer server, Supplier<T> supplier) {
        if (server.isOnThread()) return CompletableFuture.completedFuture(supplier.get());
        else return server.submit(supplier);
    }
}

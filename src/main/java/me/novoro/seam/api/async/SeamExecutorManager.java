package me.novoro.seam.api.async;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Creates and manages SeamExecutor instances.
 */
public final class SeamExecutorManager {
    private static final Set<SeamExecutor> REGISTERED_EXECUTORS = Collections.synchronizedSet(new HashSet<>());
    private static final ThreadFactoryBuilder THREAD_FACTORY = new ThreadFactoryBuilder().setDaemon(true);
    private static SeamExecutor defaultExecutor;
    private SeamExecutorManager() {}

    /**
     * Creates a new custom executor with a given thread name and thread count.
     */
    public static SeamExecutor create(String threadNameFormat, int threads) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(
                threads, THREAD_FACTORY.setNameFormat(threadNameFormat).build()
        );
        SeamExecutor executor = new SeamExecutor(scheduler);
        REGISTERED_EXECUTORS.add(executor);
        return executor;
    }

    /**
     * Gets or creates the global default executor.
     */
    public static SeamExecutor getDefaultExecutor() {
        if (defaultExecutor == null) {
            defaultExecutor = create("Seam-Async-%d", 2);
        }
        return defaultExecutor;
    }

    /**
     * Shuts down all registered executors.
     */
    public static void shutdownAll() {
        REGISTERED_EXECUTORS.forEach(SeamExecutor::shutdown);
        REGISTERED_EXECUTORS.clear();
        defaultExecutor = null;
    }
}

package net.exemine.api.util;

import lombok.RequiredArgsConstructor;
import net.exemine.api.controller.ApiController;
import net.exemine.api.util.callable.Callback;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Executor {

    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(32);

    public static Task schedule(@NotNull Callback callback) {
        return new Task(callback);
    }

    @RequiredArgsConstructor
    public static class Task {

        @NotNull
        private final Callback callback;

        public void run(boolean async) {
            if (async) {
                runAsync();
            } else {
                // We're not using runSync() here because we want to execute
                // on the original thread the Executor was called
                // (could be the server thread, but not necessarily)
                callback.run();
            }
        }

        public void runSync() {
            SyncCallback syncCallback = ApiController.getInstance().getSyncExecutorCallback();

            if (syncCallback != null) {
                syncCallback.run(callback, 0L, -1L);
            } else {
                callback.run();
            }
        }

        public int runSyncLater(long delayInMillis) {
            return runSyncTimer(delayInMillis, -1L);
        }

        public int runSyncTimer(long delayInMillis, long periodInMillis) {
            ApiController.requireMinecraftPlatform();
            SyncCallback syncCallback = ApiController.getInstance().getSyncExecutorCallback();
            Objects.requireNonNull(syncCallback, "Sync executor callback is not defined");

            long delayInTicks = delayInMillis / 50L;
            long periodInTicks = periodInMillis == -1L ? periodInMillis : periodInMillis / 50L;

            return syncCallback.run(callback, delayInTicks, periodInTicks);
        }

        /**
         * @see #runAsyncTimer(long, long) for exception handling explanation
         */
        public void runAsync() {
            try {
                THREAD_POOL.execute(callback::run);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public ScheduledFuture<?> runAsyncLater(long delayInMillis) {
            return SCHEDULER.schedule(callback::run, delayInMillis, TimeUnit.MILLISECONDS);
        }

        public ScheduledFuture<?> runAsyncTimer(long delayInMillis, long periodInMillis) {
            return SCHEDULER.scheduleAtFixedRate(() -> {
                try {
                    callback.run();
                } catch (Exception e) {
                    // If the code throws a RuntimeException, the execution of the code will stop without
                    // printing a stack trace. Here we're manually handling possible exceptions and therefore
                    // making sure that the task won't terminate and the execution continues
                    // https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ScheduledExecutorService.html
                    e.printStackTrace();
                }
            }, delayInMillis, periodInMillis, TimeUnit.MILLISECONDS);
        }
    }

    public interface SyncCallback {

        /**
         * @return Task ID
         */
        int run(Callback callback, long delay, long period);
    }
}

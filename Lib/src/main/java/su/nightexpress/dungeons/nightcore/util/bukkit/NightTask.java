package su.nightexpress.dungeons.nightcore.util.bukkit;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.bukkit.scheduler.BukkitTask;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import su.nightexpress.dungeons.nightcore.NightPlugin;
import su.nightexpress.dungeons.nightcore.util.TimeUtil;

/**
 * A repeating task handle.
 * <p>
 * Upstream wrapped {@code bridge.scheduler.AdaptedTask} (Spigot/Folia); this version wraps
 * {@link BukkitTask} directly. Together with {@code NightPlugin#runTask*} and
 * {@code manager.AbstractManager#addTask}, this is where all repeating scheduling in the plugin lives.
 */
public class NightTask {

    private final BukkitTask scheduledTask;

    public NightTask(@NonNull NightPlugin plugin, @Nullable BukkitTask scheduledTask) {
        this.scheduledTask = scheduledTask;
    }

    @NonNull
    public static NightTask create(@NonNull NightPlugin plugin, @NonNull Runnable runnable, int interval) {
        return create(plugin, runnable, TimeUtil.secondsToTicks(interval));
    }

    @NonNull
    public static NightTask create(@NonNull NightPlugin plugin, @NonNull Runnable runnable, long interval) {
        return createTask(plugin, () -> interval <= 0 ? null
            : plugin.getScheduler().runTaskTimer(plugin, runnable, 0L, interval));
    }

    @NonNull
    public static NightTask createAsync(@NonNull NightPlugin plugin, @NonNull Runnable runnable, int interval) {
        return createAsync(plugin, runnable, TimeUtil.secondsToTicks(interval));
    }

    @NonNull
    public static NightTask createAsync(@NonNull NightPlugin plugin, @NonNull Runnable runnable, long interval) {
        return createTask(plugin, () -> {
            if (interval <= 0) return null;

            return plugin.getScheduler().runTaskTimer(plugin, () -> {
                CompletableFuture.runAsync(runnable).whenComplete((empty, throwable) -> {
                    if (throwable != null) {
                        plugin.error("Async task failed.", throwable);
                    }
                });
            }, 0L, interval);
        });
    }

    @NonNull
    private static NightTask createTask(@NonNull NightPlugin plugin, @NonNull Supplier<BukkitTask> supplier) {
        return new NightTask(plugin, supplier.get());
    }

    @Nullable
    public BukkitTask getScheduledTask() {
        return this.scheduledTask;
    }

    public boolean isValid() {
        return this.scheduledTask != null;
    }

    public boolean stop() {
        if (this.scheduledTask == null) return false;

        this.scheduledTask.cancel();
        return true;
    }
}

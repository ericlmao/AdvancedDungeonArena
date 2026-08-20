package su.nightexpress.dungeons.nightcore.util.bukkit;

import java.time.Duration;
import java.util.function.Supplier;

import gg.moonrise.scheduler.Scheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import su.nightexpress.dungeons.nightcore.NightPlugin;
import su.nightexpress.dungeons.nightcore.util.TimeUtil;

/**
 * A repeating task handle.
 * <p>
 * Upstream wrapped {@code bridge.scheduler.AdaptedTask} (Spigot/Folia); the first vendored pass wrapped
 * {@link org.bukkit.scheduler.BukkitTask}. This version wraps Folia's {@link ScheduledTask}, obtained
 * through the shaded folia-scheduler facade, so the same handle type covers the global, region, entity and
 * async schedulers.
 * <p>
 * Together with {@code NightPlugin#runTask*} and {@code manager.AbstractManager#addTask}, this is where all
 * repeating scheduling in the plugin lives.
 *
 * <h2>Interval units</h2>
 * The {@code int} overloads take <b>seconds</b> and the {@code long} overloads take <b>ticks</b>. That is
 * inherited from upstream and is a well-known foot-gun; it is preserved verbatim here so that no call site
 * silently changes its real-time period during the Folia migration.
 */
public class NightTask {

    private final ScheduledTask scheduledTask;

    public NightTask(@NonNull NightPlugin plugin, @Nullable ScheduledTask scheduledTask) {
        this.scheduledTask = scheduledTask;
    }

    // -------------------------------------------------------------------------------------------------
    // Global-region timers. Use these only for plugin-wide state that touches no world, entity or block.
    // -------------------------------------------------------------------------------------------------

    /** @param interval period in <b>seconds</b>. */
    @NonNull
    public static NightTask create(@NonNull NightPlugin plugin, @NonNull Runnable runnable, int interval) {
        return create(plugin, runnable, TimeUtil.secondsToTicks(interval));
    }

    /** @param interval period in <b>ticks</b>. */
    @NonNull
    public static NightTask create(@NonNull NightPlugin plugin, @NonNull Runnable runnable, long interval) {
        return createTask(plugin, () -> interval <= 0 ? null
            // Folia's runAtFixedRate rejects a delay below 1 tick, so the old 0L initial delay becomes 1L.
            : Scheduler.sync().schedule(task -> runnable.run(), 1L, interval));
    }

    // -------------------------------------------------------------------------------------------------
    // Region timers, anchored on a location the caller owns.
    // -------------------------------------------------------------------------------------------------

    /** @param interval period in <b>ticks</b>. */
    @NonNull
    public static NightTask createAt(@NonNull NightPlugin plugin, @NonNull Location location, @NonNull Runnable runnable, long interval) {
        return createTask(plugin, () -> interval <= 0 ? null
            : Scheduler.location().schedule(location, 1L, interval, task -> runnable.run()));
    }

    // -------------------------------------------------------------------------------------------------
    // Entity timers. The task stops on its own when the entity is removed / the player disconnects.
    // -------------------------------------------------------------------------------------------------

    /** @param interval period in <b>ticks</b>. */
    @NonNull
    public static NightTask createFor(@NonNull NightPlugin plugin, @NonNull Entity entity, @NonNull Runnable runnable, long interval) {
        return createTask(plugin, () -> interval <= 0 ? null
            : Scheduler.entity(entity).schedule(task -> runnable.run(), 1L, interval));
    }

    // -------------------------------------------------------------------------------------------------
    // Async timers. Wall-clock, never ticks - see AsyncScheduler. Must not touch Bukkit state.
    // -------------------------------------------------------------------------------------------------

    /** @param interval period in <b>seconds</b>. */
    @NonNull
    public static NightTask createAsync(@NonNull NightPlugin plugin, @NonNull Runnable runnable, int interval) {
        return createAsync(plugin, runnable, TimeUtil.secondsToTicks(interval));
    }

    /**
     * @param interval period in <b>ticks</b>. Folia's async scheduler is wall-clock based, so the tick count
     *                 is converted at the canonical 50 ms/tick before being handed over. That keeps the
     *                 real-time period identical to the old {@code runTaskTimerAsynchronously} behaviour on
     *                 a healthy server.
     */
    @NonNull
    public static NightTask createAsync(@NonNull NightPlugin plugin, @NonNull Runnable runnable, long interval) {
        return createTask(plugin, () -> {
            if (interval <= 0) return null;

            Duration period = Duration.ofMillis(interval * 50L);

            return Scheduler.async().schedule(task -> {
                try {
                    runnable.run();
                }
                catch (Throwable throwable) {
                    plugin.error("Async task failed.", throwable);
                }
            }, period, period);
        });
    }

    @NonNull
    private static NightTask createTask(@NonNull NightPlugin plugin, @NonNull Supplier<ScheduledTask> supplier) {
        NightTask task = new NightTask(plugin, supplier.get());
        if (task.isValid()) {
            // Folia has no `cancelTasks(plugin)` that reaches region and entity tasks, so every repeating
            // handle has to be tracked and cancelled explicitly on disable.
            plugin.trackTask(task.scheduledTask);
        }
        return task;
    }

    @Nullable
    public ScheduledTask getScheduledTask() {
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

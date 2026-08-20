package su.nightexpress.dungeons.dungeon.script.task;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.dungeon.script.task.impl.*;
import su.nightexpress.dungeons.nightcore.config.FileConfig;

import java.util.HashMap;
import java.util.Map;

public class TaskRegistry {

    private static final Map<String, Loader> LOADERS = new HashMap<>();

    public static void load() {
        addLoader(TaskId.TICK_PASS, TickPassTask::load);
        addLoader(TaskId.KILL_MOB, KillMobTask::load);
        addLoader(TaskId.KILL_MOBS, KillMobsTask::load);
        addLoader(TaskId.KILL_LEFTOVERS, KillLeftoversTask::load);
        addLoader(TaskId.STAY_IN, StayInTask::load);
        addLoader(TaskId.MOVE_TO, MoveToTask::load);
    }

    public static void clear() {
        LOADERS.clear();
    }

    @Nullable
    public static Task loadTask(@NonNull String name, @NonNull FileConfig config, @NonNull String path) {
        Loader loader = getLoader(name);
        if (loader == null) return null;

        return loader.load(config, path);
    }

    @Nullable
    public static Loader getLoader(@NonNull String name) {
        return LOADERS.get(name.toLowerCase());
    }

    public static void addLoader(@NonNull String name, @NonNull Loader loader) {
        LOADERS.put(name.toLowerCase(), loader);
    }

    public interface Loader {

        Task load(@NonNull FileConfig config, @NonNull String path);

    }
}

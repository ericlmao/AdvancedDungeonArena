package su.nightexpress.dungeons.util;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.DungeonsAPI;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.script.action.Action;
import su.nightexpress.dungeons.dungeon.script.condition.Condition;
import su.nightexpress.dungeons.nightcore.config.FileConfig;

public class ErrorHandler {

    public static void error(@NonNull String text, @NonNull FileConfig config) {
        DungeonsAPI.getPlugin().error(text + " Found in '" + config.getFile().getPath() + "'.");
    }

    public static void error(@NonNull String text, @NonNull FileConfig config, @NonNull String path) {
        DungeonsAPI.getPlugin().error(text + " Found in '" + config.getFile().getPath() + "' -> '" + path + "'.");
    }

    public static void error(@NonNull String text, @NonNull Action action, @NonNull DungeonInstance dungeon) {
        DungeonsAPI.getPlugin().error("[Dungeon: '" + dungeon.getId() + "', Action: '" + action.getName() + "'] " + text);
    }

    public static void error(@NonNull String text, @NonNull Condition condition, @NonNull DungeonInstance dungeon) {
        DungeonsAPI.getPlugin().error("[Dungeon: '" + dungeon.getId() + "', Condition: '" + condition.getName() + "'] " + text);
    }

    public static void error(@NonNull String text, @NonNull DungeonInstance dungeon) {
        DungeonsAPI.getPlugin().error("[Dungeon: '" + dungeon.getId() + "'] " + text);
    }
}

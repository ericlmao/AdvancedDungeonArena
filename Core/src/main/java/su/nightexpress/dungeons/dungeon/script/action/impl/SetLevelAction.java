package su.nightexpress.dungeons.dungeon.script.action.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.level.Level;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.dungeon.script.action.Action;
import su.nightexpress.dungeons.dungeon.script.action.ActionId;
import su.nightexpress.dungeons.util.ErrorHandler;
import su.nightexpress.dungeons.nightcore.config.FileConfig;

public record SetLevelAction(@NonNull String levelId) implements Action {

    @NonNull
    public static SetLevelAction load(@NonNull FileConfig config, @NonNull String path) {
        String levelId = config.getString(path + ".LevelId", "null");

        return new SetLevelAction(levelId);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".LevelId", this.levelId);
    }

    @Override
    public void perform(@NonNull DungeonInstance dungeon, @NonNull DungeonGameEvent event) {
        Level level = dungeon.getConfig().getLevelById(this.levelId);
        if (level == null) {
            ErrorHandler.error("Could not set level '" + this.levelId + "': level does not exist.", this, dungeon);
            return;
        }

        dungeon.setLevel(level);
    }

    @NonNull
    @Override
    public String getName() {
        return ActionId.SET_LEVEL;
    }
}

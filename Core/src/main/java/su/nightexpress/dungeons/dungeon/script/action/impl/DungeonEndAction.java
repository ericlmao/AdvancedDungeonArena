package su.nightexpress.dungeons.dungeon.script.action.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.type.GameResult;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.dungeon.script.action.Action;
import su.nightexpress.dungeons.dungeon.script.action.ActionId;
import su.nightexpress.dungeons.config.Lang;
import su.nightexpress.dungeons.nightcore.config.ConfigValue;
import su.nightexpress.dungeons.nightcore.config.FileConfig;

public class DungeonEndAction implements Action {

    private final int     countdown;
    private final boolean completed;

    public DungeonEndAction(int countdown, boolean completed) {
        this.countdown = countdown;
        this.completed = completed;
    }

    @NonNull
    public static DungeonEndAction load(@NonNull FileConfig config, @NonNull String path) {
        int countdown = ConfigValue.create(path + ".Countdown", 10).read(config);
        boolean victory = ConfigValue.create(path + ".Completed", true).read(config);

        return new DungeonEndAction(countdown, victory);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".Countdown", this.countdown);
        config.set(path + ".Completed", this.completed);
    }

    @NonNull
    @Override
    public String getName() {
        return ActionId.DUNGEON_END;
    }

    @Override
    public void perform(@NonNull DungeonInstance dungeon, @NonNull DungeonGameEvent event) {
        dungeon.setCountdown(this.countdown, this.completed ? GameResult.VICTORY : GameResult.DEFEAT);
        dungeon.broadcast(this.completed ? Lang.DUNGEON_END_COMPLETED : Lang.DUNGEON_END_DEFEAT, replacer -> replacer.replace(dungeon.replacePlaceholders()));
    }
}

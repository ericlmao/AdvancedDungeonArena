package su.nightexpress.dungeons.dungeon.script.action.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.dungeon.script.action.Action;
import su.nightexpress.dungeons.dungeon.script.action.ActionId;
import su.nightexpress.dungeons.dungeon.stage.Stage;
import su.nightexpress.dungeons.util.ErrorHandler;
import su.nightexpress.dungeons.nightcore.config.FileConfig;

public record SetStageAction(String stageId) implements Action {

    @Override
    public void perform(@NonNull DungeonInstance dungeon, @NonNull DungeonGameEvent event) {
        Stage stage = dungeon.getConfig().getStageById(this.stageId);
        if (stage == null) {
            ErrorHandler.error("Could not set stage '" + this.stageId + "': stage does not exist.", this, dungeon);
            return;
        }

        dungeon.setStage(stage);
    }

    @NonNull
    public static SetStageAction load(@NonNull FileConfig config, @NonNull String path) {
        String stageId = config.getString(path + ".StageId", "null");

        return new SetStageAction(stageId);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".StageId", this.stageId);
    }

    @NonNull
    @Override
    public String getName() {
        return ActionId.SET_STAGE;
    }
}

package su.nightexpress.dungeons.dungeon.script.action.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.dungeon.spot.Spot;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.script.action.Action;
import su.nightexpress.dungeons.dungeon.script.action.ActionId;
import su.nightexpress.dungeons.util.ErrorHandler;
import su.nightexpress.dungeons.nightcore.config.ConfigValue;
import su.nightexpress.dungeons.nightcore.config.FileConfig;

public record ResetSpotAction(String spotId) implements Action {

    @NonNull
    public static ResetSpotAction load(@NonNull FileConfig config, @NonNull String path) {
        String spotId = ConfigValue.create(path + ".SpotId", "null").read(config);

        return new ResetSpotAction(spotId);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".SpotId", this.spotId);
    }

    @NonNull
    @Override
    public String getName() {
        return ActionId.RESET_SPOT;
    }

    @Override
    public void perform(@NonNull DungeonInstance dungeon, @NonNull DungeonGameEvent event) {
        Spot spot = dungeon.getConfig().getSpotById(this.spotId);
        if (spot == null) {
            ErrorHandler.error("Invalid spot '" + this.spotId + "'!", this, dungeon);
            return;
        }

        dungeon.resetSpotState(spot);
    }
}

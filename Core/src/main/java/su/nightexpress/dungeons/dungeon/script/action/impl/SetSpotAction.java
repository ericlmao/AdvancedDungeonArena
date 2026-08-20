package su.nightexpress.dungeons.dungeon.script.action.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.Placeholders;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.dungeon.spot.Spot;
import su.nightexpress.dungeons.dungeon.spot.SpotState;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.script.action.Action;
import su.nightexpress.dungeons.dungeon.script.action.ActionId;
import su.nightexpress.dungeons.util.ErrorHandler;
import su.nightexpress.dungeons.nightcore.config.ConfigValue;
import su.nightexpress.dungeons.nightcore.config.FileConfig;

public class SetSpotAction implements Action {

    private final String spotId;
    private final String stateId;

    public SetSpotAction(@NonNull String spotId, @NonNull String stateId) {
        this.spotId = spotId;
        this.stateId = stateId;
    }

    @NonNull
    public static SetSpotAction load(@NonNull FileConfig config, @NonNull String path) {
        String spotId = ConfigValue.create(path + ".SpotId", "null").read(config);
        String stateId = ConfigValue.create(path + ".StateId", Placeholders.DEFAULT).read(config);

        return new SetSpotAction(spotId, stateId);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".SpotId", this.spotId);
        config.set(path + ".StateId", this.stateId);
    }

    @NonNull
    @Override
    public String getName() {
        return ActionId.SET_SPOT;
    }

    @Override
    public void perform(@NonNull DungeonInstance dungeon, @NonNull DungeonGameEvent event) {
        Spot spot = dungeon.getConfig().getSpotById(this.spotId);
        if (spot == null) {
            ErrorHandler.error("Invalid spot '" + this.spotId + "'!", this, dungeon);
            return;
        }

        SpotState state = spot.getState(this.stateId);
        if (state == null) {
            ErrorHandler.error("Invalid spot state '" + this.stateId + "'!", this, dungeon);
            return;
        }

        dungeon.setSpotState(spot, state);
    }
}

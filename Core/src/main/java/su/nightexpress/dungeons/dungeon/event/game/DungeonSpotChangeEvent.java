package su.nightexpress.dungeons.dungeon.event.game;

import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.spot.Spot;
import su.nightexpress.dungeons.dungeon.spot.SpotState;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.DungeonEventType;

public class DungeonSpotChangeEvent extends DungeonGameEvent {

    public static final HandlerList HANDLER_LIST = new HandlerList();

    private final Spot spot;
    private final SpotState state;

    public DungeonSpotChangeEvent(@NonNull DungeonInstance dungeon, @NonNull Spot spot, @NonNull SpotState state) {
        super(DungeonEventType.SPOT_CHANGED, dungeon);
        this.spot = spot;
        this.state = state;
    }

    @NonNull
    public Spot getSpot() {
        return this.spot;
    }

    @NonNull
    public SpotState getState() {
        return this.state;
    }

    @NonNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @NonNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}

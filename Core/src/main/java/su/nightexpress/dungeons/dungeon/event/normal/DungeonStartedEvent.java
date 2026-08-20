package su.nightexpress.dungeons.dungeon.event.normal;

import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.AbstractDungeonEvent;

public class DungeonStartedEvent extends AbstractDungeonEvent {

    public static final HandlerList HANDLER_LIST = new HandlerList();

    public DungeonStartedEvent(@NonNull DungeonInstance dungeon) {
        super(dungeon);
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

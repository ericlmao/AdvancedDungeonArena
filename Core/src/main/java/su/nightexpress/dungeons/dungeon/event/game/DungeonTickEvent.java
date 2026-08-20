package su.nightexpress.dungeons.dungeon.event.game;

import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.DungeonEventType;

public class DungeonTickEvent extends DungeonGameEvent {

    public static final HandlerList HANDLER_LIST = new HandlerList();

    public DungeonTickEvent(@NonNull DungeonInstance dungeon) {
        super(DungeonEventType.DUNGEON_TICK, dungeon);
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

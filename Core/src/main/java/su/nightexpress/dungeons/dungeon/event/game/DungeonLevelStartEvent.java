package su.nightexpress.dungeons.dungeon.event.game;

import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.level.Level;
import su.nightexpress.dungeons.dungeon.event.DungeonEventType;

public final class DungeonLevelStartEvent extends DungeonLevelEvent {

    public static final HandlerList HANDLER_LIST = new HandlerList();

    public DungeonLevelStartEvent(@NonNull DungeonInstance dungeon, @NonNull Level level) {
        super(DungeonEventType.LEVEL_STARTED, dungeon, level);
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

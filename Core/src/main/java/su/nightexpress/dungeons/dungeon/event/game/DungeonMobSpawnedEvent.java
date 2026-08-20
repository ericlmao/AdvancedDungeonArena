package su.nightexpress.dungeons.dungeon.event.game;

import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.dungeon.DungeonEntity;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.DungeonEventType;

public class DungeonMobSpawnedEvent extends DungeonMobEvent {

    public static final HandlerList HANDLER_LIST = new HandlerList();

    public DungeonMobSpawnedEvent(@NonNull DungeonInstance dungeon, @NonNull DungeonEntity dungeonMob) {
        super(DungeonEventType.MOB_SPAWNED, dungeon, dungeonMob);
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

package su.nightexpress.dungeons.dungeon.event.game;

import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.dungeon.player.DungeonGamer;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.DungeonEventType;

public class DungeonPlayerDeathEvent extends DungeonPlayerEvent {

    public static final HandlerList HANDLER_LIST = new HandlerList();

    public DungeonPlayerDeathEvent(@NonNull DungeonInstance dungeon, @Nullable DungeonGamer player) {
        super(DungeonEventType.PLAYER_DEATH, dungeon, player);
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

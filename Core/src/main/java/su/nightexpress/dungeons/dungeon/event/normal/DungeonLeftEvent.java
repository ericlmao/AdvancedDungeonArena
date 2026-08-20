package su.nightexpress.dungeons.dungeon.event.normal;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.dungeon.DungeonPlayer;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.AbstractDungeonEvent;

public class DungeonLeftEvent extends AbstractDungeonEvent {

    public static final HandlerList HANDLER_LIST = new HandlerList();

    private final DungeonPlayer dungeonPlayer;

    public DungeonLeftEvent(@NonNull DungeonInstance dungeon, @NonNull DungeonPlayer dungeonPlayer) {
        super(dungeon);
        this.dungeonPlayer = dungeonPlayer;
    }

    @NonNull
    public DungeonPlayer getDungeonPlayer() {
        return this.dungeonPlayer;
    }

    @NonNull
    public Player getPlayer() {
        return this.dungeonPlayer.getPlayer();
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

package su.nightexpress.dungeons.dungeon.event.normal;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.AbstractDungeonEvent;
import su.nightexpress.dungeons.kit.impl.Kit;

public class DungeonJoinEvent extends AbstractDungeonEvent implements Cancellable {

    public static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final Kit kit;

    private boolean cancelled;

    public DungeonJoinEvent(@NonNull DungeonInstance dungeon, @NonNull Player player, @Nullable Kit kit) {
        super(dungeon);
        this.player = player;
        this.kit = kit;
    }

    @NonNull
    public Player getPlayer() {
        return this.player;
    }

    @Nullable
    public Kit getKit() {
        return this.kit;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
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

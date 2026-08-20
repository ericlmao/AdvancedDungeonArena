package su.nightexpress.dungeons.dungeon.event.normal;

import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.type.GameResult;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.AbstractDungeonEvent;

public class DungeonEndEvent extends AbstractDungeonEvent {

    public static final HandlerList HANDLER_LIST = new HandlerList();

    private final GameResult result;

    public DungeonEndEvent(@NonNull DungeonInstance dungeon, @NonNull GameResult result) {
        super(dungeon);
        this.result = result;
    }

    @NonNull
    public GameResult getResult() {
        return this.result;
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

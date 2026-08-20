package su.nightexpress.dungeons.dungeon.event.game;

import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.DungeonEventType;
import su.nightexpress.dungeons.dungeon.stage.Stage;

public final class DungeonStageStartEvent extends DungeonStageEvent {

    public static final HandlerList HANDLER_LIST = new HandlerList();

    public DungeonStageStartEvent(@NonNull DungeonInstance dungeon, @NonNull Stage stage) {
        super(DungeonEventType.STAGE_STARTED, dungeon, stage);
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

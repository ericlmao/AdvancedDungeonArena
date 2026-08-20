package su.nightexpress.dungeons.dungeon.event.game;

import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.DungeonEventType;
import su.nightexpress.dungeons.dungeon.stage.StageTask;
import su.nightexpress.dungeons.dungeon.stage.task.TaskProgress;

public class DungeonTaskCreatedEvent extends DungeonTaskEvent {

    public static final HandlerList HANDLER_LIST = new HandlerList();

    public DungeonTaskCreatedEvent(@NonNull DungeonInstance dungeon, @NonNull StageTask stageTask, @NonNull TaskProgress progress) {
        super(DungeonEventType.TASK_CREATED, dungeon, stageTask, progress);
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

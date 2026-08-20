package su.nightexpress.dungeons.dungeon.event.game;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.DungeonEventType;
import su.nightexpress.dungeons.dungeon.stage.StageTask;
import su.nightexpress.dungeons.dungeon.stage.task.TaskProgress;

public abstract sealed class DungeonTaskEvent extends DungeonGameEvent implements TaskEvent
    permits DungeonTaskCreatedEvent, DungeonTaskFinishedEvent {

    private final StageTask stageTask;
    private final TaskProgress progress;

    public DungeonTaskEvent(@NonNull DungeonEventType type, @NonNull DungeonInstance dungeon, @NonNull StageTask stageTask, @NonNull TaskProgress progress) {
        super(type, dungeon);
        this.stageTask = stageTask;
        this.progress = progress;
    }

    @Override
    @NonNull
    public StageTask getStageTask() {
        return this.stageTask;
    }

    @Override
    @NonNull
    public TaskProgress getProgress() {
        return this.progress;
    }
}

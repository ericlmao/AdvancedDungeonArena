package su.nightexpress.dungeons.dungeon.event.game;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.stage.StageTask;
import su.nightexpress.dungeons.dungeon.stage.task.TaskProgress;

public interface TaskEvent {

    @NonNull StageTask getStageTask();

    @NonNull TaskProgress getProgress();
}

package su.nightexpress.dungeons.dungeon.script.task;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.dungeon.stage.StageTask;
import su.nightexpress.dungeons.dungeon.stage.task.TaskProgress;
import su.nightexpress.dungeons.nightcore.config.Writeable;

public interface Task extends Writeable {

    @NonNull String getName();

    @NonNull ProgressFormatter getFormatter();

    boolean canBePerPlayer();

    void onTaskAdd(@NonNull DungeonInstance dungeon, @NonNull StageTask stageTask, @NonNull TaskProgress progress);

    void onTaskRemove(@NonNull DungeonInstance dungeon, @NonNull StageTask stageTask, @NonNull TaskProgress progress);

    void progress(@NonNull DungeonGameEvent event, @NonNull DungeonInstance dungeon, @NonNull StageTask stageTask, @NonNull TaskProgress progress);
}

package su.nightexpress.dungeons.dungeon.script.task.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.dungeon.event.DungeonEventType;
import su.nightexpress.dungeons.dungeon.script.task.ProgressFormatter;
import su.nightexpress.dungeons.dungeon.script.task.Task;
import su.nightexpress.dungeons.dungeon.script.task.TaskId;
import su.nightexpress.dungeons.dungeon.stage.StageTask;
import su.nightexpress.dungeons.dungeon.stage.task.TaskProgress;
import su.nightexpress.dungeons.nightcore.config.FileConfig;

public class TickPassTask implements Task {

    @NonNull
    public static TickPassTask load(@NonNull FileConfig config, @NonNull String path) {
        return new TickPassTask();
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        // Nothing to write :p
    }

    @NonNull
    @Override
    public String getName() {
        return TaskId.TICK_PASS;
    }

    @NonNull
    @Override
    public ProgressFormatter getFormatter() {
        return ProgressFormatter.TIME_DIGITAL;
    }

    @Override
    public boolean canBePerPlayer() {
        return false;
    }

    @Override
    public void onTaskAdd(@NonNull DungeonInstance dungeon, @NonNull StageTask stageTask, @NonNull TaskProgress progress) {

    }

    @Override
    public void onTaskRemove(@NonNull DungeonInstance dungeon, @NonNull StageTask stageTask, @NonNull TaskProgress progress) {

    }

    @Override
    public void progress(@NonNull DungeonGameEvent event, @NonNull DungeonInstance dungeon, @NonNull StageTask stageTask, @NonNull TaskProgress progress) {
        if (event.getType() == DungeonEventType.DUNGEON_TICK) {
            progress.addProgress(1);
        }
    }
}

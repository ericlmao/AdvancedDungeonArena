package su.nightexpress.dungeons.dungeon.script.task.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.type.MobFaction;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.dungeon.event.game.DungeonMobEliminatedEvent;
import su.nightexpress.dungeons.dungeon.event.game.DungeonMobSpawnedEvent;
import su.nightexpress.dungeons.dungeon.script.task.ProgressFormatter;
import su.nightexpress.dungeons.dungeon.script.task.Task;
import su.nightexpress.dungeons.dungeon.script.task.TaskId;
import su.nightexpress.dungeons.dungeon.stage.StageTask;
import su.nightexpress.dungeons.dungeon.stage.task.TaskProgress;
import su.nightexpress.dungeons.nightcore.config.FileConfig;

public class KillLeftoversTask implements Task {

    @NonNull
    public static KillLeftoversTask load(@NonNull FileConfig config, @NonNull String path) {
        return new KillLeftoversTask();
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {

    }

    @NonNull
    @Override
    public String getName() {
        return TaskId.KILL_LEFTOVERS;
    }

    @NonNull
    @Override
    public ProgressFormatter getFormatter() {
        return ProgressFormatter.NORMAL;
    }

    @Override
    public boolean canBePerPlayer() {
        return false;
    }

    @Override
    public void onTaskAdd(@NonNull DungeonInstance dungeon, @NonNull StageTask stageTask, @NonNull TaskProgress progress) {
        progress.setRequiredAmount(dungeon.countMobs(MobFaction.ENEMY));
    }

    @Override
    public void onTaskRemove(@NonNull DungeonInstance dungeon, @NonNull StageTask stageTask, @NonNull TaskProgress progress) {

    }

    @Override
    public void progress(@NonNull DungeonGameEvent event, @NonNull DungeonInstance dungeon, @NonNull StageTask stageTask, @NonNull TaskProgress progress) {
        // A leftover is a mob that spawned during the stage and has not been dealt with, so the target
        // count grows with every spawn and the progress with every elimination.
        switch (event) {
            case DungeonMobEliminatedEvent _ -> progress.addProgress(1);
            case DungeonMobSpawnedEvent _ -> progress.setRequiredAmount(progress.getRequiredAmount() + 1);
            default -> { }
        }
    }
}

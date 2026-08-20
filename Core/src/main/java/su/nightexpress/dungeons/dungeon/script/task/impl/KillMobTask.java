package su.nightexpress.dungeons.dungeon.script.task.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.mob.MobIdentifier;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.dungeon.event.game.DungeonMobKilledEvent;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.script.task.ProgressFormatter;
import su.nightexpress.dungeons.dungeon.script.task.Task;
import su.nightexpress.dungeons.dungeon.script.task.TaskId;
import su.nightexpress.dungeons.dungeon.stage.StageTask;
import su.nightexpress.dungeons.dungeon.stage.task.TaskProgress;
import su.nightexpress.dungeons.nightcore.config.FileConfig;

public class KillMobTask implements Task {

    private final MobIdentifier identifier;

    public KillMobTask(@NonNull MobIdentifier identifier) {
        this.identifier = identifier;
    }

    @NonNull
    public static KillMobTask load(@NonNull FileConfig config, @NonNull String path) {
        MobIdentifier mobId = MobIdentifier.read(config, path + ".MobId");

        return new KillMobTask(mobId);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".MobId", this.identifier);
    }

    @NonNull
    @Override
    public String getName() {
        return TaskId.KILL_MOB;
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

    }

    @Override
    public void onTaskRemove(@NonNull DungeonInstance dungeon, @NonNull StageTask stageTask, @NonNull TaskProgress progress) {

    }

    @Override
    public void progress(@NonNull DungeonGameEvent event, @NonNull DungeonInstance dungeon, @NonNull StageTask stageTask, @NonNull TaskProgress progress) {
        if (event instanceof DungeonMobKilledEvent mobKilledEvent) {
            if (mobKilledEvent.getDungeonMob().isMob(this.identifier)) {
                progress.addProgress(1);
            }
        }
    }
}

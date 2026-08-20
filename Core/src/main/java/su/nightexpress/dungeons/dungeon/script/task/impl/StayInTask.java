package su.nightexpress.dungeons.dungeon.script.task.impl;

import org.bukkit.Color;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.dungeon.script.task.ProgressFormatter;
import su.nightexpress.dungeons.dungeon.script.task.TaskId;
import su.nightexpress.dungeons.dungeon.script.task.type.AreaTask;
import su.nightexpress.dungeons.dungeon.stage.StageTask;
import su.nightexpress.dungeons.dungeon.stage.task.TaskProgress;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.util.geodata.pos.BlockPos;

public class StayInTask extends AreaTask {

    public StayInTask(int radius, int height, @NonNull BlockPos targetPos) {
        super(radius, height, targetPos);
    }

    @NonNull
    public static StayInTask load(@NonNull FileConfig config, @NonNull String path) {
        return load(config, path, StayInTask::new);
    }

    @NonNull
    @Override
    public String getName() {
        return TaskId.STAY_IN;
    }

    @NonNull
    @Override
    public ProgressFormatter getFormatter() {
        return ProgressFormatter.TIME_DIGITAL;
    }

    @Override
    protected void onTaskProgress(@NonNull DungeonGameEvent event, @NonNull DungeonInstance dungeon, @NonNull StageTask stageTask, @NonNull TaskProgress progress) {
        boolean allIn = dungeon.hasAlivePlayers() && dungeon.getAlivePlayers().stream().allMatch(this::isInside);

        if (allIn) {
            progress.addProgress(1);
        }

        this.setAreaColor(stageTask, allIn ? Color.LIME : Color.RED);
    }
}

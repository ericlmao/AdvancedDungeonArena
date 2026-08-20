package su.nightexpress.dungeons.dungeon.script.action.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.dungeon.script.action.Action;
import su.nightexpress.dungeons.dungeon.script.action.ActionId;
import su.nightexpress.dungeons.dungeon.stage.Stage;
import su.nightexpress.dungeons.dungeon.stage.StageTask;
import su.nightexpress.dungeons.util.ErrorHandler;
import su.nightexpress.dungeons.nightcore.config.FileConfig;

public record RemoveTaskAction(@NonNull String taskId) implements Action {

    @NonNull
    public static RemoveTaskAction load(@NonNull FileConfig config, @NonNull String path) {
        String taskId = config.getString(path + ".TaskId", "null");
        return new RemoveTaskAction(taskId);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".TaskId", this.taskId);
    }

    @Override
    public void perform(@NonNull DungeonInstance dungeon, @NonNull DungeonGameEvent event) {
        Stage stage = dungeon.getStage();
        StageTask stageTask = stage.getTaskById(this.taskId);
        if (stageTask == null) {
            ErrorHandler.error("Invalid task '" + this.taskId + "'!", this, dungeon);
            return;
        }

        dungeon.removeTask(stageTask);
    }

    @NonNull
    @Override
    public String getName() {
        return ActionId.REMOVE_TASK;
    }
}

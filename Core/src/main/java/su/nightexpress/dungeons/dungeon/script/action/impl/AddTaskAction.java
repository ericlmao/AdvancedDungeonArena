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

public class AddTaskAction implements Action {

    private final String taskId;
    private final boolean replace;

    public AddTaskAction(@NonNull String taskId, boolean replace) {
        this.taskId = taskId;
        this.replace = replace;
    }

    @NonNull
    public static AddTaskAction load(@NonNull FileConfig config, @NonNull String path) {
        String taskId = config.getString(path + ".TaskId", "null");
        boolean replace = config.getBoolean(path + ".Replace", false);

        return new AddTaskAction(taskId, replace);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".TaskId", this.taskId);
        config.set(path + ".Replace", this.replace);
    }

    @Override
    public void perform(@NonNull DungeonInstance dungeon, @NonNull DungeonGameEvent event) {
        Stage stage = dungeon.getStage();
        StageTask stageTask = stage.getTaskById(this.taskId);
        if (stageTask == null) {
            ErrorHandler.error("Invalid task '" + this.taskId + "'!", this, dungeon);
            return;
        }

        if (this.replace || !dungeon.hasTask(stageTask)) {
            dungeon.addTask(stageTask);
        }
    }

    @NonNull
    @Override
    public String getName() {
        return ActionId.ADD_TASK;
    }
}

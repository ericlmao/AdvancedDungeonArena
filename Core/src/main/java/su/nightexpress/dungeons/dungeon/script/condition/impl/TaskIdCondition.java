package su.nightexpress.dungeons.dungeon.script.condition.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.dungeon.event.game.TaskEvent;
import su.nightexpress.dungeons.dungeon.script.condition.Condition;
import su.nightexpress.dungeons.dungeon.script.condition.ConditionId;
import su.nightexpress.dungeons.nightcore.config.ConfigValue;
import su.nightexpress.dungeons.nightcore.config.FileConfig;

public class TaskIdCondition implements Condition {

    private final String taskId;

    public TaskIdCondition(@NonNull String taskId) {
        this.taskId = taskId;
    }

    @NonNull
    public static TaskIdCondition load(@NonNull FileConfig config, @NonNull String path) {
        String id = ConfigValue.create(path + ".TaskId", "null").read(config);

        return new TaskIdCondition(id);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".TaskId", this.taskId);
    }

    @NonNull
    @Override
    public String getName() {
        return ConditionId.TASK_ID;
    }

    @Override
    public boolean test(@NonNull DungeonInstance dungeon, @NonNull DungeonGameEvent event) {
        return event instanceof TaskEvent taskEvent && taskEvent.getStageTask().getId().equalsIgnoreCase(this.taskId);
    }
}

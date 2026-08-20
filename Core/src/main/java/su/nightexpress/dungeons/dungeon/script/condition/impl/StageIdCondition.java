package su.nightexpress.dungeons.dungeon.script.condition.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.dungeon.event.game.StageEvent;
import su.nightexpress.dungeons.dungeon.script.condition.Condition;
import su.nightexpress.dungeons.dungeon.script.condition.ConditionId;
import su.nightexpress.dungeons.nightcore.config.ConfigValue;
import su.nightexpress.dungeons.nightcore.config.FileConfig;

public class StageIdCondition implements Condition {

    private final String stageId;

    public StageIdCondition(@NonNull String stageId) {
        this.stageId = stageId;
    }

    @NonNull
    public static StageIdCondition load(@NonNull FileConfig config, @NonNull String path) {
        String id = ConfigValue.create(path + ".StageId", "null").read(config);

        return new StageIdCondition(id);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".StageId", this.stageId);
    }

    @NonNull
    @Override
    public String getName() {
        return ConditionId.STAGE_ID;
    }

    @Override
    public boolean test(@NonNull DungeonInstance dungeon, @NonNull DungeonGameEvent event) {
        return event instanceof StageEvent stageEvent && stageEvent.getStage().getId().equalsIgnoreCase(this.stageId);
    }
}

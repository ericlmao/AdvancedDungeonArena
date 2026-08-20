package su.nightexpress.dungeons.dungeon.script.condition.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.script.condition.ConditionId;
import su.nightexpress.dungeons.dungeon.script.condition.type.MobAmountCondition;
import su.nightexpress.dungeons.nightcore.config.FileConfig;

@Deprecated
public class SpawnedMobAmountCondition extends MobAmountCondition {

    public SpawnedMobAmountCondition(@NonNull MobData mobData) {
        super(mobData);
    }

    @NonNull
    public static SpawnedMobAmountCondition read(@NonNull FileConfig config, @NonNull String path) {
        return new SpawnedMobAmountCondition(readMobData(config, path));
    }

    @NonNull
    @Override
    public String getName() {
        return ConditionId.SPAWNED_MOB_AMOUNT;
    }

    @Override
    protected double getDungeonValue(@NonNull DungeonInstance dungeon) {
        return dungeon.getStats().countMobSpawns(stage -> true, mob -> mob.isMob(this.identifier));
    }
}

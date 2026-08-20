package su.nightexpress.dungeons.dungeon.script.condition.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.script.condition.ConditionId;
import su.nightexpress.dungeons.dungeon.script.condition.type.MobAmountCondition;
import su.nightexpress.dungeons.nightcore.config.FileConfig;

@Deprecated
public class AliveMobAmountCondition extends MobAmountCondition {

    public AliveMobAmountCondition(@NonNull MobData mobData) {
        super(mobData);
    }

    @NonNull
    public static AliveMobAmountCondition read(@NonNull FileConfig config, @NonNull String path) {
        return new AliveMobAmountCondition(readMobData(config, path));
    }

    @NonNull
    @Override
    public String getName() {
        return ConditionId.ALIVE_MOB_AMOUNT;
    }

    @Override
    protected double getDungeonValue(@NonNull DungeonInstance dungeon) {
        return dungeon.countMobs(mob -> mob.isMob(this.identifier));
    }
}

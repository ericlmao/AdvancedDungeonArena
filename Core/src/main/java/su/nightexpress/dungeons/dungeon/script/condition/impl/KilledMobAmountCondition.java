package su.nightexpress.dungeons.dungeon.script.condition.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.script.condition.ConditionId;
import su.nightexpress.dungeons.dungeon.script.condition.type.MobAmountCondition;
import su.nightexpress.dungeons.nightcore.config.FileConfig;

@Deprecated
public class KilledMobAmountCondition extends MobAmountCondition {

    public KilledMobAmountCondition(@NonNull MobData mobData) {
        super(mobData);
    }

    @NonNull
    public static KilledMobAmountCondition read(@NonNull FileConfig config, @NonNull String path) {
        return new KilledMobAmountCondition(readMobData(config, path));
    }

    @NonNull
    @Override
    public String getName() {
        return ConditionId.KILLED_MOB_AMOUNT;
    }

    @Override
    protected double getDungeonValue(@NonNull DungeonInstance dungeon) {
        //return dungeon.getStats().queryMobStats(MobFilter.byKey(this.identifier)).stream().mapToInt(MobStats::getKilledAmount).sum();

        return dungeon.getStats().countMobKills(stage -> true, mob -> mob.isMob(this.identifier));
    }
}

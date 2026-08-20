package su.nightexpress.dungeons.dungeon.script.condition.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.script.condition.ConditionId;
import su.nightexpress.dungeons.dungeon.script.condition.type.MobsAmountCondition;
import su.nightexpress.dungeons.nightcore.config.FileConfig;

@Deprecated
public class SpawnedMobsAmountCondition extends MobsAmountCondition {

    public SpawnedMobsAmountCondition(@NonNull MobsData data) {
        super(data);
    }

    @NonNull
    public static SpawnedMobsAmountCondition read(@NonNull FileConfig config, @NonNull String path) {
        return new SpawnedMobsAmountCondition(readMobsData(config, path));
    }

    @NonNull
    @Override
    public String getName() {
        return ConditionId.SPAWNED_MOBS_AMOUNT;
    }

    @Override
    protected double getDungeonValue(@NonNull DungeonInstance dungeon) {
        return dungeon.getStats().countMobSpawns(stage -> true, byFaction(this.getFactionLookup()));
    }
}

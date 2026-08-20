package su.nightexpress.dungeons.dungeon.script.condition.impl;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.api.type.MobFaction;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.script.condition.ConditionId;
import su.nightexpress.dungeons.dungeon.script.condition.type.MobsAmountCondition;
import su.nightexpress.dungeons.dungeon.script.number.NumberComparator;
import su.nightexpress.dungeons.nightcore.config.FileConfig;

@Deprecated
public class AliveMobsAmountCondition extends MobsAmountCondition {

    public AliveMobsAmountCondition(@NonNull MobsData data) {
        super(data);
    }

    public AliveMobsAmountCondition(@NonNull NumberComparator comparator, double compareValue, boolean checkFaction, @Nullable MobFaction faction) {
        super(comparator, compareValue, checkFaction, faction);
    }

    @NonNull
    public static AliveMobsAmountCondition read(@NonNull FileConfig config, @NonNull String path) {
        return new AliveMobsAmountCondition(readMobsData(config, path));
    }

    @NonNull
    @Override
    public String getName() {
        return ConditionId.ALIVE_MOBS_AMOUNT;
    }

    @Override
    protected double getDungeonValue(@NonNull DungeonInstance dungeon) {
        return dungeon.countMobs(byFaction(this.getFactionLookup()));
    }
}

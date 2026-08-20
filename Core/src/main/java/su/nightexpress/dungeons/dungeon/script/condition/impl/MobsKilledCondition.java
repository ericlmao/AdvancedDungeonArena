package su.nightexpress.dungeons.dungeon.script.condition.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.criteria.CriteriaProvider;
import su.nightexpress.dungeons.api.criteria.CriterionMob;
import su.nightexpress.dungeons.api.type.MobFaction;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.script.condition.ConditionId;
import su.nightexpress.dungeons.dungeon.script.condition.type.MobsCondition;
import su.nightexpress.dungeons.dungeon.script.number.NumberComparator;
import su.nightexpress.dungeons.dungeon.stage.Stage;
import su.nightexpress.dungeons.nightcore.config.FileConfig;

public class MobsKilledCondition extends MobsCondition {

    public MobsKilledCondition(@NonNull NumberComparator comparator,
                               double compareValue,
                               @NonNull CriteriaProvider<CriterionMob> mobCriterias,
                               @NonNull CriteriaProvider<Stage> stageCriterias) {
        super(comparator, compareValue, mobCriterias, stageCriterias);
    }

    @NonNull
    public static MobsKilledCondition read(@NonNull FileConfig config, @NonNull String path) {
        NumberData data = readNumberData(config, path);
        CriteriaData criteriaData = readCriteriaData(config, path);

        return new MobsKilledCondition(data.comparator(), data.compareValue(), criteriaData.mobCriterias(), criteriaData.stageCriterias());
    }

    @NonNull
    @Override
    public String getName() {
        return ConditionId.MOBS_KILLED;
    }

    @Override
    protected double getDungeonValue(@NonNull DungeonInstance dungeon) {
        return dungeon.getStats().countMobKills(this.stageCriterias.getPredicate(), this.mobCriterias.getPredicate(mob -> mob.isFaction(MobFaction.ENEMY)));
    }
}

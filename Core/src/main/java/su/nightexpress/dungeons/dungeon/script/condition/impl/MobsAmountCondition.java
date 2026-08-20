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

public class MobsAmountCondition extends MobsCondition {

    public MobsAmountCondition(@NonNull NumberComparator comparator,
                               double compareValue,
                               @NonNull CriteriaProvider<CriterionMob> mobCriterias,
                               @NonNull CriteriaProvider<Stage> stageCriterias) {
        super(comparator, compareValue, mobCriterias, stageCriterias);
    }

    @NonNull
    public static MobsAmountCondition read(@NonNull FileConfig config, @NonNull String path) {
        NumberData data = readNumberData(config, path);
        CriteriaData criteriaData = readCriteriaData(config, path);

        return new MobsAmountCondition(data.comparator(), data.compareValue(), criteriaData.mobCriterias(), criteriaData.stageCriterias());
    }

    @NonNull
    @Override
    public String getName() {
        return ConditionId.MOBS_AMOUNT;
    }

    @Override
    protected double getDungeonValue(@NonNull DungeonInstance dungeon) {
        return dungeon.countMobs(this.mobCriterias.getPredicate(mob -> mob.isFaction(MobFaction.ENEMY)));
    }
}

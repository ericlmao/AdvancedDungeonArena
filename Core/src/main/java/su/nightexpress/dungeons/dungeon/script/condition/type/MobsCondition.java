package su.nightexpress.dungeons.dungeon.script.condition.type;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.criteria.CriteriaProvider;
import su.nightexpress.dungeons.api.criteria.CriterionMob;
import su.nightexpress.dungeons.dungeon.criteria.CriteriaMap;
import su.nightexpress.dungeons.dungeon.criteria.registry.CriteriaRegistry;
import su.nightexpress.dungeons.dungeon.script.number.NumberComparator;
import su.nightexpress.dungeons.dungeon.stage.Stage;
import su.nightexpress.dungeons.nightcore.config.FileConfig;

public abstract class MobsCondition extends NumberCompareCondition {

    protected final CriteriaProvider<CriterionMob> mobCriterias;
    protected final CriteriaProvider<Stage>        stageCriterias;

    public record CriteriaData(CriteriaProvider<CriterionMob> mobCriterias, CriteriaProvider<Stage> stageCriterias) {}

    public MobsCondition(@NonNull NumberComparator comparator,
                         double compareValue,
                               @NonNull CriteriaProvider<CriterionMob> mobCriterias,
                               @NonNull CriteriaProvider<Stage> stageCriterias) {
        super(comparator, compareValue);
        this.mobCriterias = mobCriterias;
        this.stageCriterias = stageCriterias;
    }

    @NonNull
    public static CriteriaData readCriteriaData(@NonNull FileConfig config, @NonNull String path) {
        var mobCriterias = CriteriaMap.read(config, path + ".MobCriteria", CriteriaRegistry.MOB);
        var stageCriterias = CriteriaMap.read(config, path + ".StageCriteria", CriteriaRegistry.STAGE);

        return new CriteriaData(mobCriterias, stageCriterias);
    }

    @Override
    protected void writeAdditional(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".MobCriteria", this.mobCriterias);
        config.set(path + ".StageCriteria", this.stageCriterias);
    }
}

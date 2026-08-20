package su.nightexpress.dungeons.dungeon.criteria.registry.stage;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.criteria.registry.CriteriaHolder;
import su.nightexpress.dungeons.dungeon.criteria.registry.stage.impl.StageIdCriteria;

public class StageCriterias {

    public static final StageIdCriteria STAGE_ID = new StageIdCriteria("name");

    public static void setup(@NonNull CriteriaHolder<StageCriteria<?>> holder) {
        holder.register(STAGE_ID);
    }
}

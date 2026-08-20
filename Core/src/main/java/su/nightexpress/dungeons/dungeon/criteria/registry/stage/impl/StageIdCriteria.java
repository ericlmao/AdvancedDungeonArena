package su.nightexpress.dungeons.dungeon.criteria.registry.stage.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.criteria.CriteriaValidators;
import su.nightexpress.dungeons.dungeon.criteria.registry.stage.StageCriteria;
import su.nightexpress.dungeons.dungeon.stage.Stage;

public class StageIdCriteria extends StageCriteria<String> {

    public StageIdCriteria(@NonNull String name) {
        super(CriteriaValidators.STRING, name);
    }

    @Override
    public boolean test(@NonNull Stage stage, @NonNull String value) {
        return stage.getId().equalsIgnoreCase(value);
    }
}

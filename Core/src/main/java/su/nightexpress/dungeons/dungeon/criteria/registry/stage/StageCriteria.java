package su.nightexpress.dungeons.dungeon.criteria.registry.stage;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.criteria.AbstractCriteria;
import su.nightexpress.dungeons.api.criteria.CriteriaValidator;
import su.nightexpress.dungeons.dungeon.stage.Stage;

public abstract class StageCriteria<T> extends AbstractCriteria<T, Stage> {

    public StageCriteria(@NonNull CriteriaValidator<T> parser, @NonNull String name) {
        super(parser, name);
    }
}

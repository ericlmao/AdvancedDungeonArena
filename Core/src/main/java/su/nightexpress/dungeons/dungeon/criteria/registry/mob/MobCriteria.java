package su.nightexpress.dungeons.dungeon.criteria.registry.mob;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.criteria.CriterionMob;
import su.nightexpress.dungeons.dungeon.criteria.AbstractCriteria;
import su.nightexpress.dungeons.api.criteria.CriteriaValidator;

public abstract class MobCriteria<T> extends AbstractCriteria<T, CriterionMob> {

    public MobCriteria(@NonNull CriteriaValidator<T> parser, @NonNull String name) {
        super(parser, name);
    }
}

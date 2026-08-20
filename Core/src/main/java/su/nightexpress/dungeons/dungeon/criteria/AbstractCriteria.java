package su.nightexpress.dungeons.dungeon.criteria;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.criteria.Criteria;
import su.nightexpress.dungeons.api.criteria.CriteriaPredicate;
import su.nightexpress.dungeons.api.criteria.CriteriaValidator;

public abstract class AbstractCriteria<T, E> implements Criteria<T, E> {

    protected final CriteriaValidator<T> validator;
    protected final String               name;

    public AbstractCriteria(@NonNull CriteriaValidator<T> validator, @NonNull String name) {
        this.validator = validator;
        this.name = name;
    }

    @NonNull
    @Override
    public CriteriaValidator<T> getValidator() {
        return this.validator;
    }

    @NonNull
    @Override
    public String getName() {
        return this.name;
    }

    @NonNull
    @Override
    public CriteriaPredicate<T, E> validate(@NonNull String string) {
        return this.predicate(this.validator.deserialize(string));
    }

    @NonNull
    @Override
    public CriteriaPredicate<T, E> predicate(@NonNull T value) {
        return new CriteriaPredicate<>(this, value);
    }
}

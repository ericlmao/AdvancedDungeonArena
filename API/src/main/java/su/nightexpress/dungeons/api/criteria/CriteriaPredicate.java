package su.nightexpress.dungeons.api.criteria;

import org.jspecify.annotations.NonNull;

import java.util.function.Predicate;

public class CriteriaPredicate<T, E> implements Predicate<E> {

    private final Criteria<T, E> criteria;
    private final T              value;

    public CriteriaPredicate(@NonNull Criteria<T, E> criteria, @NonNull T value) {
        this.criteria = criteria;
        this.value = value;
    }

    @NonNull
    public Criteria<T, E> getCriteria() {
        return this.criteria;
    }

    @NonNull
    public T getValue() {
        return this.value;
    }

    @NonNull
    public String getRawValue() {
        return this.criteria.getValidator().serialize(this.value);
    }

    @Override
    public boolean test(@NonNull E entity) {
        return this.criteria.test(entity, this.value);
    }
}

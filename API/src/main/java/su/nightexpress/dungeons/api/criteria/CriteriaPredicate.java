package su.nightexpress.dungeons.api.criteria;

import org.jspecify.annotations.NonNull;

import java.util.function.Predicate;

public record CriteriaPredicate<T, E>(@NonNull Criteria<T, E> criteria, @NonNull T value) implements Predicate<E> {

    @NonNull
    public String getRawValue() {
        return this.criteria.getValidator().serialize(this.value);
    }

    @Override
    public boolean test(@NonNull E entity) {
        return this.criteria.test(entity, this.value);
    }
}

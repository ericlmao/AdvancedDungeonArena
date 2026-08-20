package su.nightexpress.dungeons.api.criteria;

import org.jspecify.annotations.NonNull;

public interface Criteria<T, E> {

    @NonNull CriteriaValidator<T> getValidator();

    @NonNull String getName();

    /**
     * Validates (deserializes) the provided string and wraps it to criteria predicate.
     * @param string String to validate (deserialize) for predicate.
     * @return A wrapped Predicate
     */
    @NonNull CriteriaPredicate<T, E> validate(@NonNull String string);

    @NonNull CriteriaPredicate<T, E> predicate(@NonNull T value);

    boolean test(@NonNull E entity, @NonNull T value);
}

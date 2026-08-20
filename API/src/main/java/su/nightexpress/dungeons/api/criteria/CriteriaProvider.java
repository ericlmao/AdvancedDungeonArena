package su.nightexpress.dungeons.api.criteria;

import org.jspecify.annotations.NonNull;

import java.util.function.Predicate;

public interface CriteriaProvider<E> {

    @NonNull Predicate<E> getPredicate();

    @NonNull Predicate<E> getPredicate(@NonNull Predicate<E> fallback);
}

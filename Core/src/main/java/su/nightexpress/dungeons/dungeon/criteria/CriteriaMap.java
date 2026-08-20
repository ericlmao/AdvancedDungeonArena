package su.nightexpress.dungeons.dungeon.criteria;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.criteria.CriteriaPredicate;
import su.nightexpress.dungeons.api.criteria.CriteriaProvider;
import su.nightexpress.dungeons.dungeon.criteria.registry.CriteriaHolder;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.config.Writeable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class CriteriaMap<C extends AbstractCriteria<?, E>, E> implements CriteriaProvider<E>, Writeable {

    private final Map<C, CriteriaPredicate<?, E>> criterias;

    public CriteriaMap(@NonNull Map<C, CriteriaPredicate<?, E>> criterias) {
        this.criterias = criterias;
    }

    @NonNull
    public static <C extends AbstractCriteria<?, E>, E> CriteriaMap<C, E> read(@NonNull FileConfig config, @NonNull String path, @NonNull CriteriaHolder<C> holder) {
        Map<C, CriteriaPredicate<?, E>> criterias = new HashMap<>();

        config.getSection(path).forEach(id -> {
            C criteria = holder.get(id);
            if (criteria == null) return;

            String value = config.getString(path + "." + id);
            if (value == null) return;

            CriteriaPredicate<?, E> predicate = criteria.validate(value);
            criterias.put(criteria, predicate);
        });

        return new CriteriaMap<>(criterias);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.remove(path);
        this.criterias.forEach((criteria, predicate) -> config.set(path + "." + criteria.getName(), predicate.getRawValue()));
    }

    @Override
    @NonNull
    public Predicate<E> getPredicate() {
        return this.getPredicate(entity -> true);
    }

    @NonNull
    @Override
    public Predicate<E> getPredicate(@NonNull Predicate<E> fallback) {
        return this.criterias.values().stream().map(predicate -> (Predicate<E>) predicate).reduce(Predicate::and).orElse(fallback);
    }
}

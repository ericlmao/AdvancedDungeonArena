package su.nightexpress.dungeons.dungeon.criteria.registry;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.dungeon.criteria.AbstractCriteria;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CriteriaHolder<C extends AbstractCriteria<?, ?>> {

    private final Map<String, C> byId;

    public CriteriaHolder() {
        this.byId = new HashMap<>();
    }

    public void clear() {
        this.byId.clear();
    }

    @NonNull
    public <T extends C> T register(@NonNull T criteria) {
        this.byId.put(criteria.getName().toLowerCase(), criteria);
        return criteria;
    }

    @Nullable
    public C get(@NonNull String name) {
        return this.byId.get(name.toLowerCase());
    }

    @NonNull
    public Set<C> values() {
        return new HashSet<>(this.byId.values());
    }
}

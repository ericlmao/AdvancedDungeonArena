package su.nightexpress.dungeons.dungeon.script.condition;

import org.jspecify.annotations.NonNull;

public class ConditionInfo {

    private final boolean cached;
    private final Condition condition;

    public ConditionInfo(boolean cached, @NonNull Condition condition) {
        this.cached = cached;
        this.condition = condition;
    }

    public boolean isCached() {
        return this.cached;
    }

    @NonNull
    public Condition getCondition() {
        return this.condition;
    }
}

package su.nightexpress.dungeons.dungeon.script.action;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.nightcore.util.random.Rnd;

public class ActionInfo {

    private final String runIfCondition;
    private final double chance;
    private final Action action;

    public ActionInfo(@Nullable String runIfCondition, double chance, Action action) {
        this.runIfCondition = runIfCondition;
        this.chance = chance;
        this.action = action;
    }

    public boolean run(@NonNull DungeonInstance instance, @NonNull DungeonGameEvent event) {
        if (Rnd.chance(this.chance)) {
            this.action.perform(instance, event);
            return true;
        }
        return false;
    }

    @Nullable
    public String getRunIfCondition() {
        return this.runIfCondition;
    }

    @NonNull
    public Action getAction() {
        return this.action;
    }

    public double getChance() {
        return this.chance;
    }
}

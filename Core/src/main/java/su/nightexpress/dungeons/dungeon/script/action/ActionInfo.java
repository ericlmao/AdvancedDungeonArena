package su.nightexpress.dungeons.dungeon.script.action;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.nightcore.util.random.Rnd;

public record ActionInfo(@Nullable String runIfCondition, double chance, Action action) {

    public boolean run(@NonNull DungeonInstance instance, @NonNull DungeonGameEvent event) {
        if (Rnd.chance(this.chance)) {
            this.action.perform(instance, event);
            return true;
        }
        return false;
    }
}

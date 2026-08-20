package su.nightexpress.dungeons.dungeon.script.condition;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.nightcore.config.Writeable;

public interface Condition extends Writeable {

    @NonNull String getName();

    boolean test(@NonNull DungeonInstance dungeon, @NonNull DungeonGameEvent event);
}

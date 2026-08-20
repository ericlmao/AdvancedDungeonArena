package su.nightexpress.dungeons.dungeon.script.action;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.nightcore.config.Writeable;

public interface Action extends Writeable {

    @NonNull String getName();

    void perform(@NonNull DungeonInstance dungeon, @NonNull DungeonGameEvent event);
}

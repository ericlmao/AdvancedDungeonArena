package su.nightexpress.dungeons.dungeon.event.game;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.AbstractDungeonEvent;
import su.nightexpress.dungeons.dungeon.event.DungeonEventType;

public abstract class DungeonGameEvent extends AbstractDungeonEvent {

    protected final DungeonEventType type;

    public DungeonGameEvent(@NonNull DungeonEventType type, @NonNull DungeonInstance dungeon) {
        super(dungeon);
        this.type = type;
    }

    @NonNull
    public DungeonEventType getType() {
        return this.type;
    }
}

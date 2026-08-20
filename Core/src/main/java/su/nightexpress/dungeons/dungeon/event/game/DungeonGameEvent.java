package su.nightexpress.dungeons.dungeon.event.game;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.AbstractDungeonEvent;
import su.nightexpress.dungeons.dungeon.event.DungeonEventType;

/**
 * Everything the dungeon clock reports while a game is running.
 * <p>
 * Sealed because the set is closed and the compiler should say so: a handler that switches on the event
 * gets exhaustiveness checking, and a subtype added without a corresponding branch fails the build rather
 * than falling through at runtime.
 * <p>
 * {@link DungeonEventType} is kept alongside the hierarchy because it is the token users write in their
 * dungeon configs - it names an event in YAML, it does not dispatch. Code that wants to know which event
 * it has should pattern match on the type, not compare the enum.
 */
public abstract sealed class DungeonGameEvent extends AbstractDungeonEvent
    permits DungeonLevelEvent, DungeonMobEvent, DungeonPlayerEvent, DungeonSpotChangeEvent,
            DungeonStageEvent, DungeonTaskEvent, DungeonTickEvent {

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

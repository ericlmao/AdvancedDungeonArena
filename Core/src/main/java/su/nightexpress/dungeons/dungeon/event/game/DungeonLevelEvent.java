package su.nightexpress.dungeons.dungeon.event.game;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.level.Level;
import su.nightexpress.dungeons.dungeon.event.DungeonEventType;

public abstract sealed class DungeonLevelEvent extends DungeonGameEvent permits DungeonLevelStartEvent {

    private final Level level;

    public DungeonLevelEvent(@NonNull DungeonEventType type, @NonNull DungeonInstance dungeon, @NonNull Level level) {
        super(type, dungeon);
        this.level = level;
    }

    @NonNull
    public Level getLevel() {
        return this.level;
    }
}

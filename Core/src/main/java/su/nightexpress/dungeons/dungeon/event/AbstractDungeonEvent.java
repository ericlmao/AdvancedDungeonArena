package su.nightexpress.dungeons.dungeon.event;

import org.bukkit.event.Event;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;

public abstract class AbstractDungeonEvent extends Event {

    protected final DungeonInstance dungeon;

    public AbstractDungeonEvent(@NonNull DungeonInstance dungeon) {
        this.dungeon = dungeon;
    }

    @NonNull
    public DungeonInstance getDungeon() {
        return this.dungeon;
    }
}

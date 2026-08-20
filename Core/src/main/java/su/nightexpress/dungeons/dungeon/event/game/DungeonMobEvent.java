package su.nightexpress.dungeons.dungeon.event.game;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.dungeon.DungeonEntity;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.DungeonEventType;

public abstract class DungeonMobEvent extends DungeonGameEvent implements MobEvent {

    private final DungeonEntity dungeonMob;

    public DungeonMobEvent(@NonNull DungeonEventType type, @NonNull DungeonInstance dungeon, @NonNull DungeonEntity dungeonMob) {
        super(type, dungeon);
        this.dungeonMob = dungeonMob;
    }

    @NonNull
    @Override
    public DungeonEntity getDungeonMob() {
        return this.dungeonMob;
    }
}

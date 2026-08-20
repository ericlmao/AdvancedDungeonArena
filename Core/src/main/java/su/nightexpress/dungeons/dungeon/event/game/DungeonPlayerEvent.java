package su.nightexpress.dungeons.dungeon.event.game;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.dungeon.player.DungeonGamer;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.DungeonEventType;

public abstract sealed class DungeonPlayerEvent extends DungeonGameEvent implements GamerEvent permits DungeonPlayerDeathEvent {

    protected DungeonGamer player;

    public DungeonPlayerEvent(@NonNull DungeonEventType type, @NonNull DungeonInstance dungeon, @Nullable DungeonGamer player) {
        super(type, dungeon);
        this.player = player;
    }

    @Nullable
    @Deprecated
    public DungeonGamer getPlayer() {
        return this.player;
    }

    @Nullable
    @Override
    public DungeonGamer getGamer() {
        return this.player;
    }

    @Override
    public void setGamer(@Nullable DungeonGamer gamer) {
        this.player = gamer;
    }
}

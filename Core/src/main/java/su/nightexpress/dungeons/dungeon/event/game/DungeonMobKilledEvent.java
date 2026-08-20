package su.nightexpress.dungeons.dungeon.event.game;

import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.api.dungeon.DungeonEntity;
import su.nightexpress.dungeons.dungeon.player.DungeonGamer;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.DungeonEventType;

public final class DungeonMobKilledEvent extends DungeonMobEvent implements GamerEvent {

    public static final HandlerList HANDLER_LIST = new HandlerList();

    private DungeonGamer killer;

    public DungeonMobKilledEvent(@NonNull DungeonInstance dungeon, @NonNull DungeonEntity dungeonMob) {
        super(DungeonEventType.MOB_KILLED, dungeon, dungeonMob);
    }

    @Nullable
    public DungeonGamer getKiller() {
        return this.getGamer();
    }

    @Nullable
    @Override
    public DungeonGamer getGamer() {
        return this.killer;
    }

    @Override
    public void setGamer(@Nullable DungeonGamer gamer) {
        this.killer = gamer;
    }

    @NonNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @NonNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}

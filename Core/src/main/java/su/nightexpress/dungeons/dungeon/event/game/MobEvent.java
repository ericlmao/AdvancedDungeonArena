package su.nightexpress.dungeons.dungeon.event.game;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.dungeon.DungeonEntity;

public interface MobEvent {

    @NonNull DungeonEntity getDungeonMob();
}

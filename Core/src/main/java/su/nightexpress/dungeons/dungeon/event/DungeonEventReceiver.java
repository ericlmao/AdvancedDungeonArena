package su.nightexpress.dungeons.dungeon.event;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;

public interface DungeonEventReceiver {

    void addHandler(@NonNull DungeonEventHandler handler);

    boolean onDungeonEventBroadcastReceive(@NonNull DungeonGameEvent event, @NonNull DungeonEventType eventType, @NonNull DungeonInstance dungeon);
}

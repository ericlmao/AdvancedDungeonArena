package su.nightexpress.dungeons.api.dungeon;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.type.MobFaction;

import java.util.UUID;

public interface DungeonHolder {

    @NonNull
    default Dungeon getDungeon() {
        return this.getEntity().getDungeon();
    }

    @NonNull
    default MobFaction getFaction() {
        return this.getEntity().getFaction();
    }

    UUID getUUID();

    default DungeonEntity getEntity() {
        return DungeonEntityBridge.getByMobId(this.getUUID());
    }

    default boolean isValidDungeon() {
        return DungeonEntityBridge.getByMobId(this.getUUID()) != null;
    }
}

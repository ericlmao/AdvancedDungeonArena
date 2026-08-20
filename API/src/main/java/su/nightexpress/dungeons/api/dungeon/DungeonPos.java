package su.nightexpress.dungeons.api.dungeon;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.nightcore.util.geodata.pos.ChunkPos;

// Used as a HashMap key (DungeonManager) - equality was already structural over these two components.
public record DungeonPos(@NonNull String worldName, @NonNull ChunkPos chunkPos) {

}

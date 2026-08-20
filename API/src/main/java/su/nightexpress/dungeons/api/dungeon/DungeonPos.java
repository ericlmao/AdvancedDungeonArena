package su.nightexpress.dungeons.api.dungeon;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.nightcore.util.geodata.pos.ChunkPos;

import java.util.Objects;

public class DungeonPos {

    private final String   worldName;
    private final ChunkPos chunkPos;

    public DungeonPos(@NonNull String worldName, @NonNull ChunkPos chunkPos) {
        this.worldName = worldName;
        this.chunkPos = chunkPos;
    }

    @NonNull
    public String getWorldName() {
        return this.worldName;
    }

    @NonNull
    public ChunkPos getChunkPos() {
        return this.chunkPos;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof DungeonPos that)) return false;
        return Objects.equals(worldName, that.worldName) && Objects.equals(chunkPos, that.chunkPos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldName, chunkPos);
    }
}

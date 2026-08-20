package su.nightexpress.dungeons.api.dungeon;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.nightcore.config.Writeable;
import su.nightexpress.dungeons.nightcore.util.geodata.pos.BlockPos;

import java.util.Set;

public interface DungeonSpawner extends Writeable {

    boolean isEmpty();

    @NonNull
    BlockPos getRandomPosition();

    @NonNull String getId();

    @NonNull Set<BlockPos> getPositions();
}

package su.nightexpress.dungeons.dungeon.config;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.dungeon.DungeonSpawner;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.util.Lists;
import su.nightexpress.dungeons.nightcore.util.geodata.pos.BlockPos;
import su.nightexpress.dungeons.nightcore.util.random.Rnd;

import java.util.Objects;
import java.util.Set;

public class DungeonMobSpawner implements DungeonSpawner {

    private final String id;
    private final Set<BlockPos> positions;

    public DungeonMobSpawner(@NonNull String id, @NonNull Set<BlockPos> positions) {
        this.id = id.toLowerCase();
        this.positions = positions;
    }

    @NonNull
    public static DungeonMobSpawner read(@NonNull FileConfig config, @NonNull String path, @NonNull String id) {
        Set<BlockPos> positions = Lists.modify(config.getStringSet(path + ".Positions"), BlockPos::deserialize);
        positions.removeIf(Objects::isNull);

        return new DungeonMobSpawner(id, positions);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".Positions", this.positions.stream().map(BlockPos::serialize).toList());
    }

    @Override
    public boolean isEmpty() {
        return this.positions.isEmpty();
    }

    @Override
    @NonNull
    public BlockPos getRandomPosition() {
        return Rnd.get(this.positions);
    }

    @Override
    @NonNull
    public String getId() {
        return this.id;
    }

    @Override
    @NonNull
    public Set<BlockPos> getPositions() {
        return this.positions;
    }
}

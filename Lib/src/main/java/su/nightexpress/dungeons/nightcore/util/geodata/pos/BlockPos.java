package su.nightexpress.dungeons.nightcore.util.geodata.pos;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.config.Writeable;
import su.nightexpress.dungeons.nightcore.util.NumberUtil;

public record BlockPos(int x, int y, int z) implements Writeable {

    @NonNull
    public static BlockPos read(@NonNull FileConfig config, @NonNull String path) {
        String str = config.getString(path, "");
        return deserialize(str);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path, this.serialize());
    }

    @NonNull
    public String serialize() {
        return this.x + "," + this.y + "," + this.z;
    }

    @NonNull
    public static BlockPos deserialize(@NonNull String str) {
        String[] split = str.split(",");
        if (split.length < 3) return empty();

        int x = NumberUtil.getAnyInteger(split[0], 0);
        int y = NumberUtil.getAnyInteger(split[1], 0);
        int z = NumberUtil.getAnyInteger(split[2], 0);

        return new BlockPos(x, y, z);
    }


    public boolean isEmpty() {
        return this.x == 0 && this.y == 0 && this.z == 0;
    }

    @NonNull
    public static BlockPos empty() {
        return new BlockPos(0, 0, 0);
    }

    @NonNull
    public static BlockPos from(@NonNull Block block) {
        return new BlockPos(block.getX(), block.getY(), block.getZ());
    }

    @NonNull
    public static BlockPos from(@NonNull Location location) {
        return new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @NonNull
    public static BlockPos from(@NonNull ExactPos pos) {
        return new BlockPos((int) pos.x(), (int) pos.y(), (int) pos.z());
    }


    @NonNull
    public Block toBlock(@NonNull World world) {
        return world.getBlockAt(this.x, this.y, this.z);
    }

    @NonNull
    public Location toLocation(@NonNull World world) {
        return new Location(world, this.x, this.y, this.z);
    }

    @NonNull
    public ChunkPos toChunkPos() {
        return ChunkPos.from(this);
    }

    @NonNull
    public Chunk toChunk(@NonNull World world) {
        return this.toChunkPos().getChunk(world);
    }

    public boolean isChunkLoaded(@NonNull World world) {
        return this.toChunkPos().isLoaded(world);
    }

}

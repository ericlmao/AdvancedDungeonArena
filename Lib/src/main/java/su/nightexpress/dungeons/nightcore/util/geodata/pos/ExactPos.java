package su.nightexpress.dungeons.nightcore.util.geodata.pos;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.config.Writeable;
import su.nightexpress.dungeons.nightcore.util.NumberUtil;

public record ExactPos(double x, double y, double z, float yaw, float pitch) implements Writeable {

    @NonNull
    public static ExactPos read(@NonNull FileConfig config, @NonNull String path) {
        return deserialize(String.valueOf(config.getString(path)));
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path, this.serialize());
    }

    @NonNull
    public String serialize() {
        return this.x + "," + this.y + "," + this.z + "," + this.pitch + "," + this.yaw;
    }

    @NonNull
    public static ExactPos deserialize(@NonNull String str) {
        String[] split = str.split(",");
        if (split.length < 3) return empty();

        double x = NumberUtil.getAnyDouble(split[0], 0);
        double y = NumberUtil.getAnyDouble(split[1], 0);
        double z = NumberUtil.getAnyDouble(split[2], 0);
        float pitch = (float) NumberUtil.getAnyDouble(split[3], 0);
        float yaw = (float) NumberUtil.getAnyDouble(split[4], 0);

        return new ExactPos(x, y, z, yaw, pitch);
    }


    public boolean isEmpty() {
        return this.x == 0 && this.y == 0 && this.z == 0 && this.pitch == 0 && this.yaw == 0;
    }

    @NonNull
    public static ExactPos empty() {
        return new ExactPos(0, 0, 0, 0F, 0F);
    }

    @NonNull
    public static ExactPos from(@NonNull Block block) {
        return new ExactPos(block.getX(), block.getY(), block.getZ(), 0F, 0F);
    }

    @NonNull
    public static ExactPos from(@NonNull BlockPos pos) {
        return new ExactPos(pos.x(), pos.y(), pos.z(), 0F, 0F);
    }

    @NonNull
    public static ExactPos from(@NonNull Location location) {
        return new ExactPos(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location
            .getYaw(), location.getPitch());
    }


    @NonNull
    public Block toBlock(@NonNull World world) {
        return this.toBlockPos().toBlock(world);
    }

    @NonNull
    public Location toLocation(@NonNull World world) {
        Location location = new Location(world, this.x, this.y, this.z);
        location.setPitch(this.pitch);
        location.setYaw(this.yaw);
        return location;
    }

    @NonNull
    public BlockPos toBlockPos() {
        return new BlockPos((int) this.x, (int) this.y, (int) this.z);
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

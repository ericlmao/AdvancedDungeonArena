package su.nightexpress.dungeons.nightcore.util.geodata;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.dungeons.nightcore.util.geodata.pos.BlockPos;
import su.nightexpress.dungeons.nightcore.util.geodata.pos.ChunkPos;
import su.nightexpress.dungeons.nightcore.util.geodata.pos.ExactPos;

import java.util.*;
import java.util.stream.Collectors;

@NullMarked
public class Cuboid {

    private final BlockPos min;
    private final BlockPos max;
    private final BlockPos center;

    private final BlockPos minUp;
    private final BlockPos maxDown;

    private final BlockPos zUp;
    private final BlockPos zDown;

    private final BlockPos xDown;
    private final BlockPos xUp;

    private final boolean empty;

    private final Set<ChunkPos> intersectingChunks;

    public Cuboid(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
    }

    public Cuboid(BlockPos min, BlockPos max) {
        int minX = Math.min(min.x(), max.x());
        int minY = Math.min(min.y(), max.y());
        int minZ = Math.min(min.z(), max.z());

        int maxX = Math.max(min.x(), max.x());
        int maxY = Math.max(min.y(), max.y());
        int maxZ = Math.max(min.z(), max.z());

        this.min = new BlockPos(minX, minY, minZ);
        this.max = new BlockPos(maxX, maxY, maxZ);

        int cx = (int) (minX + (maxX - minX) / 2D);
        int cy = (int) (minY + (maxY - minY) / 2D);
        int cz = (int) (minZ + (maxZ - minZ) / 2D);

        this.center = new BlockPos(cx, cy, cz);

        this.empty = this.min.isEmpty() && this.max.isEmpty();

        minUp = new BlockPos(this.min.x(), this.max.y(), this.min.z());
        maxDown = new BlockPos(this.max.x(), this.min.y(), this.max.z());

        zUp = new BlockPos(this.min.x(), this.max.y(), this.max.z());
        zDown = new BlockPos(this.min.x(), this.min.y(), this.max.z());

        xDown = new BlockPos(this.max.x(), this.min.y(), this.min.z());
        xUp = new BlockPos(this.max.x(), this.max.y(), this.min.z());

        this.intersectingChunks = new HashSet<>(this.getIntersectingChunks());
    }

    public static Cuboid fromCenterAndRadius(BlockPos center, int radius) {
        return new Cuboid(
            center.x() - radius, center.y() - radius, center.z() - radius, center.x() + radius, center
                .y() + radius, center.z() + radius
        );
    }


    @Deprecated
    public Cuboid maxHeight(World world) {
        return this.setHeight(world);
    }


    public Cuboid setHeight(World world) {
        return this.setHeight(world.getMinHeight(), world.getMaxHeight());
    }


    public Cuboid setHeight(int minHeight, int maxHeight) {
        BlockPos min = new BlockPos(this.min.x(), minHeight, this.min.z());
        BlockPos max = new BlockPos(this.max.x(), maxHeight, this.max.z());

        return new Cuboid(min, max);
    }

    public boolean isSimilar(Cuboid other) {
        if (this.isEmpty() || other.isEmpty()) return false;

        return this.min.equals(other.min) && this.max.equals(other.max);
    }

    public boolean isEmpty() {
        return this.empty;
    }

    public boolean contains(Location location) {
        return this.contains(location, DimensionType._3D);
    }

    public boolean contains(Location location, DimensionType type) {
        return this.contains(BlockPos.from(location), type);
    }

    public boolean contains(ExactPos pos) {
        return this.contains(pos.toBlockPos());
    }

    public boolean contains(BlockPos pos) {
        return this.contains(pos, DimensionType._3D);
    }

    public boolean contains(ChunkPos pos) {
        return this.containsX(GeoUtils.shiftToCoord(pos.x())) && this.containsZ(GeoUtils.shiftToCoord(pos.z()));
    }

    public boolean contains(BlockPos pos, DimensionType type) {
        if (!this.containsX(pos.x())) return false;
        if (!this.containsZ(pos.z())) return false;

        if (type == DimensionType._3D) {
            return this.containsY(pos.y());
        }
        return true;
    }

    public boolean containsX(int x) {
        return x >= this.min.x() && x <= this.max.x();
    }

    public boolean containsY(int y) {
        return y >= this.min.y() && y <= this.max.y();
    }

    public boolean containsZ(int z) {
        return z >= this.min.z() && z <= this.max.z();
    }


    public List<Block> getBlocks(World world) {
        List<Block> blocks = new ArrayList<>();

        for (int x = this.min.x(); x <= this.max.x(); x++) {
            for (int y = this.min.y(); y <= this.max.y(); y++) {
                for (int z = this.min.z(); z <= this.max.z(); z++) {
                    Block block = world.getBlockAt(x, y, z);
                    blocks.add(block);
                }
            }
        }

        return blocks;
    }


    public List<BlockPos> getCorners() {
        List<BlockPos> list = new ArrayList<>();

        list.add(this.min);
        list.add(this.max);
        list.add(minUp);
        list.add(maxDown);
        list.add(zUp);
        list.add(zDown);
        list.add(xUp);
        list.add(xDown);

        return list;
    }


    public List<BlockPos> getCornerWiresY() {
        List<BlockPos> list = new ArrayList<>();

        for (int y = this.min.y() + 1; y < this.minUp.y(); y++) {
            list.add(new BlockPos(this.min.x(), y, this.min.z()));
        }

        for (int y = this.max.y() - 1; y > this.maxDown.y(); y--) {
            list.add(new BlockPos(this.max.x(), y, this.max.z()));
        }

        for (int y = this.zDown.y() + 1; y < this.zUp.y(); y++) {
            list.add(new BlockPos(this.zDown.x(), y, this.zDown.z()));
        }

        for (int y = this.xDown.y() + 1; y < this.xUp.y(); y++) {
            list.add(new BlockPos(this.xDown.x(), y, this.xDown.z()));
        }

        return list;
    }


    public List<BlockPos> getCornerWiresX() {
        List<BlockPos> list = new ArrayList<>();

        for (int x = this.min.x() + 1; x < this.xDown.x(); x++) {
            list.add(new BlockPos(x, this.min.y(), this.min.z()));
            list.add(new BlockPos(x, this.minUp.y(), this.minUp.z()));
        }
        for (int x = this.zDown.x() + 1; x < this.max.x(); x++) {
            list.add(new BlockPos(x, this.zDown.y(), this.zDown.z()));
            list.add(new BlockPos(x, this.zUp.y(), this.zUp.z()));
        }

        return list;
    }


    public List<BlockPos> getCornerWiresZ() {
        List<BlockPos> list = new ArrayList<>();

        for (int z = this.min.z() + 1; z < this.zDown.z(); z++) {
            list.add(new BlockPos(this.min.x(), this.min.y(), z));
            list.add(new BlockPos(this.min.x(), this.minUp.y(), z));
        }
        for (int z = this.xDown.z() + 1; z < this.max.z(); z++) {
            list.add(new BlockPos(this.xDown.x(), this.xDown.y(), z));
            list.add(new BlockPos(this.xUp.x(), this.xUp.y(), z));
        }

        return list;
    }

    public boolean isIntersectingWith(Cuboid other) {
        return this.isIntersectingWith(other, DimensionType._3D);
    }

    public boolean isIntersectingWith(Cuboid other, DimensionType type) {
        return other.includedIn(this, type) || this.includedIn(other, type);
    }

    private boolean checkIntersect(float min1, float max1, float min2, float max2) {
        return min1 <= max2 && max1 >= min2;
    }

    public boolean includedIn(Cuboid other, DimensionType dimensionType) {
        if (!this.checkIntersect(this.min.x(), this.max.x(), other.getMin().x(), other.getMax().x()))
            return false;
        if (!this.checkIntersect(this.min.z(), this.max.z(), other.getMin().z(), other.getMax().z()))
            return false;

        if (dimensionType == DimensionType._3D) {
            return this.checkIntersect(this.min.y(), this.max.y(), other.getMin().y(), other.getMax().y());
        }

        return true;
    }


    public Set<Chunk> getIntersectingChunks(World world) {
        return this.getIntersectingChunkPositions().stream().map(pos -> pos.getChunk(world)).collect(Collectors
            .toSet());
    }


    public Set<ChunkPos> getIntersectingChunkPositions() {
        return this.intersectingChunks;
    }


    private Collection<ChunkPos> getIntersectingChunks() {
        List<ChunkPos> chunks = new ArrayList<>();
        if (this.isEmpty()) return chunks;

        int minX = GeoUtils.shiftToChunk(this.min.x());
        int maxX = GeoUtils.shiftToChunk(this.max.x());
        int minZ = GeoUtils.shiftToChunk(this.min.z());
        int maxZ = GeoUtils.shiftToChunk(this.max.z());

        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                chunks.add(new ChunkPos(x, z));
            }
        }

        return chunks;
    }

    public int minX() {
        return this.min.x();
    }

    public int minY() {
        return this.min.y();
    }

    public int minZ() {
        return this.min.z();
    }

    public int maxX() {
        return this.max.x();
    }

    public int maxY() {
        return this.max.y();
    }

    public int maxZ() {
        return this.max.z();
    }

    public BlockPos getMin() {
        return this.min;
    }


    public BlockPos getMax() {
        return this.max;
    }


    public BlockPos getCenter() {
        return this.center;
    }

    public int getVolume() {
        return this.getVolume(DimensionType._3D);
    }

    public int getVolume(DimensionType dimensionType) {
        int xLength = this.max.x() - this.min.x() + 1;
        int yLength = dimensionType == DimensionType._2D ? 1 : this.max.y() - this.min.y() + 1;
        int zLength = this.max.z() - this.min.z() + 1;

        return xLength * zLength * yLength;
    }
}

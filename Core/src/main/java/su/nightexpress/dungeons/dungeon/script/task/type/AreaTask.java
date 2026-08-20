package su.nightexpress.dungeons.dungeon.script.task.type;

import gg.moonrise.scheduler.Scheduler;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.jspecify.annotations.NonNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import su.nightexpress.dungeons.api.dungeon.DungeonPlayer;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.dungeon.event.DungeonEventType;
import su.nightexpress.dungeons.dungeon.script.task.Task;
import su.nightexpress.dungeons.dungeon.stage.StageTask;
import su.nightexpress.dungeons.dungeon.stage.task.TaskProgress;
import su.nightexpress.dungeons.nightcore.config.ConfigValue;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.util.geodata.pos.BlockPos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AreaTask implements Task {

    protected final Map<String, Set<BlockDisplay>> blockLights;

    protected final int      radius;
    protected final int      height;
    protected final BlockPos targetPos;

    public AreaTask(int radius, int height, @NonNull BlockPos targetPos) {
        // Populated from per-chunk region tasks that can run concurrently on Folia.
        this.blockLights = new ConcurrentHashMap<>();

        this.radius = radius;
        this.height = height;
        this.targetPos = targetPos;
    }

    protected interface Creator<T extends AreaTask> {

        @NonNull T create(int radius, int height, @NonNull BlockPos targetPos);
    }

    @NonNull
    protected static <T extends AreaTask> T load(@NonNull FileConfig config, @NonNull String path, @NonNull Creator<T> creator) {
        int radius = ConfigValue.create(path + ".Radius", 5).read(config);
        int height = ConfigValue.create(path + ".Height", 5).read(config);
        BlockPos pos = BlockPos.read(config, path + ".Location");

        return creator.create(radius, height, pos);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".Radius", this.radius);
        config.set(path + ".Height", this.height);
        config.set(path + ".Location", this.targetPos.serialize());
    }

    @Override
    public boolean canBePerPlayer() {
        return false;
    }

    /**
     * Marks out the task area with a ring of glowing {@link BlockDisplay} entities.
     * <p>
     * Both halves of this are region work spread over an arbitrary square: reading {@code getBlockAt} for
     * every column, and spawning one display entity per block. A radius of any size crosses chunk - and
     * therefore potentially Folia region - boundaries, so the area is walked chunk by chunk and each chunk's
     * reads and spawns happen on the region that owns it.
     */
    @Override
    public void onTaskAdd(@NonNull DungeonInstance dungeon, @NonNull StageTask stageTask, @NonNull TaskProgress progress) {
        World world = dungeon.getWorld();
        int fixedY = this.targetPos.getY();

        this.forEachChunkOfArea((chunkX, chunkZ, columns) -> Scheduler.location().executeChunk(world, chunkX, chunkZ, () -> {
            columns.forEach(column -> {
                Block block = world.getBlockAt(column.x(), fixedY, column.z());

                world.spawn(block.getLocation(), BlockDisplay.class, display -> {
                    display.setBlock(block.getBlockData());
                    display.setTransformation(new Transformation(new Vector3f(), new AxisAngle4f(), new Vector3f(0.999f, 0.999f, 0.999f), new AxisAngle4f()));
                    display.setGlowing(true);
                    display.setGlowColorOverride(Color.RED);
                    display.setPersistent(false);
                    this.blockLights.computeIfAbsent(stageTask.getId(), k -> ConcurrentHashMap.newKeySet()).add(display);
                });
            });
        }));
    }

    @Override
    public void onTaskRemove(@NonNull DungeonInstance dungeon, @NonNull StageTask stageTask, @NonNull TaskProgress progress) {
        Set<BlockDisplay> displays = this.blockLights.remove(stageTask.getId());
        if (displays != null) {
            displays.forEach(display -> Scheduler.entity(display).run(task -> display.remove()));
        }
    }

    /** A single (x, z) column of the area, resolved to a block only on its owning region. */
    protected record Column(int x, int z) {}

    protected interface ChunkConsumer {

        void accept(int chunkX, int chunkZ, @NonNull List<Column> columns);
    }

    /** Buckets the circle's columns by chunk so each bucket can be dispatched to its owning region. */
    protected void forEachChunkOfArea(@NonNull ChunkConsumer consumer) {
        Map<Long, List<Column>> byChunk = new LinkedHashMap<>();

        int centerX = this.targetPos.getX();
        int centerZ = this.targetPos.getZ();

        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                if (!this.isInsideCircle(x, z)) continue;

                long chunkKey = Chunk.getChunkKey(x >> 4, z >> 4);
                byChunk.computeIfAbsent(chunkKey, key -> new ArrayList<>()).add(new Column(x, z));
            }
        }

        byChunk.forEach((chunkKey, columns) -> consumer.accept((int) (long) chunkKey, (int) (chunkKey >> 32), columns));
    }

    protected abstract void onTaskProgress(@NonNull DungeonGameEvent event, @NonNull DungeonInstance dungeon, @NonNull StageTask stageTask, @NonNull TaskProgress progress);

    @Override
    public void progress(@NonNull DungeonGameEvent event, @NonNull DungeonInstance dungeon, @NonNull StageTask stageTask, @NonNull TaskProgress progress) {
        if (event.getType() != DungeonEventType.DUNGEON_TICK) return;

        this.onTaskProgress(event, dungeon, stageTask, progress);
    }

    protected void setAreaColor(@NonNull StageTask stageTask, @NonNull Color color) {
        // Recolouring a display is an entity mutation, and the displays are spread across the area, so each
        // one is recoloured on its own scheduler rather than in a sweep from the caller's thread.
        this.blockLights.getOrDefault(stageTask.getId(), Collections.emptySet())
            .forEach(display -> Scheduler.entity(display).run(task -> display.setGlowColorOverride(color)));
    }

    protected boolean isInside(@NonNull DungeonPlayer gamer) {
        // Deliberately the snapshot rather than player.getLocation(): this runs from the dungeon clock, and
        // on Folia the player is very often owned by a different region thread.
        Location location = gamer.getLastKnownLocation();
        if (location == null) return false;

        int yDiff = Math.abs(location.getBlockY() - this.targetPos.getY());
        if (yDiff > this.height) return false;

        return this.isInsideCircle(location.getBlockX(), location.getBlockZ());
    }

    protected boolean isInsideCircle(int x, int z) {
        int dx = this.targetPos.getX() - x;
        int dz = this.targetPos.getZ() - z;
        return (dx * dx + dz * dz) <= (this.radius * this.radius);
    }
}

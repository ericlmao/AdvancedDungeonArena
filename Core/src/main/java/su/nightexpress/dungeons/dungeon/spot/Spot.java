package su.nightexpress.dungeons.dungeon.spot;

import gg.moonrise.scheduler.Scheduler;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.Placeholders;
import su.nightexpress.dungeons.api.schema.SchemaBlock;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.manager.AbstractFileData;
import su.nightexpress.dungeons.nightcore.util.FileUtil;
import su.nightexpress.dungeons.nightcore.util.geodata.pos.BlockPos;

import java.io.File;
import java.util.*;
import java.util.function.UnaryOperator;

public class Spot extends AbstractFileData<DungeonPlugin> {

    private static final String EXT_OLD = ".schema";
    private static final String EXT_NEW = ".schema2";

    private final Map<String, SpotState> stateByIdMap;

    private String    name;
    //private Cuboid cuboid;
    private String defaultStateId;
    private String lastState;

    public Spot(@NonNull DungeonPlugin plugin, @NonNull File file) {
        super(plugin, file);
        this.stateByIdMap = new HashMap<>();
        this.defaultStateId = Placeholders.DEFAULT;
    }

    @Override
    protected boolean onLoad(@NonNull FileConfig config) {
        this.setName(config.getString("Name", this.getId()));
        this.setDefaultStateId(config.getString("DefaultState", Placeholders.DEFAULT));

//        BlockPos min = BlockPos.read(config, "Bounds.Min");
//        BlockPos max = BlockPos.read(config, "Bounds.Max");
//        this.setCuboid(new Cuboid(min, max));

        for (String stateId : config.getSection("States")) {
            SpotState state = SpotState.read(config, "States." + stateId, stateId);
            this.addState(state);
            this.loadStateSchema(state);
        }

        return true;
    }

    @Override
    protected void onSave(@NonNull FileConfig config) {
        config.set("Name", this.name);
        config.set("DefaultState", this.defaultStateId);
//        this.cuboid.getMin().write(config, "Bounds.Min");
//        this.cuboid.getMax().write(config, "Bounds.Max");

        config.set("States", null);
        this.getStates().forEach(state -> {
            config.set("States." + state.getId(), state);
        });
    }

    /**
     * Stamps a spot state back into the world.
     * <p>
     * {@code setSchemaBlock} is a raw NMS write - {@code CraftBlock#setBlockData} plus
     * {@code BlockEntity#loadWithComponents} straight into the {@code ServerLevel}. A spot schema is an
     * arbitrary cuboid and routinely spans several chunks, therefore potentially several Folia regions, so
     * there is no single thread from which the whole loop is legal. Writing from the wrong region here is
     * not a "might throw" - it is silent world corruption and a likely chunk-system crash.
     * <p>
     * The schema is therefore bucketed by chunk and each bucket is dispatched to the region that owns it.
     * Blocks within one chunk keep their original relative order.
     */
    public void build(@NonNull World world, @NonNull SpotState state) {
        Map<Long, List<SchemaBlock>> byChunk = new LinkedHashMap<>();

        state.getSchema().forEach(schemaBlock -> {
            BlockPos pos = schemaBlock.getBlockPos();
            long chunkKey = Chunk.getChunkKey(pos.getX() >> 4, pos.getZ() >> 4);
            byChunk.computeIfAbsent(chunkKey, key -> new ArrayList<>()).add(schemaBlock);
        });

        byChunk.forEach((chunkKey, blocks) -> {
            int chunkX = (int) (long) chunkKey;
            int chunkZ = (int) (chunkKey >> 32);

            Scheduler.location().executeChunk(world, chunkX, chunkZ, () -> blocks.forEach(schemaBlock -> {
                this.plugin.getInternals().setSchemaBlock(world, schemaBlock);
            }));
        });
    }

    public void loadStateSchemas() {
        this.getStates().forEach(this::loadStateSchema);
    }

    public void loadStateSchema(@NonNull SpotState state) {
        boolean compressed = true;

        File file = this.getNewStateSchemaFile(state);
        if (!file.exists()) {
            file = this.getOldStateSchemaFile(state);
            compressed = false;
        }
        if (!file.exists()) return;

        state.loadSchema(this.plugin, file, compressed);
    }

    public void writeStateSchema(@NonNull SpotState state, @NonNull World world, @NonNull List<Block> blocks) {
        File file = this.getNewStateSchemaFile(state);
        FileUtil.create(file);

        this.plugin.getInternals().saveSchema(world, blocks, file);
    }

    public void addStateOrUpdate(@NonNull SpotState state, @NonNull World world, @NonNull List<Block> blocks) {
        this.removeState(state);
        this.addState(state);
        this.writeStateSchema(state, world, blocks);
        this.loadStateSchema(state);
    }

    public void removeState(@NonNull SpotState state) {
        this.stateByIdMap.remove(state.getId());

        File file = this.getAnyStateSchemaFile(state);
        if (file.exists()) {
            file.delete();
        }
    }

    public void removeStates() {
        this.getStates().forEach(this::removeState);
    }

    @NonNull
    public UnaryOperator<String> replacePlaceholders() {
        return Placeholders.SPOT.replacer(this);
    }

    @NonNull
    public File getAnyStateSchemaFile(@NonNull SpotState state) {
        File modern = this.getNewStateSchemaFile(state);
        return modern.exists() ? modern : this.getOldStateSchemaFile(state);
    }

    @NonNull
    public File getOldStateSchemaFile(@NonNull SpotState state) {
        return this.getStateSchemaFile(state, EXT_OLD);
    }

    @NonNull
    public File getNewStateSchemaFile(@NonNull SpotState state) {
        return this.getStateSchemaFile(state, EXT_NEW);
    }

    @NonNull
    private File getStateSchemaFile(@NonNull SpotState state, @NonNull String extension) {
        String name = this.getId() + "_" + state.getId() + extension;
        return new File(this.file.getAbsoluteFile().getParent(), name);
    }

    @Nullable
    public SpotState getDefaultState() {
        return this.getState(this.defaultStateId);
    }

    @NonNull
    public String getName() {
        return this.name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getDefaultStateId() {
        return this.defaultStateId;
    }

    public void setDefaultStateId(@NonNull String defaultStateId) {
        this.defaultStateId = defaultStateId;
    }

//    @NonNull
//    @Deprecated
//    public Cuboid getCuboid() {
//        return this.cuboid;
//    }
//
//    @Deprecated
//    public void setCuboid(@Nullable Cuboid cuboid) {
//        this.cuboid = cuboid;
//    }

    public void addState(@NonNull SpotState state) {
        this.stateByIdMap.put(state.getId(), state);
    }

    @NonNull
    public Map<String, SpotState> getStateByIdMap() {
        return this.stateByIdMap;
    }

    @NonNull
    public Set<SpotState> getStates() {
        return new HashSet<>(this.stateByIdMap.values());
    }

    @Nullable
    public SpotState getState(@NonNull String id) {
        return this.stateByIdMap.get(id.toLowerCase());
    }

    @NonNull
    public String getLastState() {
        return Objects.requireNonNullElse(this.lastState, this.defaultStateId);
    }

    public void setLastState(@Nullable SpotState lastState) {
        this.lastState = lastState == null ? null : lastState.getId();
    }
}

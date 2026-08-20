package su.nightexpress.dungeons.selection;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.config.Config;
import su.nightexpress.dungeons.config.Keys;
import su.nightexpress.dungeons.config.Lang;
import su.nightexpress.dungeons.hook.HookId;
import su.nightexpress.dungeons.selection.impl.CuboidSelection;
import su.nightexpress.dungeons.selection.impl.PositionSelection;
import su.nightexpress.dungeons.selection.impl.Selection;
import su.nightexpress.dungeons.selection.listener.SelectionListener;
import su.nightexpress.dungeons.selection.visual.BlockInfo;
import su.nightexpress.dungeons.selection.visual.Tracker;
import su.nightexpress.dungeons.selection.visual.highlight.BlockHighlighter;
import su.nightexpress.dungeons.selection.visual.highlight.BlockPacketsHighlighter;
import su.nightexpress.dungeons.selection.visual.highlight.BlockProtocolHighlighter;
import su.nightexpress.dungeons.nightcore.manager.AbstractManager;
import su.nightexpress.dungeons.nightcore.util.PDCUtil;
import su.nightexpress.dungeons.nightcore.util.Players;
import su.nightexpress.dungeons.nightcore.util.Plugins;
import su.nightexpress.dungeons.nightcore.util.geodata.Cuboid;
import su.nightexpress.dungeons.nightcore.util.geodata.pos.BlockPos;
import su.nightexpress.dungeons.nightcore.util.geodata.pos.ChunkPos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class SelectionManager extends AbstractManager<DungeonPlugin> {

    public static final float DISPLAY_SIZE = 0.998f;

    // Hoisted out of the render path. `Material#createBlockData` parses a block-state string against the
    // block registry, and the highlight render runs on the async scheduler - doing registry work there once
    // per player per frame was both wasteful and off-thread. Building them once at class init makes the
    // async render a pure read of immutable data.
    private static final BlockData CORNER_DATA = Material.WHITE_STAINED_GLASS.createBlockData();
    private static final BlockData WIRE_DATA   = Material.IRON_CHAIN.createBlockData();
    private static final BlockData WIRE_DATA_X = createBlockData(Material.IRON_CHAIN, Axis.X);
    private static final BlockData WIRE_DATA_Z = createBlockData(Material.IRON_CHAIN, Axis.Z);

    private final Map<UUID, Selection> selectionMap;
    private final Map<UUID, Tracker>   chunkTracker;

    private BlockHighlighter highlighter;

    public SelectionManager(@NonNull DungeonPlugin plugin) {
        super(plugin);
        // Both are read from the async highlight timer and written from the selection listener on the
        // player's own thread. chunkTracker was already concurrent; selectionMap was the same race, missed.
        this.selectionMap = new ConcurrentHashMap<>();
        this.chunkTracker = new ConcurrentHashMap<>();
    }

    @Override
    protected void onLoad() {
        this.loadHighlighter();

        this.addListener(new SelectionListener(this.plugin, this));
    }

    @Override
    protected void onShutdown() {
        if (this.highlighter != null) {
            this.highlighter.clear();
            this.highlighter = null;
        }

        this.plugin.getServer().getOnlinePlayers().forEach(this::removeAll);
    }

    private void loadHighlighter() {
        if (Plugins.isInstalled(HookId.PACKET_EVENTS)) {
            this.highlighter = new BlockPacketsHighlighter(this.plugin);
        }
        else if (Plugins.isInstalled(HookId.PROTOCOL_LIB)) {
            this.highlighter = new BlockProtocolHighlighter(this.plugin);
        }
        else return;

        // `40L` is a long, so it really is 40 ticks / 2 seconds - unlike the `int` overloads elsewhere, which
        // mean seconds. NightTask converts this to a 2 s wall-clock period on the async scheduler, so the
        // real-time interval is unchanged.
        this.addAsyncTask(this::highlightBounds, 40L);
    }

    /**
     * Reset current player's chunk position to force trigger chunk bounds render on next task execution.
     */
    public void resetTrackedChunks() {
        this.chunkTracker.values().forEach(tracker -> tracker.setPreviousPos(null));
    }

    public void highlightBounds() {
        this.chunkTracker.forEach((uuid, tracker) -> {
            Player player = this.plugin.getServer().getPlayer(uuid);
            if (player == null) return;

            Location playerLocation = player.getLocation();
            BlockPos oldPlayerPos = tracker.getPreviousPos();

            // Check if we should skip render.
            if (oldPlayerPos != null && !oldPlayerPos.isEmpty()) {
                // Always shift rendering bounds to player's Y position.
                if (Math.abs(oldPlayerPos.getY() - playerLocation.getBlockY()) < 3) {
                    // Otherwise render only if player went to other chunk.
                    ChunkPos currentChunkPos = ChunkPos.from(playerLocation);
                    ChunkPos oldChunkPos = ChunkPos.from(oldPlayerPos);

                    if (currentChunkPos.equals(oldChunkPos)) return;
                }
            }

            if (tracker.isSelection()) {
                this.highlightSelection(player);
            }

            tracker.setPreviousPos(BlockPos.from(playerLocation));
        });
    }

    public void highlightSelection(@NonNull Player player) {
        Selection selection = this.getSelection(player);
        if (selection == null) return;

        this.highlightSelection(player, selection);
    }

    public void highlightSelection(@NonNull Player player, @NonNull Selection selection) {
        if (selection instanceof CuboidSelection cuboidSelection) {
            this.highlightCuboid(player, cuboidSelection.getFirst(), cuboidSelection.getSecond());
        }
        else if (selection instanceof PositionSelection positionSelection) {
            this.removeVisuals(player);

            Set<BlockInfo> dataSet = new HashSet<>();
            positionSelection.getPositions().forEach(blockPos -> {
                dataSet.add(new BlockInfo(blockPos, CORNER_DATA));
            });

            this.highlightBlocks(player, dataSet);
        }
    }

    private void highlightCuboid(@NonNull Player player, @Nullable BlockPos min, @Nullable BlockPos max) {
        if (min == null) min = BlockPos.empty();
        if (max == null) max = BlockPos.empty();
        if (min.isEmpty() && !max.isEmpty()) min = max;
        if (max.isEmpty() && !min.isEmpty()) max = min;

        this.highlightCuboid(player, new Cuboid(min, max));
    }

    public void highlightCuboid(@NonNull Player player, @NonNull Cuboid cuboid) {
        this.highlightCuboid(player, cuboid, true/*, true*/);
    }

    public void highlightCuboid(@NonNull Player player, @NonNull Cuboid cuboid, boolean reset/*, boolean checkIntersect*/) {
        if (this.highlighter == null) return;

        if (reset) {
            this.removeVisuals(player);
        }

        World world = player.getWorld();
        Set<BlockInfo> dataSet = new HashSet<>();

        // Draw corners of the chunk/region all the time.
        this.collectBlockData(cuboid.getCorners(), dataSet, CORNER_DATA);
        this.collectBlockData(cuboid.getCornerWiresY(), dataSet, WIRE_DATA);

        // Draw connections only for regions or when player is inside a chunk.
        this.collectBlockData(cuboid.getCornerWiresX(), dataSet, WIRE_DATA_X);
        this.collectBlockData(cuboid.getCornerWiresZ(), dataSet, WIRE_DATA_Z);

        // Draw all visual blocks at prepated positions with prepared block data.
        dataSet.forEach(blockInfo -> {
            BlockPos blockPos = blockInfo.getBlockPos();
            Location location = blockPos.toLocation(world);
            ChatColor color = this.getBlockColor(blockPos, cuboid);

            this.highlighter.addVisualBlock(player, location, blockInfo.getBlockData(), color, DISPLAY_SIZE);
        });
    }

    private void highlightBlocks(@NonNull Player player, @NonNull Set<BlockInfo> dataSet) {
        World world = player.getWorld();
        ChatColor color = ChatColor.GREEN;

        dataSet.forEach(blockInfo -> {
            BlockPos blockPos = blockInfo.getBlockPos();
            Location location = blockPos.toLocation(world);

            this.highlighter.addVisualBlock(player, location, blockInfo.getBlockData(), color, DISPLAY_SIZE);
        });
    }

    @NonNull
    private ChatColor getBlockColor(@NonNull BlockPos blockPos, @NonNull Cuboid cuboid) {
        ChatColor color;

        color = ChatColor.WHITE;
        if (blockPos.equals(cuboid.getMin()) || blockPos.equals(cuboid.getMax())) {
            color = ChatColor.GREEN;
        }

        return color;
    }

    private void collectBlockData(@NonNull Collection<BlockPos> source, @NonNull Set<BlockInfo> target, @NonNull BlockData data) {
        if (data.getMaterial().isAir()) return;

        source.stream().filter(blockPos -> blockPos != null && !blockPos.isEmpty()).map(blockPos -> new BlockInfo(blockPos, data)).forEach(target::add);
    }

    @NonNull
    private static BlockData createBlockData(@NonNull Material material, @NonNull Axis axis) {
        BlockData data = material.createBlockData();
        if (data instanceof Orientable orientable) {
            orientable.setAxis(axis);
        }
        return data;
    }



    @NonNull
    public ItemStack getItem() {
        ItemStack itemStack = Config.ITEMS_WAND_ITEM.get().getItemStack();
        PDCUtil.set(itemStack, Keys.dungeonWand, true);
        return itemStack;
    }

    public boolean isItem(@NonNull ItemStack itemStack) {
        return PDCUtil.getBoolean(itemStack, Keys.dungeonWand).isPresent();
    }

    public void onItemUse(@NonNull Player player, @NonNull Block block, @NonNull Action action) {
        this.selectPosition(player, block.getLocation(), action);
    }

    public void onItemDrop(@NonNull Player player) {
        this.stopSelection(player);
    }

    public boolean isInSelection(@NonNull Player player) {
        return this.getSelection(player) != null;
    }

    @Nullable
    public Selection getSelection(@NonNull Player player) {
        return this.selectionMap.get(player.getUniqueId());
    }

    public void removeAll(@NonNull Player player) {
        if (this.isInSelection(player)) {
            this.stopSelection(player);
        }
        this.chunkTracker.remove(player.getUniqueId());
    }

    public void removeVisuals(@NonNull Player player) {
        if (this.highlighter != null) {
            this.highlighter.removeVisuals(player);
        }
    }

    @NonNull
    public Tracker addTracker(@NonNull Player player) {
        return this.chunkTracker.computeIfAbsent(player.getUniqueId(), k -> new Tracker());
    }

    @Nullable
    public Tracker getTracker(@NonNull Player player) {
        return this.chunkTracker.get(player.getUniqueId());
    }

    public void removeTracker(@NonNull Player player, @NonNull Consumer<Tracker> consumer) {
        Tracker tracker = this.getTracker(player);
        if (tracker == null) return;

        consumer.accept(tracker);

        if (!tracker.isSelection()) {
            this.chunkTracker.remove(player.getUniqueId());
        }
    }

    public void removeTracker(@NonNull Player player) {
        this.chunkTracker.remove(player.getUniqueId());
    }



    @NonNull
    public Selection startSelection(@NonNull Player player, @NonNull SelectionType type) {
        this.stopSelection(player);

        Selection selection = Selection.create(type);
        this.selectionMap.put(player.getUniqueId(), selection);
        this.addTracker(player).setSelection(true);

        Players.addItem(player, this.getItem());
        Lang.SETUP_SELECTION_ACTIVATED.message().send(player);
        return selection;
    }

    public void stopSelection(@NonNull Player player) {
        this.removeVisuals(player);
        this.removeTracker(player, tracker -> tracker.setSelection(false));

        Players.takeItem(player, this::isItem);
        this.selectionMap.remove(player.getUniqueId());
    }

    public void selectPosition(@NonNull Player player, @NonNull Location location, @NonNull Action action) {
        Selection selection = this.getSelection(player);
        if (selection == null) return;

        BlockPos blockPos = BlockPos.from(location);

        selection.onSelect(player, blockPos, action);

        this.plugin.runTaskAsync(() -> {
            this.highlightSelection(player);
        });
    }
}

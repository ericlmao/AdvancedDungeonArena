package su.nightexpress.dungeons.dungeon.lootchest;

import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.Placeholders;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.scale.ScalableAmount;
import su.nightexpress.dungeons.nightcore.config.ConfigValue;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.manager.AbstractFileData;
import su.nightexpress.dungeons.nightcore.util.geodata.pos.BlockPos;
import su.nightexpress.dungeons.nightcore.util.random.Rnd;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LootChest extends AbstractFileData<DungeonPlugin> {

    private BlockPos blockPos;
//    private int minItems;
//    private int maxItems;
    private ScalableAmount itemsAmount;
    private boolean uniqueOnly;

    private final Map<String, LootItem> itemByIdMap;

    public LootChest(@NonNull DungeonPlugin plugin, @NonNull File file) {
        super(plugin, file);
        this.itemByIdMap = new HashMap<>();
    }

    @Override
    protected boolean onLoad(@NonNull FileConfig config) {
        this.setBlockPos(BlockPos.read(config, "Location"));
        this.itemsAmount = ScalableAmount.read(config, "ItemsAmount");
//        this.setMinItems(ConfigValue.create("MinItems", 0).read(config));
//        this.setMaxItems(ConfigValue.create("MaxItems", -1).read(config));
        this.setUniqueOnly(ConfigValue.create("UniqueOnly", false).read(config));

        config.getSection("Items").forEach(sId -> {
            try {
                LootItem item = LootItem.read(config, "Items." + sId, sId);
                this.itemByIdMap.put(sId.toLowerCase(), item);
            }
            catch (IllegalStateException exception) {
                this.plugin.warn("Loot item '%s' in '%s' can not be loaded: %s".formatted(sId, this.file.getPath(), exception.getMessage()));
            }
        });

        return true;
    }

    @Override
    protected void onSave(@NonNull FileConfig config) {
        config.set("Location", this.blockPos);
        config.set("ItemsAmount", this.itemsAmount);
//        config.set("MinItems", this.minItems);
//        config.set("MaxItems", this.maxItems);
        config.set("UniqueOnly", this.uniqueOnly);
        config.remove("Items");

        this.itemByIdMap.forEach((id, item) -> {
            config.set("Items." + id, item);
        });
    }

    @NonNull
    public UnaryOperator<String> replacePlaceholders() {
        return Placeholders.LOOT_CHEST.replacer(this);
    }

    // TODO Highlight loot chest when generated per player?

    public void generateLoot(@NonNull DungeonInstance dungeon) {
        Container container = this.getContainer(dungeon);
        if (container == null) return;

        Inventory inventory = container.getInventory();
        int inventorySize = inventory.getSize();

//        int minLoot = Math.max(0, this.minItems);
//        int maxLoot = this.maxItems < 0 ? inventorySize : this.maxItems;

        int lootCount = this.itemsAmount.getScaledInt(dungeon);// Rnd.get(minLoot, maxLoot);
        if (lootCount <= 0) return;

        Set<Integer> freeSlots = IntStream.range(0, inventorySize).boxed().collect(Collectors.toCollection(HashSet::new));
        Map<LootItem, Double> weightMap = new HashMap<>();
        this.getItems().forEach(item -> {
            weightMap.put(item, item.weight());
        });

        inventory.clear();

        while (lootCount > 0 && !freeSlots.isEmpty() && !weightMap.isEmpty()) {
            LootItem item = Rnd.getByWeight(weightMap);

            int slot = Rnd.get(freeSlots);
            inventory.setItem(slot, item.item().getItemStack());

            if (this.uniqueOnly) {
                weightMap.remove(item);
            }

            lootCount--;
            freeSlots.remove(slot);
        }
    }

    public void clearLoot(@NonNull DungeonInstance dungeon) {
        Container container = this.getContainer(dungeon);
        if (container == null) return;

        Inventory inventory = container.getInventory();
        inventory.clear();
    }

    @Nullable
    private Container getContainer(@NonNull DungeonInstance dungeon) {
        if (!dungeon.isActive()) return null;

        Block block = this.blockPos.toBlock(dungeon.getWorld());
        if (block.getState() instanceof Container container) {
            return container;
        }

        return null;
    }

    @NonNull
    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public void setBlockPos(@NonNull BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    @NonNull
    public ScalableAmount getItemsAmount() {
        return this.itemsAmount;
    }

    public void setItemsAmount(@NonNull ScalableAmount itemsAmount) {
        this.itemsAmount = itemsAmount;
    }

//    public int getMinItems() {
//        return this.minItems;
//    }
//
//    public void setMinItems(int minItems) {
//        this.minItems = minItems;
//    }
//
//    public int getMaxItems() {
//        return this.maxItems;
//    }
//
//    public void setMaxItems(int maxItems) {
//        this.maxItems = maxItems;
//    }

    public boolean isUniqueOnly() {
        return this.uniqueOnly;
    }

    public void setUniqueOnly(boolean uniqueOnly) {
        this.uniqueOnly = uniqueOnly;
    }

    @NonNull
    public Map<String, LootItem> getItemByIdMap() {
        return this.itemByIdMap;
    }

    @NonNull
    public Set<LootItem> getItems() {
        return new HashSet<>(this.itemByIdMap.values());
    }

    @Nullable
    public LootItem getItemById(@NonNull String id) {
        return this.itemByIdMap.get(id.toLowerCase());
    }

    public void addItem(@NonNull LootItem item) {
        this.itemByIdMap.put(item.id(), item);
    }
}

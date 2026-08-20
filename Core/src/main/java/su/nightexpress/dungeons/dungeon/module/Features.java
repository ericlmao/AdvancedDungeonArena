package su.nightexpress.dungeons.dungeon.module;

import org.bukkit.Material;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.config.DungeonConfig;
import su.nightexpress.dungeons.dungeon.feature.LevelRequirement;
import su.nightexpress.dungeons.dungeon.feature.itemfilter.ItemFilterCriteria;
import su.nightexpress.dungeons.dungeon.feature.itemfilter.ItemFilterMode;
import su.nightexpress.dungeons.nightcore.config.ConfigValue;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.config.Writeable;
import su.nightexpress.dungeons.nightcore.integration.currency.CurrencyId;
import su.nightexpress.dungeons.nightcore.util.Lists;
import su.nightexpress.dungeons.nightcore.util.RankTable;

import java.util.*;

public class Features implements Writeable {

    //private final DungeonConfig dungeonConfig;

    private       boolean             permissionRequired;
    private       RankTable           entranceCooldown;
    private final Map<String, Double> entranceCostMap;
    private       List<String>        entranceCommands;
    private       LevelRequirement    levelRequirement;
    private       List<String>        exitCommands;

    private ItemFilterMode     itemFilterMode     = ItemFilterMode.BAN_SPECIFIC;
    private ItemFilterCriteria itemFilterCriteria = new ItemFilterCriteria(Collections.emptyList(), Collections.emptyList(), Lists.newList(Material.ENDER_PEARL, Material.ENCHANTED_GOLDEN_APPLE));

    public Features(@NonNull DungeonConfig dungeonConfig) {
        //this.dungeonConfig = dungeonConfig;

        this.entranceCooldown = RankTable.ranked(0).permissionPrefix("dungeon.cooldown.").addRankValue("admin", 0).build();
        this.entranceCostMap = new LinkedHashMap<>(); // Linked to keep currency order in placeholders.
        this.entranceCommands = new ArrayList<>();
        this.exitCommands = new ArrayList<>();
        this.entranceCostMap.put(CurrencyId.VAULT, 0D);
    }

    public void load(@NonNull FileConfig config, @NonNull String path) {
        this.setPermissionRequired(config.getBoolean(path + ".Permission_Required"));

        this.entranceCooldown = ConfigValue.create(path + ".Entrance.Cooldown", RankTable::read, this.entranceCooldown).read(config);
        this.entranceCommands = ConfigValue.create(path + ".Entrance.Commands", this.entranceCommands).read(config);
        this.exitCommands = ConfigValue.create(path + ".Exit.Commands", this.exitCommands).read(config);

        this.entranceCostMap.clear();
        config.getSection(path + ".Entrance.Payment").forEach(curId -> {
            double price = config.getDouble(path + ".Entrance.Payment." + curId);
            if (price <= 0) return;

            this.entranceCostMap.put(curId.toLowerCase(), price);
        });

        this.levelRequirement = LevelRequirement.read(config, path + ".LevelRequirement");

        this.itemFilterMode = ConfigValue.create(path + ".ItemFilter.Mode", ItemFilterMode.class, this.itemFilterMode).read(config);
        this.itemFilterCriteria = ConfigValue.create(path + ".ItemFilter.Criteria", ItemFilterCriteria::read, this.itemFilterCriteria).read(config);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".Permission_Required", this.permissionRequired);
        config.set(path + ".Entrance.Cooldown", this.entranceCooldown);
        config.set(path + ".Entrance.Commands", this.entranceCommands);
        config.set(path + ".Exit.Commands", this.exitCommands);
        config.set(path + ".LevelRequirement", this.levelRequirement);

        config.remove(path + "Entrance.Payment");
        this.entranceCostMap.forEach((id, price) -> config.set(path + ".Entrance.Payment." + id, price));

        config.set(path + ".ItemFilter.Mode", this.itemFilterMode);
        config.set(path + ".ItemFilter.Criteria", this.itemFilterCriteria);
    }

    public boolean hasEntranceCost() {
        return !this.entranceCostMap.isEmpty();
    }

    public boolean isPermissionRequired() {
        return this.permissionRequired;
    }

    public void setPermissionRequired(boolean permissionRequired) {
        this.permissionRequired = permissionRequired;
    }

    @NonNull
    public RankTable getEntranceCooldown() {
        return this.entranceCooldown;
    }

    @NonNull
    public List<String> getEntranceCommands() {
        return this.entranceCommands;
    }

    @NonNull
    public Map<String, Double> getEntranceCostMap() {
        return this.entranceCostMap;
    }

    @NonNull
    public List<String> getExitCommands() {
        return this.exitCommands;
    }

    @NonNull
    public LevelRequirement getLevelRequirement() {
        return this.levelRequirement;
    }

    @NonNull
    public ItemFilterMode getItemFilterMode() {
        return this.itemFilterMode;
    }

    @NonNull
    public ItemFilterCriteria getItemFilterCriteria() {
        return this.itemFilterCriteria;
    }
}

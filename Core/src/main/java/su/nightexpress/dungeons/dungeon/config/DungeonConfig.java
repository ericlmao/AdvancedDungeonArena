package su.nightexpress.dungeons.dungeon.config;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.Placeholders;
import su.nightexpress.dungeons.api.dungeon.DungeonPos;
import su.nightexpress.dungeons.config.Config;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.level.Level;
import su.nightexpress.dungeons.dungeon.lootchest.LootChest;
import su.nightexpress.dungeons.dungeon.module.Features;
import su.nightexpress.dungeons.dungeon.module.GameSettings;
import su.nightexpress.dungeons.dungeon.reward.Reward;
import su.nightexpress.dungeons.dungeon.spot.Spot;
import su.nightexpress.dungeons.dungeon.stage.Stage;
import su.nightexpress.dungeons.util.ErrorHandler;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.manager.AbstractFileData;
import su.nightexpress.dungeons.nightcore.util.FileUtil;
import su.nightexpress.dungeons.nightcore.util.StringUtil;
import su.nightexpress.dungeons.nightcore.util.bukkit.NightItem;
import su.nightexpress.dungeons.nightcore.util.geodata.Cuboid;
import su.nightexpress.dungeons.nightcore.util.geodata.pos.BlockPos;
import su.nightexpress.dungeons.nightcore.util.geodata.pos.ExactPos;
import su.nightexpress.dungeons.nightcore.util.text.night.wrapper.TagWrappers;

import java.io.File;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class DungeonConfig extends AbstractFileData<DungeonPlugin> {

    public static final String FILE_NAME = "config.yml";

    private final Map<String, DungeonMobSpawner> spawnerByIdMap;
    private final Map<String, Stage>             stageByIdMap;
    private final Map<String, Level>             levelByIdMap;
    private final Map<String, Reward>            rewardByIdMap;
    private final Map<String, LootChest>         lootChestByIdMap;
    private final Map<String, Spot>              spotByIdMap;

    private final Features     features;
    private final GameSettings gameSettings;

    private DungeonInstance instance;

    private boolean broken;
    private String  worldName;
    private Cuboid  cuboid;

    private String       startLevelId;
    private String       startStageId;
    private String       name;
    private List<String> description;
    private String       prefix;
    private NightItem    icon;
    private ExactPos     lobbyPos;

    public DungeonConfig(@NonNull DungeonPlugin plugin, @NonNull File file, @NonNull String id) {
        super(plugin, file, id);
        this.spawnerByIdMap = new HashMap<>();
        this.stageByIdMap = new HashMap<>();
        this.levelByIdMap = new HashMap<>();
        this.rewardByIdMap = new HashMap<>();
        this.lootChestByIdMap = new HashMap<>();
        this.spotByIdMap = new HashMap<>();
        this.features = new Features(this);
        this.gameSettings = new GameSettings(this);
        this.lobbyPos = ExactPos.empty();
    }

    @Override
    protected boolean onLoad(@NonNull FileConfig config) {
        this.setWorldName(String.valueOf(config.getString("WorldName")));
        this.setStartLevelId(String.valueOf(config.getString("StartLevel")));
        this.setStartStageId(String.valueOf(config.getString("StartStage")));
        this.setName(config.getString("Name", StringUtil.capitalizeUnderscored(this.getId())));
        this.setDescription(config.getStringList("Description"));
        this.setPrefix(config.getString("Prefix", TagWrappers.GRAY.wrap("[" + Placeholders.DUNGEON_NAME + "]")));
        this.setIcon(config.getCosmeticItem("Icon"));
        this.setLobbyPos(ExactPos.read(config, "LobbyPos"));

        BlockPos cMin = BlockPos.read(config, "Cuboid.Min");
        BlockPos cMax = BlockPos.read(config, "Cuboid.Max");
        this.setCuboid(new Cuboid(cMin, cMax));

        config.getSection("Spawners").forEach(sId -> {
            DungeonMobSpawner spawner = DungeonMobSpawner.read(config, "Spawners." + sId, sId);
            this.addSpawner(spawner);
        });

        this.loadStages();
        this.loadLevels();
        this.loadRewards();
        this.loadLootChests();
        this.loadSpots();

        this.features.load(config, "Features");
        this.gameSettings.load(config, "Game");

        return true;
    }

    @Override
    protected void onSave(@NonNull FileConfig config) {
        config.set("WorldName", this.worldName);
        config.set("StartLevel", this.startLevelId);
        config.set("StartStage", this.startStageId);
        config.set("Name", this.name);
        config.set("Description", this.description);
        config.set("Prefix", this.prefix);
        config.set("Icon", this.getIcon());
        this.lobbyPos.write(config, "LobbyPos");

        this.cuboid.getMin().write(config, "Cuboid.Min");
        this.cuboid.getMax().write(config, "Cuboid.Max");

        this.writeSpawners(config);

        //config.remove("Features");
        //config.remove("Game");
        config.set("Features", this.features);
        config.set("Game", this.gameSettings);
    }

    public void saveSpawners() {
        this.writeSection(this::writeSpawners);
    }

    private void writeSection(@NonNull Consumer<FileConfig> consumer) {
        FileConfig config = this.getConfig();
        consumer.accept(config);
        config.saveChanges();
    }

    private void writeSpawners(@NonNull FileConfig config) {
        config.remove("Spawners");
        this.spawnerByIdMap.forEach((id, spawner) -> config.set("Spawners." + id, spawner));
    }

    @NonNull
    public String getFolderPath() {
        return this.file.getParentFile().getAbsolutePath();
    }

    @NonNull
    public String getStagesPath() {
        return this.getSubPath(Config.DIR_STAGES);
    }

    @NonNull
    public String getLevelsPath() {
        return this.getSubPath(Config.DIR_LEVELS);
    }

    @NonNull
    public String getRewardsPath() {
        return this.getSubPath(Config.DIR_REWARDS);
    }

    @NonNull
    public String getLootChestsPath() {
        return this.getSubPath(Config.DIR_LOOT_CHESTS);
    }

    @NonNull
    public String getSpotsPath() {
        return this.getSubPath(Config.DIR_SPOTS);
    }

    @NonNull
    private String getSubPath(@NonNull String sub) {
        return this.getFolderPath() + sub;
    }

    private void loadStages() {
        this.loadData(this.getStagesPath(), Stage::new, this::addStage);
        this.plugin.info("Loaded " + this.stageByIdMap.size() + " stages for the '" + this.getId() + "' dungeon.");
    }

    private void loadLevels() {
        this.loadData(this.getLevelsPath(), Level::new, this::addLevel);
        this.plugin.info("Loaded " + this.levelByIdMap.size() + " levels for the '" + this.getId() + "' dungeon.");
    }

    private void loadRewards() {
        this.loadData(this.getRewardsPath(), Reward::new, this::addReward);
        this.plugin.info("Loaded " + this.rewardByIdMap.size() + " rewards for the '" + this.getId() + "' dungeon.");
    }

    private void loadLootChests() {
        this.loadData(this.getLootChestsPath(), LootChest::new, this::addLootChest);
        this.plugin.info("Loaded " + this.lootChestByIdMap.size() + " loot chests for the '" + this.getId() + "' dungeon.");
    }

    private void loadSpots() {
        this.loadData(this.getSpotsPath(), Spot::new, this::addSpot);
        this.plugin.info("Loaded " + this.spotByIdMap.size() + " spots for the '" + this.getId() + "' dungeon.");
    }

    private <T extends AbstractFileData<DungeonPlugin>> void loadData(@NonNull String path, @NonNull BiFunction<DungeonPlugin, File, T> function, @NonNull Consumer<T> onLoad) {
        for (File file : this.getFilesInDirectory(path)) {
            T spot = function.apply(this.plugin, file);
            if (spot.load()) {
                onLoad.accept(spot);
            }
            else this.plugin.error("Could not load: '" + file.getPath() + "'.");
        }
    }

    @NonNull
    private List<File> getFilesInDirectory(@NonNull String path) {
        File dir = new File(path);
        dir.mkdirs();

        return FileUtil.getConfigFiles(dir.getAbsolutePath(), true);
    }

    public boolean isBroken() {
        return this.broken;
    }

    public void validate() {
        this.broken = false;
        FileConfig config = this.getConfig();

        if (this.getStartLevel() == null) {
            ErrorHandler.error("Invalid start level '" + this.startLevelId + "'!", config);
            this.broken = true;
        }

        if (this.getStartStage() == null) {
            ErrorHandler.error("Invalid start stage '" + this.startStageId + "'!", config);
            this.broken = true;
        }

        if (this.cuboid.isEmpty()) {
            ErrorHandler.error("No protection defined!", config);
            this.broken = true;
        }

        if (this.lobbyPos.isEmpty()) {
            ErrorHandler.error("No lobby position set!", config);
            this.broken = true;
        }
        else if (!this.cuboid.contains(this.lobbyPos)) {
            ErrorHandler.error("Lobby position is outside of the protection!", config);
            this.broken = true;
        }

        this.getSpawners().forEach(spawner -> {
            spawner.getPositions().forEach(blockPos -> {
                if (!this.cuboid.contains(blockPos)) {
                    ErrorHandler.error("Position '" + blockPos + "' of spawner '" + spawner.getId() + "' is outside of the dungeon protection.", config, "Spawners -> " + spawner.getId());
                }
            });
        });

        this.getLevels().forEach(level -> {
            if (!this.cuboid.contains(level.getSpawnPos().toBlockPos())) {
                ErrorHandler.error("Spawn position of level '" + level.getId() + "' is outside of the dungeon protection.", level.getConfig());
                this.broken = true;
            }
        });

        this.getLootChests().forEach(lootChest -> {
            if (!this.cuboid.contains(lootChest.getBlockPos())) {
                ErrorHandler.error("Block position of the '" + lootChest.getId() + "' loot chest is outside of the dungeon protection.", lootChest.getConfig());
            }
        });

        if (this.broken) {
            this.plugin.error("Dungeon '" + this.getId() + "' has configuration problems and can't be used until fixed.");
        }
    }

    @NonNull
    public UnaryOperator<String> replacePlaceholders() {
        return Placeholders.DUNGEON_CONFIG.replacer(this);
    }

    public boolean isInProtection(@NonNull Entity entity) {
        return this.isInProtection(entity.getLocation());
    }

    public boolean isInProtection(@NonNull Block block) {
        return this.isInProtection(BlockPos.from(block));
    }

    public boolean isInProtection(@NonNull ExactPos exactPos) {
        return this.isInProtection(exactPos.toBlockPos());
    }

    public boolean isInProtection(@NonNull BlockPos blockPos) {
        return this.cuboid.contains(blockPos);
    }

    public boolean isInProtection(@NonNull Location location) {
        return this.cuboid.contains(location);
    }

    public boolean isWorld(@NonNull World world) {
        return this.worldName.equalsIgnoreCase(world.getName());
    }

    @NonNull
    public Set<DungeonPos> getDungeonPositions() {
        return this.cuboid.getIntersectingChunkPositions().stream().map(pos -> new DungeonPos(this.worldName, pos)).collect(Collectors.toSet());
    }




    @NonNull
    public DungeonInstance getInstance() {
        if (this.instance == null) throw new IllegalStateException("Dungeon instance is not set!");

        return this.instance;
    }

    public void setInstance(@NonNull DungeonInstance instance) {
        this.instance = instance;
    }

    @NonNull
    public Features features() {
        return this.features;
    }

    @NonNull
    public GameSettings gameSettings() {
        return this.gameSettings;
    }



    @NonNull
    public String getWorldName() {
        return this.worldName;
    }

    public void setWorldName(@NonNull String worldName) {
        this.worldName = worldName;
    }

    public Level getStartLevel() {
        return this.getLevelById(this.startLevelId);
    }

    public Stage getStartStage() {
        return this.getStageById(this.startStageId);
    }

    @NonNull
    public String getStartLevelId() {
        return this.startLevelId;
    }

    public void setStartLevelId(@NonNull String startLevelId) {
        this.startLevelId = startLevelId;
    }

    @NonNull
    public String getStartStageId() {
        return startStageId;
    }

    public void setStartStageId(@NonNull String startStageId) {
        this.startStageId = startStageId;
    }

    @NonNull
    public Cuboid getCuboid() {
        return this.cuboid;
    }

    public void setCuboid(@NonNull Cuboid cuboid) {
        this.cuboid = cuboid;
    }

    @NonNull
    public String getName() {
        return this.name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public List<String> getDescription() {
        return this.description;
    }

    public void setDescription(@NonNull List<String> description) {
        this.description = description;
    }

    @NonNull
    public String getPrefix() {
        return this.prefix;
    }

    public void setPrefix(@NonNull String prefix) {
        this.prefix = prefix;
    }

    @NonNull
    public NightItem getIcon() {
        return this.icon.copy();
    }

    public void setIcon(@NonNull NightItem icon) {
        this.icon = icon.copy().setHideComponents(true);
    }

    @NonNull
    public ExactPos getLobbyPos() {
        return this.lobbyPos;
    }

    public void setLobbyPos(@NonNull ExactPos lobbyPos) {
        this.lobbyPos = lobbyPos;
    }

    public void addSpawner(@NonNull DungeonMobSpawner spawner) {
        this.spawnerByIdMap.put(spawner.getId(), spawner);
    }

    @NonNull
    public Map<String, DungeonMobSpawner> getSpawnerByIdMap() {
        return this.spawnerByIdMap;
    }

    @NonNull
    public Set<DungeonMobSpawner> getSpawners() {
        return new HashSet<>(this.spawnerByIdMap.values());
    }

    @Nullable
    public DungeonMobSpawner getSpawnerById(@NonNull String id) {
        return this.spawnerByIdMap.get(id.toLowerCase());
    }

    public void addStage(@NonNull Stage stage) {
        this.stageByIdMap.put(stage.getId(), stage);
    }

    @NonNull
    public Map<String, Stage> getStageByIdMap() {
        return this.stageByIdMap;
    }

    @NonNull
    public Set<Stage> getStages() {
        return new HashSet<>(this.stageByIdMap.values());
    }

    @Nullable
    public Stage getStageById(@NonNull String id) {
        return this.stageByIdMap.get(id.toLowerCase());
    }

    @NonNull
    public Map<String, Level> getLevelByIdMap() {
        return this.levelByIdMap;
    }

    @NonNull
    public Set<Level> getLevels() {
        return new HashSet<>(this.levelByIdMap.values());
    }

    @Nullable
    public Level getLevelById(@NonNull String id) {
        return this.levelByIdMap.get(id.toLowerCase());
    }

    public void addLevel(@NonNull Level level) {
        this.levelByIdMap.put(level.getId(), level);
    }


    @NonNull
    public Map<String, Reward> getRewardByIdMap() {
        return this.rewardByIdMap;
    }

    @NonNull
    public Set<Reward> getRewards() {
        return new HashSet<>(this.rewardByIdMap.values());
    }

    @Nullable
    public Reward getRewardById(@NonNull String id) {
        return this.rewardByIdMap.get(id.toLowerCase());
    }

    public void addReward(@NonNull Reward reward) {
        this.rewardByIdMap.put(reward.getId(), reward);
    }

    public void removeReward(@NonNull String id) {
        this.rewardByIdMap.remove(id);
    }


    @NonNull
    public Map<String, LootChest> getLootChestByIdMap() {
        return this.lootChestByIdMap;
    }

    @NonNull
    public Set<LootChest> getLootChests() {
        return new HashSet<>(this.lootChestByIdMap.values());
    }

    @Nullable
    public LootChest getLootChestById(@NonNull String id) {
        return this.lootChestByIdMap.get(id.toLowerCase());
    }

    public void addLootChest(@NonNull LootChest lootChest) {
        this.lootChestByIdMap.put(lootChest.getId(), lootChest);
    }

    public void removeLootChest(@NonNull String id) {
        this.lootChestByIdMap.remove(id);
    }



    @NonNull
    public Map<String, Spot> getSpotByIdMap() {
        return this.spotByIdMap;
    }

    @NonNull
    public Set<Spot> getSpots() {
        return new HashSet<>(this.spotByIdMap.values());
    }

    @Nullable
    public Spot getSpotById(@NonNull String id) {
        return this.spotByIdMap.get(id.toLowerCase());
    }

    public void addSpot(@NonNull Spot spot) {
        this.spotByIdMap.put(spot.getId(), spot);
    }

    public void removeSpot(@NonNull String id) {
        this.spotByIdMap.remove(id);
    }
}

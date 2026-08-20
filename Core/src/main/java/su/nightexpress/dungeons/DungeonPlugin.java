package su.nightexpress.dungeons;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.dungeon.DungeonEntityBridge;
import su.nightexpress.dungeons.command.impl.BaseCommands;
import su.nightexpress.dungeons.command.impl.KitCommands;
import su.nightexpress.dungeons.command.impl.SetupCommands;
import su.nightexpress.dungeons.config.Config;
import su.nightexpress.dungeons.config.Keys;
import su.nightexpress.dungeons.config.Lang;
import su.nightexpress.dungeons.config.Perms;
import su.nightexpress.dungeons.data.DataHandler;
import su.nightexpress.dungeons.dungeon.DungeonManager;
import su.nightexpress.dungeons.dungeon.DungeonSetup;
import su.nightexpress.dungeons.dungeon.criteria.registry.CriteriaRegistry;
import su.nightexpress.dungeons.dungeon.scale.ScaleBaseRegistry;
import su.nightexpress.dungeons.dungeon.script.action.ActionRegistry;
import su.nightexpress.dungeons.dungeon.script.condition.ConditionRegistry;
import su.nightexpress.dungeons.dungeon.script.number.NumberComparators;
import su.nightexpress.dungeons.dungeon.script.task.TaskRegistry;
import su.nightexpress.dungeons.hook.HookId;
import su.nightexpress.dungeons.hook.impl.McMMOHook;
import su.nightexpress.dungeons.hook.impl.PlaceholderHook;
import su.nightexpress.dungeons.kit.KitManager;
import su.nightexpress.dungeons.nms.DungeonNMS;
import su.nightexpress.dungeons.nms.mc_1_21_11.MC_1_21_11;
import su.nightexpress.dungeons.registry.compat.BoardPluginRegistry;
import su.nightexpress.dungeons.registry.compat.GodPluginRegistry;
import su.nightexpress.dungeons.registry.level.LevelRegistry;
import su.nightexpress.dungeons.registry.mob.MobRegistry;
import su.nightexpress.dungeons.registry.pet.PetRegistry;
import su.nightexpress.dungeons.selection.SelectionManager;
import su.nightexpress.dungeons.user.UserManager;
import su.nightexpress.dungeons.nightcore.NightPlugin;
import su.nightexpress.dungeons.nightcore.commands.command.NightCommand;
import su.nightexpress.dungeons.nightcore.config.PluginDetails;
import su.nightexpress.dungeons.nightcore.util.Plugins;
import su.nightexpress.dungeons.nightcore.util.nbt.NbtBridge;

public class DungeonPlugin extends NightPlugin {

    private DataHandler dataHandler;
    private UserManager userManager;

    private SelectionManager selectionManager;
    private KitManager       kitManager;
    private DungeonManager   dungeonManager;
    private DungeonSetup dungeonSetup;

    private DungeonNMS internals;

    @Override
    @NonNull
    protected PluginDetails getDefaultDetails() {
        return PluginDetails.create("Dungeons", new String[]{"ada", "dungeon", "dungeons", "dungeonarena"})
            .setConfigClass(Config.class)
            .setPermissionsClass(Perms.class);
    }

    @Override
    protected void addRegistries() {
        this.registerLang(Lang.class);
    }

    @Override
    public void enable() {
        if (!this.loadInternals()) return;

        this.loadEngine();

        this.dataHandler = new DataHandler(this);
        this.dataHandler.setup();

        this.userManager = new UserManager(this, this.dataHandler);
        this.userManager.setup();

        this.selectionManager = new SelectionManager(this);
        this.selectionManager.setup();

        this.kitManager = new KitManager(this);
        this.kitManager.setup();

        this.dungeonManager = new DungeonManager(this);
        this.dungeonManager.setup();

        this.dungeonSetup = new DungeonSetup(this);
        this.dungeonSetup.setup();

        this.loadCommands();

        if (Plugins.hasPlaceholderAPI()) {
            PlaceholderHook.setup(this);
        }
        if (Plugins.isInstalled(HookId.MCMMO)) {
            McMMOHook.setup();
        }
    }

    @Override
    public void disable() {
        if (Plugins.hasPlaceholderAPI()) {
            PlaceholderHook.shutdown();
        }

        if (this.dungeonSetup != null) this.dungeonSetup.shutdown();
        if (this.dungeonManager != null) this.dungeonManager.shutdown();
        if (this.kitManager != null) this.kitManager.shutdown();
        if (this.selectionManager != null) this.selectionManager.shutdown();

        if (this.userManager != null) this.userManager.shutdown();
        if (this.dataHandler != null) this.dataHandler.shutdown();

        NumberComparators.clear();
        ConditionRegistry.clear();
        ActionRegistry.clear();
        TaskRegistry.clear();
        ScaleBaseRegistry.clear();
        MobRegistry.clear();
        LevelRegistry.clear();
        PetRegistry.clear();
        DungeonEntityBridge.clear();
        CriteriaRegistry.clear();
        GodPluginRegistry.clear();
        BoardPluginRegistry.clear();
        Keys.clear();
        DungeonsAPI.clear();
        NbtBridge.clear();
    }

    private boolean loadInternals() {
        // Single-version build: the module tree only contains MC_1_21_11, so there is nothing to switch on.
        MC_1_21_11 internals = new MC_1_21_11();
        this.internals = internals;
        NbtBridge.register(internals);

        return true;
    }

    private void loadEngine() {
        DungeonsAPI.load(this);
        Keys.load(this);
        GodPluginRegistry.load(this);
        BoardPluginRegistry.load(this);
        CriteriaRegistry.load(this);
        MobRegistry.load(this);
        LevelRegistry.load(this);
        PetRegistry.load(this);
        NumberComparators.load();
        ConditionRegistry.load();
        ActionRegistry.load();
        TaskRegistry.load();
        ScaleBaseRegistry.load();
    }

    private void loadCommands() {
        this.rootCommand = NightCommand.forPlugin(this, builder -> {
            BaseCommands.load(this, builder);
            SetupCommands.load(this, builder);
            KitCommands.load(this, builder);
        });
    }

    @NonNull
    public DataHandler getDataHandler() {
        return this.dataHandler;
    }

    @NonNull
    public UserManager getUserManager() {
        return this.userManager;
    }

    @NonNull
    public SelectionManager getSelectionManager() {
        return this.selectionManager;
    }

    @NonNull
    public DungeonManager getDungeonManager() {
        return this.dungeonManager;
    }

    @NonNull
    public DungeonSetup getDungeonSetup() {
        return this.dungeonSetup;
    }

    @NonNull
    public KitManager getKitManager() {
        return this.kitManager;
    }

    @NonNull
    public DungeonNMS getInternals() {
        return this.internals;
    }
}

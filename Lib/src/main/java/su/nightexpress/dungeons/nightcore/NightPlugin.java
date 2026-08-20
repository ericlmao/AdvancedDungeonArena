package su.nightexpress.dungeons.nightcore;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jspecify.annotations.NonNull;

import su.nightexpress.dungeons.nightcore.commands.CommandProvider;
import su.nightexpress.dungeons.nightcore.commands.command.NightCommand;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.config.PluginDetails;
import su.nightexpress.dungeons.nightcore.core.config.CoreLang;
import su.nightexpress.dungeons.nightcore.locale.LangContainer;
import su.nightexpress.dungeons.nightcore.locale.LangRegistry;
import su.nightexpress.dungeons.nightcore.ui.menu.MenuRegistry;
import su.nightexpress.dungeons.nightcore.util.FileUtil;
import su.nightexpress.dungeons.nightcore.util.LowerCase;
import su.nightexpress.dungeons.nightcore.util.Reflex;
import su.nightexpress.dungeons.nightcore.util.wrapper.UniPermission;

/**
 * Vendored replacement for {@code su.nightexpress.nightcore.NightPlugin}.
 * <p>
 * The upstream {@code NightCorePlugin} interface has been folded into this class, and every service that
 * used to be bootstrapped by the standalone NightCore plugin now lives in {@link CoreBootstrap}, which this
 * class drives from its own lifecycle.
 * <p>
 * Scheduling: upstream routed everything through {@code AdaptedScheduler} (Spigot/Folia). This vendored
 * version talks to {@link BukkitScheduler} directly - see the {@code runTask*} methods below, plus
 * {@code manager.AbstractManager#addTask} and {@code util.bukkit.NightTask}. Those are the only places the
 * plugin schedules anything, and they are the places a Folia migration has to touch.
 */
public abstract class NightPlugin extends JavaPlugin {

    public static final String CONFIG_FILE = "config.yml";
    public static final String ENGINE_FILE = "engine.yml";

    protected final List<CommandProvider> commandProviders;

    protected NightCommand   rootCommand;
    protected List<Runnable> postLoaders;

    protected LangRegistry langRegistry;

    protected FileConfig    engineConf;
    protected FileConfig    config;
    protected PluginDetails details;

    protected NightPlugin() {
        this.commandProviders = new ArrayList<>();
    }

    @Override
    public void onEnable() {
        if (!this.onInit()) {
            this.getPluginManager().disablePlugin(this);
            return;
        }

        long loadTook = System.currentTimeMillis();
        this.onStartup();
        this.loadManagers();
        this.info("Plugin loaded in " + (System.currentTimeMillis() - loadTook) + " ms!");
    }

    @Override
    public void onDisable() {
        this.unloadManagers();
        CoreBootstrap.shutdown();
        this.onShutdown();
    }

    protected boolean onInit() {
        CoreBootstrap.init(this);
        return true;
    }

    protected void onStartup() {

    }

    protected void onShutdown() {

    }

    public void reload() {
        this.unloadManagers();
        this.loadManagers();
    }

    @NonNull
    public String getPlaceholderAPIIdentifier() {
        return LowerCase.INTERNAL.apply(this.getName());
    }

    @NonNull
    public Path dataPath() {
        return this.getDataFolder().toPath();
    }

    @Override
    @NonNull
    public final FileConfig getConfig() {
        return this.config;
    }

    @NonNull
    public FileConfig getEngineConfig() {
        return this.engineConf;
    }

    @NonNull
    public PluginDetails getDetails() {
        if (this.details == null) throw new IllegalStateException("Plugin is not yet initialized!");

        return this.details;
    }

    @NonNull
    protected abstract PluginDetails getDefaultDetails();

    @NonNull
    public String getNameLocalized() {
        return this.getDetails().getName();
    }

    @NonNull
    public String getPrefix() {
        return this.getDetails().getPrefix();
    }

    @NonNull
    public String[] getCommandAliases() {
        return this.getDetails().getCommandAliases();
    }

    public void registerPermissions(@NonNull Class<?> clazz) {
        Reflex.getStaticFields(clazz, UniPermission.class, false).forEach(permission -> {
            if (this.getPluginManager().getPermission(permission.getName()) == null) {
                this.getPluginManager().addPermission(permission);
            }
        });
    }

    protected void setupRegistries() {
        this.langRegistry = new LangRegistry(this);
        this.langRegistry.setup();

        this.langRegistry.register(CoreLang.class);
        this.addRegistries();

        this.langRegistry.loadLocale();
    }

    protected void addRegistries() {

    }

    protected void setupConfig() {
        this.engineConf = FileConfig.loadOrExtract(this, ENGINE_FILE);
        this.config = FileConfig.loadOrExtract(this, CONFIG_FILE);

        // ---------- MIGRATION - START ----------
        if (this.config.contains("Plugin")) {
            PluginDetails details = PluginDetails.read(this, this.config, this.getDefaultDetails());
            details.write(this.engineConf, "");
            this.config.remove("Plugin");
        }
        // ---------- MIGRATION - END ----------

        this.details = PluginDetails.read(this, this.engineConf, this.getDefaultDetails());

        if (this.details.getConfigClass() != null) {
            this.config.initializeOptions(this.details.getConfigClass());
        }
    }

    protected void setupPermissions() {
        Class<?> clazz = this.details.getPermissionsClass();
        if (clazz == null) return;

        this.registerPermissions(clazz);
    }

    protected void loadManagers() {
        this.postLoaders = new ArrayList<>(); // Initialize a list for post-loading code.

        this.setupConfig();         // Load configuration.
        this.setupRegistries();     // Load registries so the plugin modules can access them.
        this.setupPermissions();    // Register plugin permissions.

        CoreBootstrap.enable(this); // Colour schemes, tag handlers, currencies, UI listener.

        this.enable();              // Load the plugin.

        this.postLoad();            // Load some stuff that needs to be injected after the rest managers.

        if (this.rootCommand != null) {
            this.rootCommand.register();
        }
        else {
            this.registerCommands();
        }

        this.config.saveChanges();
        if (this.langRegistry != null) this.langRegistry.complete();
        this.engineConf.saveChanges();
    }

    protected void unloadManagers() {
        Bukkit.getScheduler().cancelTasks(this); // Stop all plugin tasks.

        this.disable();

        MenuRegistry.closeAll();
        HandlerList.unregisterAll(this);        // Unregister all plugin listeners.

        if (this.rootCommand != null) {
            this.rootCommand.unregister();
            this.rootCommand = null;
        }
        if (this.langRegistry != null) this.langRegistry.shutdown();

        CoreBootstrap.disable();

        this.details = null; // Reset so it will use default ones on config read.
    }

    public abstract void enable();

    public abstract void disable();

    protected void postLoad() {
        this.postLoaders.forEach(Runnable::run);
        this.postLoaders.clear();
        this.postLoaders = null; // Prevent from adding post-load code during runtime.
    }

    public void onPostLoad(@NonNull Runnable runnable) {
        if (this.postLoaders == null) {
            throw new IllegalStateException("Can't inject post loading code during runtime.");
        }
        this.postLoaders.add(runnable);
    }

    public void doReload(@NonNull CommandSender sender) {
        this.reload();
        CoreLang.PLUGIN_RELOADED.withPrefix(this).send(sender);
    }

    private void registerCommands() {
        this.rootCommand = NightCommand.forPlugin(this, builder -> {
            this.commandProviders.forEach(provider -> provider.provideCommands(builder));
        });
        this.rootCommand.register();
    }

    public void addCommandProvider(@NonNull CommandProvider provider) {
        this.commandProviders.add(provider);
    }

    @NonNull
    public final LangRegistry getLangRegistry() {
        return this.langRegistry;
    }

    public void registerListener(@NonNull Listener listener) {
        this.getPluginManager().registerEvents(listener, this);
    }

    public void registerLang(@NonNull Class<? extends LangContainer> clazz) {
        this.langRegistry.register(clazz);
    }

    public void injectLang(@NonNull Class<? extends LangContainer> langClass) {
        this.langRegistry.inject(langClass);
    }

    public void injectLang(@NonNull LangContainer langContainer) {
        this.langRegistry.inject(langContainer);
    }

    public void extractResources(@NonNull String jarPath) {
        this.extractResources(jarPath, this.getDataFolder() + jarPath);
    }

    public void extractResources(@NonNull String jarPath, @NonNull String targetPath) {
        File destination = new File(targetPath);
        if (destination.exists()) return;

        if (jarPath.startsWith("/")) {
            jarPath = jarPath.substring(1);
        }
        if (jarPath.endsWith("/")) {
            jarPath = jarPath.substring(0, jarPath.length() - 1);
        }

        FileUtil.extractResources(this.getFile(), jarPath, destination);
    }

    public void info(@NonNull String msg) {
        this.getLogger().info(msg);
    }

    public void warn(@NonNull String msg) {
        this.getLogger().warning(msg);
    }

    public void error(@NonNull String msg) {
        this.getLogger().severe(msg);
    }

    public void warn(@NonNull String msg, @NonNull Throwable throwable) {
        this.getLogger().log(Level.WARNING, msg, throwable);
    }

    public void error(@NonNull String msg, @NonNull Throwable throwable) {
        this.getLogger().log(Level.SEVERE, msg, throwable);
    }

    public void debug(@NonNull String msg) {
        this.info("[DEBUG] " + msg);
    }

    @NonNull
    public PluginManager getPluginManager() {
        return this.getServer().getPluginManager();
    }

    @NonNull
    public BukkitScheduler getScheduler() {
        return this.getServer().getScheduler();
    }

    // ---------------------------------------------------------------------------------------------
    // Scheduling. Folia note: the entity/location/chunk overloads exist purely to preserve the
    // upstream call shape; on Paper they all land on the single main-thread scheduler.
    // ---------------------------------------------------------------------------------------------

    public void runTask(@NonNull Runnable runnable) {
        this.getScheduler().runTask(this, runnable);
    }

    public void runTask(@NonNull Entity entity, @NonNull Runnable runnable) {
        this.runTask(runnable);
    }

    public void runTask(@NonNull Location location, @NonNull Runnable runnable) {
        this.runTask(runnable);
    }

    public void runTask(@NonNull Chunk chunk, @NonNull Runnable runnable) {
        this.runTask(runnable);
    }

    public void runTaskAsync(@NonNull Runnable runnable) {
        this.getScheduler().runTaskAsynchronously(this, runnable);
    }

    public void runTaskLater(@NonNull Runnable runnable, long delay) {
        this.getScheduler().runTaskLater(this, runnable, delay);
    }

    public void runTaskLaterAsync(@NonNull Runnable runnable, long delay) {
        this.getScheduler().runTaskLaterAsynchronously(this, runnable, delay);
    }

    public void runTaskTimer(@NonNull Runnable runnable, long delay, long interval) {
        this.getScheduler().runTaskTimer(this, runnable, delay, interval);
    }

    public void runTaskTimerAsync(@NonNull Runnable runnable, long delay, long interval) {
        this.getScheduler().runTaskTimerAsynchronously(this, runnable, delay, interval);
    }
}

package su.nightexpress.dungeons.nightcore;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import gg.moonrise.scheduler.Scheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
 * version talks to Paper's region schedulers through the shaded {@link Scheduler} facade - see the
 * {@code runTask*} methods below, plus {@code manager.AbstractManager#addTask} and
 * {@code util.bukkit.NightTask}. Those are the only places the plugin schedules anything.
 * <p>
 * {@code BukkitScheduler} is deliberately not exposed any more: it is unimplemented on Folia, so a single
 * accessor would have been enough to reintroduce main-thread assumptions across the whole plugin.
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

    /**
     * Every repeating task this plugin owns. Folia has no {@code cancelTasks(plugin)} that reaches region
     * and entity schedulers, so repeating handles are tracked here and cancelled explicitly on disable -
     * otherwise a reload would leave the previous generation of timers running against dead state.
     */
    private final Set<ScheduledTask> trackedTasks = ConcurrentHashMap.newKeySet();

    protected NightPlugin() {
        this.commandProviders = new ArrayList<>();
    }

    @Override
    public void onEnable() {
        // Must be the very first thing that happens: every Scheduler entrypoint throws
        // IllegalStateException until init() has run, and onInit() already schedules.
        Scheduler.init(this);

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
        this.cancelTrackedTasks(); // Stop all plugin tasks. Folia has no cancelTasks(plugin) equivalent.

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

    // ---------------------------------------------------------------------------------------------
    // Scheduling.
    //
    // Every one of these delegates to folia-scheduler, which talks to Paper's four region schedulers
    // directly. Those exist on regular Paper since 1.20.1 as plain main-thread delegates, so this is a
    // single code path for both Paper and Folia - there is no runtime platform check anywhere.
    //
    // Picking the right overload is not cosmetic on Folia:
    //   runTask(Runnable)          -> global region. Plugin-wide state ONLY; no world/entity access.
    //   runTask(Entity, Runnable)  -> that entity's scheduler. Messages, inventories, teleports, removal.
    //   runTask(Location|Chunk, .) -> the owning region. Blocks, chunk tickets, world spawns.
    //   runTaskAsync(Runnable)     -> async pool. Disk/DB/network/CPU; no Bukkit access at all.
    // ---------------------------------------------------------------------------------------------

    public void runTask(@NonNull Runnable runnable) {
        Scheduler.sync().run(task -> runnable.run());
    }

    public void runTask(@NonNull Entity entity, @NonNull Runnable runnable) {
        Scheduler.entity(entity).run(task -> runnable.run());
    }

    /**
     * Entity-bound task with a retired callback.
     *
     * @param retired runs instead of {@code runnable} when the entity's scheduler is retired (entity
     *                removed, player disconnected) and the task can therefore never fire. Use it wherever a
     *                missed run would leave state inconsistent.
     */
    public void runTask(@NonNull Entity entity, @NonNull Runnable runnable, @NonNull Runnable retired) {
        // Paper only invokes the retired callback for a task that was accepted and then orphaned. If the
        // scheduler is *already* retired at submission time it returns null and drops the task on the floor
        // without telling anyone - which is precisely the case a retired callback exists to cover.
        if (Scheduler.entity(entity).run(task -> runnable.run(), retired) == null) {
            retired.run();
        }
    }

    public void runTask(@NonNull Location location, @NonNull Runnable runnable) {
        Scheduler.location().run(location, task -> runnable.run());
    }

    public void runTask(@NonNull Chunk chunk, @NonNull Runnable runnable) {
        Scheduler.location().run(chunk, task -> runnable.run());
    }

    public void runTaskAsync(@NonNull Runnable runnable) {
        Scheduler.async().run(task -> runnable.run());
    }

    public void runTaskLater(@NonNull Runnable runnable, long delay) {
        Scheduler.sync().runDelayed(task -> runnable.run(), Math.max(1L, delay));
    }

    public void runTaskLater(@NonNull Entity entity, @NonNull Runnable runnable, long delay) {
        Scheduler.entity(entity).runDelayed(task -> runnable.run(), Math.max(1L, delay));
    }

    public void runTaskLater(@NonNull Location location, @NonNull Runnable runnable, long delay) {
        Scheduler.location().runDelayed(location, Math.max(1L, delay), task -> runnable.run());
    }

    /** @param delay delay in <b>ticks</b>, converted to wall time at 50 ms/tick for the async scheduler. */
    public void runTaskLaterAsync(@NonNull Runnable runnable, long delay) {
        Scheduler.async().runDelayed(task -> runnable.run(), Math.max(1L, delay) * 50L, TimeUnit.MILLISECONDS);
    }

    public void runTaskTimer(@NonNull Runnable runnable, long delay, long interval) {
        this.trackTask(Scheduler.sync().schedule(task -> runnable.run(), Math.max(1L, delay), interval));
    }

    public void runTaskTimer(@NonNull Location location, @NonNull Runnable runnable, long delay, long interval) {
        this.trackTask(Scheduler.location().schedule(location, Math.max(1L, delay), interval, task -> runnable.run()));
    }

    public void runTaskTimer(@NonNull Entity entity, @NonNull Runnable runnable, long delay, long interval) {
        this.trackTask(Scheduler.entity(entity).schedule(task -> runnable.run(), Math.max(1L, delay), interval));
    }

    /** @param delay and {@code interval} in <b>ticks</b>, converted to wall time at 50 ms/tick. */
    public void runTaskTimerAsync(@NonNull Runnable runnable, long delay, long interval) {
        this.trackTask(Scheduler.async().schedule(task -> runnable.run(),
            Math.max(1L, delay) * 50L, Math.max(1L, interval) * 50L, TimeUnit.MILLISECONDS));
    }

    /**
     * Registers a repeating task handle so that {@link #unloadManagers()} can cancel it.
     * <p>
     * Only repeating tasks need this. One-shot tasks are already dropped by the server when the plugin is
     * disabled, and tracking them would mean an unbounded set of handles that nothing ever removes.
     */
    public void trackTask(@Nullable ScheduledTask task) {
        if (task != null) {
            this.trackedTasks.add(task);
        }
    }

    protected void cancelTrackedTasks() {
        this.trackedTasks.forEach(ScheduledTask::cancel);
        this.trackedTasks.clear();
    }
}

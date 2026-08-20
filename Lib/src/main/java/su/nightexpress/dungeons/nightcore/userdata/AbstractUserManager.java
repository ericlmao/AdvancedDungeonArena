package su.nightexpress.dungeons.nightcore.userdata;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.nightcore.NightPlugin;
import su.nightexpress.dungeons.nightcore.manager.AbstractManager;
import su.nightexpress.dungeons.nightcore.util.Players;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractUserManager<P extends NightPlugin, U extends AbstractUser> extends AbstractManager<P> {

    protected final UserdataConfig                config;
    protected final UserDataStore<U> dataManager;

    private final Map<UUID, U>   loadedByIdMap;
    private final Map<String, U> loadedByNameMap;

    /**
     * In-flight database reads, keyed by player id.
     * <p>
     * Without this, a player whose data is not resident can have several fetches racing for the same row -
     * one per menu render, one per join check - and the losers overwrite the winner's cache entry with a
     * second, distinct user object. Callers share the first future instead.
     */
    private final Map<UUID, CompletableFuture<U>> pendingFetches;

    /**
     * Carries the blocking JDBC reads.
     * <p>
     * Virtual threads rather than the async scheduler: these tasks are pure block-on-socket, they can be
     * numerous (one per joining player during a restart storm), and parking a virtual thread costs nothing
     * where occupying a scheduler worker would starve every other async task the plugin owns.
     */
    private final ExecutorService fetchExecutor;

    public AbstractUserManager(@NonNull P plugin, @NonNull UserDataStore<U> dataManager) {
        super(plugin);
        this.config = UserdataConfig.read(plugin);
        this.dataManager = dataManager;
        this.loadedByIdMap = new ConcurrentHashMap<>();
        this.loadedByNameMap = new ConcurrentHashMap<>();
        this.pendingFetches = new ConcurrentHashMap<>();
        this.fetchExecutor = Executors.newThreadPerTaskExecutor(
            Thread.ofVirtual().name(plugin.getName() + "-userdata-", 0).factory());
    }

    @Override
    protected void onLoad() {
        this.addListener(new UserListener<>(this.plugin, this));
        this.addAsyncTask(this::saveScheduled, this.config.getSaveInterval());
        this.addAsyncTask(this::unloadExpired, this.config.getCacheCleanupInterval());
        if (this.dataManager.getSyncInterval() > 0) {
            this.addAsyncTask(this.dataManager::onSynchronize, this.dataManager.getSyncInterval());
        }

        this.plugin.onPostLoad(this::loadOnline);
    }

    @Override
    protected void onShutdown() {
        this.fetchExecutor.shutdownNow();
        this.pendingFetches.clear();
        this.saveLoaded();
        this.loadedByIdMap.clear();
        this.loadedByNameMap.clear();
    }

    protected void onLoad(@NonNull U user) {

    }

    protected void onUnload(@NonNull U user) {

    }

    /**
     * Warms the cache for everyone already connected, off the server threads.
     * <p>
     * This runs at post-load, which on a {@code /reload} or a hot plugin enable happens while players are
     * online and their regions are ticking. Reading their rows inline would stall every one of those
     * regions for the duration of the query.
     */
    public void loadOnline() {
        Players.getOnline().forEach(player -> this.fetchAsync(player.getUniqueId()).thenAccept(user -> {
            if (user != null) this.cachePermanent(user);
        }));
    }

    public void unloadExpired() {
        this.getLoaded().forEach(user -> {
            if (user.isCacheExpired() && !user.isOnline()) {
                this.unload(user);
            }
        });
    }

    public final void handleJoin(@NonNull Player player) {
        U user = this.getLoaded(player);
        if (user == null) {
            // The pre-login fetch is what normally puts the user here. It can be missed - a login that
            // raced a plugin reload, a database blip - and every later read would then find nothing and
            // fall back to empty data. Close the window off-thread rather than blocking the join.
            String name = player.getName();
            this.fetchAsync(player.getUniqueId()).thenAccept(fetched -> {
                if (fetched == null || !player.isOnline()) return;

                fetched.setName(name);
                this.cachePermanent(fetched);
            });
            return;
        }

        user.setName(player.getName()); // Update name

        this.cachePermanent(user);
    }

    public final void handleQuit(@NonNull Player player) {
        U user = this.getLoaded(player);
        if (user == null) return;

        user.setLastOnline(System.currentTimeMillis());

        // Force save data on quit + disable auto-save and delay synchronization.
        if (user.isAutoSavePlanned()) {
            this.plugin.runTaskAsync(() -> this.saveScheduled(Collections.singletonList(user)));
        }
        else {
            this.plugin.runTaskAsync(() -> this.dataManager.saveUserCommons(user));
        }

        this.cacheTemporary(user);
    }

    public void saveScheduled() {
        this.saveScheduled(this.getLoaded().stream().filter(AbstractUser::isAutoSaveReady).toList());
    }

    private void saveScheduled(@NonNull Collection<U> users) {
        if (users.isEmpty()) return;

        this.dataManager.saveUsersFully(users);

        users.forEach(user -> {
            user.disableAutoSave(); // Reset autosave timestamp.
            user.setAutoSyncIn(this.config.getSaveSyncPause()); // Unlock synchronization only when all data was pushed to the database.
        });
    }

    public void saveLoaded() {
        this.dataManager.saveUsersCommons(this.getLoaded());
    }

    public void save(@NonNull Player player) {
        U user = this.getLoaded(player.getUniqueId());
        if (user == null) return;

        this.save(user);
    }

    public void save(@NonNull U user) {
        user.setAutoSaveIn(this.config.getSaveDelay());
        user.disableAutoSync();
    }

    private void load(@NonNull U user) {
        user.onLoad();
        this.cacheTemporary(user);
        this.onLoad(user);
    }

    private void unload(@NonNull U user) {
        user.onUnload();
        this.onUnload(user);

        this.loadedByIdMap.remove(user.getId());
        this.loadedByNameMap.remove(user.getName().toLowerCase());
    }

    @NonNull
    public abstract U create(@NonNull UUID uuid, @NonNull String name);

    public void cacheTemporary(@NonNull U user) {
        user.setCacheFor(this.config.getCacheLifetime());
        this.cache(user);
    }

    public void cachePermanent(@NonNull U user) {
        user.setPermanentCache();
        this.cache(user);
    }

    private void cache(@NonNull U user) {
        this.loadedByIdMap.putIfAbsent(user.getId(), user);
        this.loadedByNameMap.putIfAbsent(user.getName().toLowerCase(), user);
    }

    public boolean isInDatabase(@NonNull String name) {
        return this.dataManager.isUserExists(name);
    }

    public boolean isInDatabase(@NonNull UUID uuid) {
        return this.dataManager.isUserExists(uuid);
    }

    public void addInDatabase(@NonNull U user) {
        this.dataManager.insertUser(user);
    }

    public void saveInDatabase(@NonNull U user) {
        this.dataManager.saveUserFully(user);
    }

    @Nullable
    public U getFromDatabase(@NonNull String name) {
        return this.dataManager.getUser(name);
    }

    @Nullable
    public U getFromDatabase(@NonNull UUID uuid) {
        return this.dataManager.getUser(uuid);
    }

    /**
     * Resident user data for an online player, without ever touching the database.
     * <p>
     * Callers are render and decision paths - menu fillers, join checks - that run on the region thread
     * owning the player and have no continuation to hand a result to. A blocking read here freezes that
     * region, and with it every other player in it, for a full query round trip.
     * <p>
     * Data is resident for the whole of a session: the pre-login listener fetches it before the player is
     * spawned, {@link #handleJoin(Player)} pins it, and {@link #unloadExpired()} never evicts an online
     * player. A miss therefore means something already went wrong upstream, and the recovery is to warm
     * the cache off-thread and let this call proceed on a blank user rather than stall the region. Route
     * anything that must see real data through {@link #ensureLoaded(Player, Runnable)} first.
     */
    @NonNull
    public final U getOrFetch(@NonNull Player player) {
        UUID uuid = player.getUniqueId();

        U user = this.getLoaded(uuid);
        if (user != null) return user;

        if (player.isOnline()) {
            this.plugin.warn("User data for '" + uuid + "' aka '" + player.getName()
                + "' was not resident; serving blank data and reloading it in the background.");
            this.fetchAsync(uuid).thenAccept(fetched -> {
                if (fetched != null && player.isOnline()) this.cachePermanent(fetched);
            });
        }

        return this.create(uuid, player.getName());
    }

    /**
     * Runs {@code action} with this player's data guaranteed resident.
     * <p>
     * Runs inline when the data is already cached, which is the normal case and keeps the action on the
     * caller's tick. Otherwise the read happens on a virtual thread and the action is handed back to the
     * player's own scheduler, so it stays legal to touch the player from it. The action is dropped if the
     * player leaves before the read finishes.
     */
    public final void ensureLoaded(@NonNull Player player, @NonNull Runnable action) {
        if (this.isLoaded(player)) {
            action.run();
            return;
        }

        this.fetchAsync(player.getUniqueId()).thenAccept(user -> {
            if (user != null) this.cachePermanent(user);
            if (player.isOnline()) this.plugin.runTask(player, action, () -> {});
        });
    }

    /**
     * Reads a user off the server threads, sharing one database round trip between concurrent callers.
     * <p>
     * The result is <b>not</b> cached by this method - {@link #getOrFetch(UUID)} caches only what it loads
     * itself, and a fetch whose result is discarded should not resurrect a user the caller never asked to
     * keep. Callers that want residency say so explicitly.
     */
    @NonNull
    public final CompletableFuture<U> fetchAsync(@NonNull UUID uuid) {
        U loaded = this.getLoaded(uuid);
        if (loaded != null) return CompletableFuture.completedFuture(loaded);
        if (this.fetchExecutor.isShutdown()) return CompletableFuture.completedFuture(null);

        CompletableFuture<U> future = this.pendingFetches.computeIfAbsent(uuid, id ->
            CompletableFuture.supplyAsync(() -> this.getFromDatabase(id), this.fetchExecutor));

        // Registered after the put, never from inside the mapping function: a fetch that finishes before
        // computeIfAbsent returns would otherwise clean up an entry that is not in the map yet, and the
        // real entry would then be stuck there for the rest of the session.
        future.whenComplete((user, error) -> this.pendingFetches.remove(uuid, future));

        return future;
    }

    /** Blocking. Call from a virtual thread or the async scheduler only - never from a region thread. */
    @Nullable
    public final U getOrFetch(@NonNull String name) {
        U user = this.getLoaded(name);
        if (user != null) return user;

        user = this.getFromDatabase(name);
        if (user != null) {
            this.load(user);
        }

        return user;
    }

    /** Blocking. Call from a virtual thread or the async scheduler only - never from a region thread. */
    @Nullable
    public final U getOrFetch(@NonNull UUID uuid) {
        U user = this.getLoaded(uuid);
        if (user != null) return user;

        user = this.getFromDatabase(uuid);
        if (user != null) {
            this.load(user);
        }

        return user;
    }

    public final CompletableFuture<U> getUserDataAsync(@NonNull String name) {
        return CompletableFuture.supplyAsync(() -> this.getOrFetch(name), this.fetchExecutor);
    }

    public final CompletableFuture<U> getUserDataAsync(@NonNull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> this.getOrFetch(uuid), this.fetchExecutor);
    }

    /**
     * Performs an operation on the given user.<br>
     * Runs immediately in the current thread if player is online or data is already loaded.<br>
     * Otherwise fetches player data asynchronously and performs an operation in async CompletableFuture thread.
     * 
     * @param name Name of a player.
     */
    public void manageUser(@NonNull String name, Consumer<U> consumer) {
        this.manageUser(() -> this.getLoaded(name), () -> this.getUserDataAsync(name), consumer);
    }

    /**
     * Performs an operation on the given user.<br>
     * Runs immediately in the current thread if player is online or data is already loaded.<br>
     * Otherwise fetches player data asynchronously and performs an operation in async CompletableFuture thread.
     * 
     * @param playerId UUID of a player.
     */
    public void manageUser(@NonNull UUID playerId, Consumer<U> consumer) {
        this.manageUser(() -> this.getLoaded(playerId), () -> this.getUserDataAsync(playerId), consumer);
    }

    /**
     * Performs an operation on the given user.<br>
     * Runs immediately in the current thread if player is online or data is already loaded.<br>
     * Otherwise fetches player data asynchronously and performs an operation next tick in the main thread.
     * 
     * @param name Name of a player.
     */
    public void manageUserSynchronized(@NonNull String name, Consumer<U> consumer) {
        this.manageUserSynchronized(() -> this.getLoaded(name), () -> this.getUserDataAsync(name), consumer);
    }

    /**
     * Performs an operation on the given user.<br>
     * Runs immediately in the current thread if player is online or data is already loaded.<br>
     * Otherwise fetches player data asynchronously and performs an operation next tick in the main thread.
     * 
     * @param playerId UUID of a player.
     */
    public void manageUserSynchronized(@NonNull UUID playerId, Consumer<U> consumer) {
        this.manageUserSynchronized(() -> this.getLoaded(playerId), () -> this.getUserDataAsync(playerId), consumer);
    }

    private void manageUserSynchronized(@NonNull Supplier<U> loadedSupplier,
                                        @NonNull Supplier<CompletableFuture<U>> fetchSupplier,
                                        @NonNull Consumer<U> consumer) {
        this.manageUser(loadedSupplier, fetchSupplier, user -> this.plugin.runTask(() -> consumer.accept(user)));
    }

    public void manageUser(@NonNull Player player, Consumer<U> consumer) {
        this.manageUser(player, consumer, false);
    }

    public void manageUserWithSave(@NonNull Player player, Consumer<U> consumer) {
        this.manageUser(player, consumer, true);
    }

    private void manageUser(@NonNull Player player, Consumer<U> consumer, boolean save) {
        U user = this.getLoaded(player);
        if (user == null) return;

        consumer.accept(user);
        if (save) this.save(user);
    }

    private void manageUser(@NonNull Supplier<U> loadedSupplier, @NonNull Supplier<CompletableFuture<U>> fetchSupplier,
                            @NonNull Consumer<U> consumer) {
        U user = loadedSupplier.get();
        if (user != null) {
            consumer.accept(user);
        }
        else fetchSupplier.get().thenAccept(consumer);
    }

    /**
     * @return Fetches all users from the database and returns them in as a single set.<br>
     *         Users that are already loaded won't be replaced by ones from the database, however they will be fetched
     *         anyway.
     */
    @NonNull
    public Set<U> getAll() {
        Set<U> users = this.getLoaded();
        this.dataManager.getUsers().stream().filter(user -> !this.isLoaded(user.getId())).forEach(users::add);
        return users;
    }

    @NonNull
    public Set<U> getLoaded() {
        return new HashSet<>(this.loadedByIdMap.values());
    }

    @NonNull
    public Map<UUID, U> getLoadedByIdMap() {
        return this.loadedByIdMap;
    }

    @NonNull
    public Map<String, U> getLoadedByNameMap() {
        return this.loadedByNameMap;
    }

    @Nullable
    public U getLoaded(@NonNull Player player) {
        return this.getLoaded(player.getUniqueId());
    }

    @Nullable
    public U getLoaded(@NonNull UUID uuid) {
        return this.loadedByIdMap.get(uuid);
    }

    @Nullable
    public U getLoaded(@NonNull String name) {
        U byName = this.loadedByNameMap.get(name.toLowerCase());
        if (byName != null) return byName;

        Player player = Players.getPlayer(name);
        return player == null ? null : this.getLoaded(player.getUniqueId());
    }

    public boolean isLoaded(@NonNull Player player) {
        return this.isLoaded(player.getUniqueId());
    }

    public boolean isLoaded(@NonNull UUID id) {
        return this.loadedByIdMap.containsKey(id);
    }

    public boolean isLoaded(@NonNull String name) {
        return this.getLoaded(name) != null;
    }
}

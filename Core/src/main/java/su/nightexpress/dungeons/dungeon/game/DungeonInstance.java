package su.nightexpress.dungeons.dungeon.game;

import gg.moonrise.scheduler.Scheduler;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.Placeholders;
import su.nightexpress.dungeons.api.criteria.CriterionMob;
import su.nightexpress.dungeons.api.dungeon.*;
import su.nightexpress.dungeons.api.mob.MobIdentifier;
import su.nightexpress.dungeons.api.mob.MobProvider;
import su.nightexpress.dungeons.api.type.GameResult;
import su.nightexpress.dungeons.api.type.GameState;
import su.nightexpress.dungeons.api.type.MobFaction;
import su.nightexpress.dungeons.config.Config;
import su.nightexpress.dungeons.config.Lang;
import su.nightexpress.dungeons.config.Perms;
import su.nightexpress.dungeons.dungeon.config.DungeonConfig;
import su.nightexpress.dungeons.dungeon.criteria.registry.mob.MobCriterias;
import su.nightexpress.dungeons.dungeon.event.DungeonEventReceiver;
import su.nightexpress.dungeons.dungeon.event.game.*;
import su.nightexpress.dungeons.dungeon.event.normal.DungeonEndEvent;
import su.nightexpress.dungeons.dungeon.event.normal.DungeonStartedEvent;
import su.nightexpress.dungeons.dungeon.feature.KillStreak;
import su.nightexpress.dungeons.dungeon.feature.LevelRequirement;
import su.nightexpress.dungeons.dungeon.feature.itemfilter.ItemFilterCriteria;
import su.nightexpress.dungeons.dungeon.feature.itemfilter.ItemFilterMode;
import su.nightexpress.dungeons.dungeon.level.Level;
import su.nightexpress.dungeons.dungeon.lootchest.LootChest;
import su.nightexpress.dungeons.dungeon.mob.DungeonMob;
import su.nightexpress.dungeons.dungeon.player.DungeonGamer;
import su.nightexpress.dungeons.dungeon.player.PlayerSnapshot;
import su.nightexpress.dungeons.dungeon.reward.GameReward;
import su.nightexpress.dungeons.dungeon.reward.Reward;
import su.nightexpress.dungeons.dungeon.spot.Spot;
import su.nightexpress.dungeons.dungeon.spot.SpotState;
import su.nightexpress.dungeons.dungeon.stage.Stage;
import su.nightexpress.dungeons.dungeon.stage.StageTask;
import su.nightexpress.dungeons.dungeon.stage.task.TaskProgress;
import su.nightexpress.dungeons.dungeon.stats.DungeonStats;
import su.nightexpress.dungeons.kit.KitUtils;
import su.nightexpress.dungeons.kit.impl.Kit;
import su.nightexpress.dungeons.registry.mob.MobRegistry;
import su.nightexpress.dungeons.user.DungeonUser;
import su.nightexpress.dungeons.util.DungeonUtils;
import su.nightexpress.dungeons.util.ErrorHandler;
import su.nightexpress.dungeons.util.MobUitls;
import su.nightexpress.dungeons.nightcore.integration.currency.EconomyBridge;
import su.nightexpress.dungeons.nightcore.locale.entry.MessageLocale;
import su.nightexpress.dungeons.nightcore.locale.message.LangMessage;
import su.nightexpress.dungeons.nightcore.util.EntityUtil;
import su.nightexpress.dungeons.nightcore.util.ItemUtil;
import su.nightexpress.dungeons.nightcore.util.Players;
import su.nightexpress.dungeons.nightcore.util.geodata.pos.BlockPos;
import su.nightexpress.dungeons.nightcore.util.placeholder.Replacer;
import su.nightexpress.dungeons.nightcore.util.random.Rnd;
import su.nightexpress.dungeons.nightcore.util.time.TimeFormatType;
import su.nightexpress.dungeons.nightcore.util.time.TimeFormats;
import su.nightexpress.dungeons.nightcore.util.wrapper.UniParticle;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class DungeonInstance implements Dungeon {

    private final DungeonPlugin    plugin;
    private final DungeonConfig    config;
    private final DungeonStats     stats;
    private final DungeonVariables variables;

    private final List<DungeonEventReceiver>   eventReceivers;
    private final Map<StageTask, TaskProgress> taskProgress;
    private final Map<UUID, DungeonGamer>      players;
    private final Map<UUID, DungeonMob>        mobByIdMap;
    private final Set<Item>                    groundItems;

    private final String prefix;

    // The instance clock runs on the global region scheduler, but these fields are read from player and mob
    // schedulers (gamer ticks, board renders), from listener threads, and from PAPI's caller thread.
    // They are single-writer, so volatile is enough - no lock is needed anywhere.
    private volatile World world;
    private long  tickCount;

    private volatile GameState  state;
    private volatile GameResult gameResult;
    private volatile int  countdown;
    private volatile long timeLeft;

    private volatile Level level;
    private volatile Stage stage;
    private boolean stageCompleted;

    public DungeonInstance(@NonNull DungeonPlugin plugin, @NonNull DungeonConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.stats = new DungeonStats(this);
        this.variables = new DungeonVariables();
        // Concurrency note: the tick loop owns these, but listeners (mob death, item spawn, player quit) and
        // the async chat handler all mutate them from whatever thread their event arrived on. On Paper that
        // was one thread and the races were invisible; on Folia they are routine. Hence concurrent
        // collections throughout - and synchronizedMap rather than ConcurrentHashMap for taskProgress,
        // because the task order is load-bearing.
        this.eventReceivers = new CopyOnWriteArrayList<>(); // List to keep receivers order.
        this.taskProgress = Collections.synchronizedMap(new LinkedHashMap<>()); // Linked to keep tasks order.
        this.players = new ConcurrentHashMap<>();
        this.mobByIdMap = new ConcurrentHashMap<>();
        this.groundItems = ConcurrentHashMap.newKeySet();

        this.reset();

        this.prefix = this.replacePlaceholders().apply(config.getPrefix());
    }

    @NonNull
    public UnaryOperator<String> replacePlaceholders() {
        return str -> Placeholders.DUNGEON_INSTANCE.replacer(this).apply(this.replaceVariables().apply(str));
    }

    @NonNull
    public UnaryOperator<String> replaceVariables() {
        return this.variables.replacePlaceholders();
    }

    private void reset() {
        this.killGroundItems();
        this.killMobs();
        this.removeTasks();
        this.resetSpotStates();
        this.clearLootChests();

        this.stats.clear();
        this.variables.clear();

        this.taskProgress.clear();
        this.players.clear();
        this.mobByIdMap.clear();
        this.tickCount = 0L;
        this.countdown = this.config.gameSettings().getLobbyTime();
        this.setTimeLeft(0L);

        this.level = null;
        this.stage = null;
        this.stageCompleted = false;
        this.state = GameState.WAITING;
        this.eventReceivers.clear();
        this.gameResult = null;
    }

    public void stop() {
        if (this.state == GameState.INGAME) {
            DungeonEndEvent event = new DungeonEndEvent(this, this.gameResult);
            this.plugin.getPluginManager().callEvent(event);

            if (this.config.gameSettings().isEndAnnouncement()) {
                Lang.DUNGEON_ANNOUNCE_END.message().broadcast(replacer -> replacer.replace(this.replacePlaceholders()));
            }

            this.unholdChunks();
        }

        this.getPlayers().forEach(this::leavePlayer);

        this.reset();
    }

    public boolean activate() {
        World world = Bukkit.getWorld(this.config.getWorldName());
        if (world == null) return false;

        this.activate(world);
        return true;
    }

    public void activate(@NonNull World world) {
        if (this.config.getWorldName().equalsIgnoreCase(world.getName())) {
            this.world = world;
            this.plugin.debug("Dungeon " + this.getId() + " activated.");
        }
    }

    public void deactivate(@NonNull World world) {
        if (this.config.getWorldName().equalsIgnoreCase(world.getName())) {
            this.deactivate();
        }
    }

    public void deactivate() {
        this.gameResult = GameResult.NONE;
        this.stop();
        this.world = null;
        this.plugin.debug("Dungeon " + this.getId() + " deactivated.");
    }

    @Override
    @NonNull
    public World getWorld() {
        if (this.world == null) throw new IllegalStateException("Dungeon world is not loaded! You must check Dungeon#isActive before calling this method.");

        return this.world;
    }

    public boolean isActive() {
        return this.world != null && !this.config.isBroken();
    }

    @NonNull
    public DungeonPlugin getPlugin() {
        return this.plugin;
    }

    private void updateListeners() {
        this.eventReceivers.clear();
        if (this.level != null) {
            this.eventReceivers.add(this.level);
        }
        if (this.stage != null) {
            this.eventReceivers.add(this.stage);
        }
    }

    public void tick() {
        if (!this.isActive()) return;

        if (this.state == GameState.INGAME) {
            this.tickGame();

            if (this.state != GameState.INGAME) return;

            DungeonGameEvent tickEvent = new DungeonTickEvent(this);
            this.broadcastEvent(tickEvent);

            this.tickCount++;
        }
        else {
            this.tickLobby();
        }

        // Per-player work leaves the instance clock here. DungeonGamer#tick reapplies kit potion effects and
        // re-renders the scoreboard, both of which are entity mutations that must happen on the thread that
        // owns that player - which on Folia is very often not the thread running this method.
        this.getPlayers().forEach(gamer -> this.plugin.runTask(gamer.getPlayer(), gamer::tick));
        this.showStatus();
    }

    public void tickLobby() {
        boolean readyToStart = this.isReadyToStart();

        if (this.state == GameState.WAITING) {
            if (readyToStart) {
                this.state = GameState.READY;

                if (this.config.gameSettings().isStartAnnouncement()) {
                    Players.getOnline().forEach(player -> {
                        if (this.plugin.getDungeonManager().isPlaying(player)) return;
                        if (!this.hasPermission(player)) return;

                        Lang.DUNGEON_ANNOUNCE_START.message().send(player, replacer -> replacer
                            .replace(this.replacePlaceholders())
                            .replace(Placeholders.GENERIC_TIME, this.countdown));
                    });
                }
            }

            return;
        }

        if (!readyToStart) {
            this.state = GameState.WAITING;
            this.setCountdown(this.config.gameSettings().getLobbyTime());
            // this.updateSigns();
            return;
        }

        Set<DungeonGamer> players = this.getPlayers();
        boolean allReady = players.stream().allMatch(DungeonPlayer::isReady);

        // Drop countdown timer to a specific value when all players are ready to fight.
        int dropTo = Config.DUNGEON_LOBBY_DROP_TIMER.get();
        if (dropTo > 0 && this.countdown > dropTo && allReady) {
            this.setCountdown(dropTo);
        }

        if (this.countdown <= 0) {
            this.holdChunks();

            this.state = GameState.INGAME;
            this.setLevel(this.config.getStartLevel());
            this.setStage(this.config.getStartStage());
            players.forEach(this::spawnPlayer);
            this.countdown = -1;
            this.setTimeLeft(this.config.gameSettings().hasTimeleft() ? this.config.gameSettings().getTimeleft() * 60L : -1L);

            DungeonStartedEvent event = new DungeonStartedEvent(this);
            this.plugin.getPluginManager().callEvent(event);
            return;
        }

        this.countdown--;
    }

    public void tickGame() {
        if (!this.config.gameSettings().isItemPickupAllowed()) {
            this.burnGroundItems();
        }

        this.eliminateDeadMobs();

        if (this.isAboutToEnd()) {
            if (this.countdown-- <= 0) {
                this.stop();
            }
            return;
        }

        if (!this.hasAlivePlayers()) {
            long lastDeathTime = this.getDeadPlayers().stream().mapToLong(DungeonGamer::getDeathTime).max().orElse(0L);
            if (System.currentTimeMillis() - lastDeathTime > Config.DUNGEON_TIME_TO_REVIVE.get() * 1000L) {
                this.setCountdown(Config.DUNGEON_COUNTDOWN_DEFEAT.get(), GameResult.DEFEAT);
                this.broadcast(Lang.DUNGEON_END_ALL_DEAD, replacer -> replacer.replace(this.replacePlaceholders()));
            }
            return;
        }

        if (this.timeLeft > 0) {
            if (--this.timeLeft == 0) {
                this.setCountdown(Config.DUNGEON_COUNTDOWN_DEFEAT.get(), GameResult.DEFEAT);
                this.broadcast(Lang.DUNGEON_END_TIMEOUT, replacer -> replacer.replace(this.replacePlaceholders()));
                return;
            }
        }

        if (this.isTasksCompleted() && !this.stageCompleted) {
            this.handleStageEnd();
        }
    }

    // Chunk tickets are per-chunk region work, and the old `getIntersectingChunks(world)` helper forced a
    // synchronous load of every chunk in the arena just to hand back Chunk objects. Both are illegal from
    // the instance clock on Folia, so we work from the precomputed ChunkPos set (no loading) and hop to each
    // chunk's own region to take or drop its ticket. A large arena spans several regions; one task per
    // chunk is the only shape that is correct for all of them.
    private void holdChunks() {
        World world = this.world;
        if (world == null) return;

        this.config.getCuboid().getIntersectingChunkPositions().forEach(pos -> {
            Scheduler.location().executeChunk(world, pos.x(), pos.z(),
                () -> world.getChunkAt(pos.x(), pos.z()).addPluginChunkTicket(this.plugin));
        });
    }

    private void unholdChunks() {
        World world = this.world;
        if (world == null) return;

        this.config.getCuboid().getIntersectingChunkPositions().forEach(pos -> {
            Scheduler.location().executeChunk(world, pos.x(), pos.z(),
                () -> world.getChunkAt(pos.x(), pos.z()).removePluginChunkTicket(this.plugin));
        });
    }

    private void broadcastEvent(@NonNull DungeonGameEvent event) {
        // No cancellation check: game events report what the instance clock has already done, and sealing
        // the hierarchy proved that none of them implement Cancellable, so the guard that used to sit here
        // could never fire. Only the `normal` events (DungeonJoinEvent) are cancellable, and those are
        // fired from DungeonManager, not from here.
        this.plugin.getPluginManager().callEvent(event);

        // Copy to prevent new stage/levels to handle that event if they were changed during it.
        List<DungeonEventReceiver> receivers = new ArrayList<>(this.eventReceivers);

        receivers.forEach(receiver -> receiver.onDungeonEventBroadcastReceive(event, event.getType(), this));

        // Extra set to call finish events for only tasks completed in the first loop
        // and to prevent ConcurrentModificationException since TaskFinishEvent may produce new tasks added by script actions.
        Set<StageTask> completedTasks = new HashSet<>();

        this.taskProgress.forEach((stageTask, progress) -> {
            if (progress.isCompleted()) return;

            stageTask.getTask().progress(event, this, stageTask, progress);

            if (progress.isCompleted()) completedTasks.add(stageTask);
        });

        // Call task finish events in a different loop to prevent event-in-event recursion and associated issues.
        completedTasks.forEach(stageTask -> {
            var progress = this.taskProgress.get(stageTask);
            if (progress == null) return;

            this.broadcastEvent(new DungeonTaskFinishedEvent(this, stageTask, progress));
            this.broadcast(Lang.DUNGEON_TASK_COMPLETED_INFO, replacer -> replacer
                .replace(this.replacePlaceholders())
                .replace(Placeholders.GENERIC_NAME, stageTask.getParams().getDisplay())
            );
        });
    }

    // Broadcasts fan out to players who may each be on a different region thread, and rendering a message
    // can resolve PAPI placeholders (arbitrary third-party code) against the recipient. Every recipient is
    // therefore messaged from their own scheduler rather than from whichever thread called broadcast.

    public void broadcast(@NonNull MessageLocale locale, @NonNull Consumer<Replacer> consumer) {
        this.getPlayers().forEach(gamer -> this.plugin.runTask(gamer.getPlayer(),
            () -> this.getPrefixed(locale).send(gamer.getPlayer(), consumer)));
    }

    public void broadcast(@NonNull MessageLocale locale, @NonNull BiConsumer<Player, Replacer> consumer) {
        this.getPlayers().forEach(gamer -> this.plugin.runTask(gamer.getPlayer(),
            () -> this.getPrefixed(locale).send(gamer.getPlayer(), replacer -> consumer.accept(gamer.getPlayer(), replacer))));
    }

    public void broadcast(@NonNull String message) {
        this.getPlayers().forEach(gamer -> this.plugin.runTask(gamer.getPlayer(),
            () -> Players.sendMessage(gamer.getPlayer(), message)));
    }

    public void sendMessage(@NonNull Player player, @NonNull MessageLocale locale, @NonNull Consumer<Replacer> consumer) {
        this.getPrefixed(locale).send(player, consumer);
    }

    public void runCommand(@NonNull List<String> commands, @NonNull DungeonTarget target, @Nullable DungeonGameEvent event) {
        if (target == DungeonTarget.GLOBAL) {
            // Console dispatch is global-region work: the command has no owning entity or location, and many
            // command implementations assume they are on the "main" thread, which on Folia means global.
            this.plugin.runTask(() -> commands.forEach(command -> {
                this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), command);
            }));
            return;
        }

        // Player dispatch runs as the player, so it belongs on the player's scheduler.
        this.runForPlayers(target, event, gamer ->
            this.plugin.runTask(gamer.getPlayer(), () -> Players.dispatchCommands(gamer.getPlayer(), commands)));
    }

    public void giveReward(@NonNull GameReward reward, boolean instant, @NonNull DungeonTarget target, @Nullable DungeonGameEvent event) {
        if (target == DungeonTarget.GLOBAL) {
            ErrorHandler.error("Reward must have player-specific target, not " + target.name() + ".", this);
            return;
        }

        this.runForPlayers(target, event, gamer -> this.giveReward(gamer, reward, instant));
    }

    public void giveReward(@NonNull DungeonGamer gamer, @NonNull GameReward gameReward, boolean instant) {
        Reward reward = gameReward.getReward();
        Player player = gamer.getPlayer();

        if (instant) reward.give(this, gamer);
        else gamer.addReward(gameReward);

        this.sendMessage(player, Lang.DUNGEON_GAME_REWARD_RECEIVED, replacer -> replacer.replace(this.replacePlaceholders()).replace(reward.replacePlaceholders()));
    }

    private void runForPlayers(@NonNull DungeonTarget target, @Nullable DungeonGameEvent event, @NonNull Consumer<DungeonGamer> consumer) {
        if (target == DungeonTarget.EVENT_PLAYER) {
            if (!(event instanceof GamerEvent gamerEvent)) return;

            DungeonGamer gamer = gamerEvent.getGamer();
            if (gamer != null) {
                consumer.accept(gamer);
            }
            return;
        }

        this.getPlayers().forEach(gamer -> {
            if (!gamer.isDead() || target == DungeonTarget.ALL_PLAYERS) {
                consumer.accept(gamer);
            }
        });
    }

    @NonNull
    private LangMessage getPrefixed(@NonNull MessageLocale locale) {
        return locale.withPrefix(this.prefix);
    }

    private void showStatus() {
        switch (this.state) {
            case WAITING -> this.broadcast(Lang.DUNGEON_STATUS_LOBBY_WAITING, replacer -> replacer
                .replace(Placeholders.GENERIC_CURRENT, String.valueOf(this.countPlayers()))
                .replace(Placeholders.GENERIC_MIN, String.valueOf(this.config.gameSettings().getMinPlayers()))
            );
            case READY -> {
                boolean isClose = this.countdown <= 10;
                this.broadcast((isClose ? Lang.DUNGEON_STATUS_LOBBY_READY_CLOSE : Lang.DUNGEON_STATUS_LOBBY_READY_FAR), replacer -> replacer
                    .replace(Placeholders.GENERIC_TIME, TimeFormats.toDigitalShort(TimeUnit.SECONDS.toMillis(this.countdown))));
            }
            case INGAME -> {
                if (!this.isAboutToEnd()) return;

                this.broadcast((this.gameResult == GameResult.VICTORY ? Lang.DUNGEON_STATUS_ENDING_VICTORY : Lang.DUNGEON_STATUS_ENDING_DEFEAT), replacer -> replacer
                    .replace(Placeholders.GENERIC_TIME, TimeFormats.toDigitalShort(TimeUnit.SECONDS.toMillis(this.countdown))));
            }
        }
    }

    private void spawnPlayer(@NonNull DungeonGamer gamer) {
        Player player = gamer.getPlayer();
        Kit kit = gamer.getKit();

        // Everything that touches the player waits for the move to land. Kit effects used to be applied
        // before the teleport; applying them afterwards produces the same end state and keeps the whole
        // player-facing sequence inside one entity task.
        gamer.teleportThen(this.getSpawnLocation(), () -> { // Teleport to current level's spawn.
            if (this.isKitsMode() && kit != null) {
                kit.applyPotionEffects(player);
                kit.applyAttributeModifiers(player);
            }

            player.setHealth(EntityUtil.getAttribute(player, Attribute.MAX_HEALTH)); // Restore health.

            Lang.DUNGEON_GAME_STARTED.message().send(player);
        });

        // Instance bookkeeping touches no Bukkit state, so it stays on the clock where the ordering
        // guarantees are.
        gamer.setState(GameState.INGAME);
        this.taskProgress.forEach((_, progress) -> progress.onPlayerJoined(gamer)); // Adjust task progress for new players amount.
    }

    private void leavePlayer(@NonNull DungeonPlayer gamer) {
        this.plugin.getDungeonManager().leaveInstance((DungeonGamer) gamer);
    }

    public boolean hasPermission(@NonNull Player player) {
        if (!this.config.features().isPermissionRequired()) return true;

        return player.hasPermission(Perms.PREFIX_DUNGEON + this.getId()) || player.hasPermission(Perms.DUNGEON_ALL);
    }

    public boolean canAffordEntrance(@NonNull Player player) {
        if (!this.config.features().hasEntranceCost()) return true;

        return this.config.features().getEntranceCostMap().entrySet().stream().allMatch(entry -> EconomyBridge.hasEnough(player, entry.getKey(), entry.getValue()));
    }

    public boolean hasGoodLevel(@NonNull Player player) {
        LevelRequirement requirement = this.config.features().getLevelRequirement();
        if (!requirement.isRequired()) return true;

        return requirement.isGoodLevel(player);
    }

    public void payEntrance(@NonNull Player player) {
        this.config.features().getEntranceCostMap().forEach((id, price) -> {
            EconomyBridge.withdraw(player, id, price);
        });
    }

    public void refundEntrance(@NonNull Player player) {
        this.config.features().getEntranceCostMap().forEach((id, price) -> {
            EconomyBridge.deposit(player, id, price);
        });
    }

    public void confiscateBadItems(@NonNull Player player, @NonNull List<ItemStack> confiscate) {
        ItemFilterMode mode = this.config.features().getItemFilterMode();
        if (mode == ItemFilterMode.NONE) return;

        ItemFilterCriteria criteria = this.config.features().getItemFilterCriteria();

        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack == null) continue;

            boolean matched = criteria.matches(itemStack);
            boolean needConfiscated = (mode == ItemFilterMode.BAN_SPECIFIC && matched) || (mode == ItemFilterMode.ALLOW_SPECIFIC && !matched);

            if (needConfiscated) {
                confiscate.add(new ItemStack(itemStack));
                itemStack.setAmount(0);
            }
        }

        if (!confiscate.isEmpty()) {
            this.getPrefixed(Lang.DUNGEON_CONFISACATE_INFO).send(player, replacer -> replacer
                .replace(this.replacePlaceholders())
                .replace(Placeholders.GENERIC_ITEM, confiscate.stream().map(ItemUtil::getNameSerialized).collect(Collectors.joining(", ")))
            );
        }
    }

    public boolean canJoin(@NonNull Player player, boolean force, boolean notify) {
        if (!this.isActive()) {
            if (notify) this.sendMessage(player, Lang.DUNGEON_ENTER_ERROR_INACTIVE, replacer -> replacer.replace(this.replacePlaceholders()));
            return false;
        }

        if (this.isAboutToEnd()) {
            if (notify) this.sendMessage(player, Lang.DUNGEON_ENTER_ERROR_ENDING, replacer -> replacer.replace(this.replacePlaceholders()));
            return false;
        }

        if (!force) {
            if (this.config.features().isPermissionRequired() && !this.hasPermission(player)) {
                if (notify) this.sendMessage(player, Lang.DUNGEON_ENTER_ERROR_PERMISSION, replacer -> replacer.replace(this.replacePlaceholders()));
                return false;
            }

            if (!player.hasPermission(Perms.BYPASS_DUNGEON_COOLDOWN)) {
                DungeonUser user = this.plugin.getUserManager().getOrFetch(player);
                if (user.isOnCooldown(this)) {
                    if (notify) this.sendMessage(player, Lang.DUNGEON_ENTER_ERROR_COOLDOWN, replacer -> replacer
                        .replace(Placeholders.GENERIC_TIME, TimeFormats.formatDuration(user.getArenaCooldown(this), TimeFormatType.LITERAL))
                        .replace(this.replacePlaceholders()));
                    return false;
                }
            }

            if (this.state == GameState.INGAME && !player.hasPermission(Perms.BYPASS_DUNGEON_JOIN_STARTED)) {
                if (notify) this.sendMessage(player, Lang.DUNGEON_ENTER_ERROR_STARTED, replacer -> replacer.replace(this.replacePlaceholders()));
                return false;
            }

            int playerMax = this.config.gameSettings().getMaxPlayers();
            if (playerMax > 0 && this.countPlayers() >= playerMax) {
                if (notify) this.sendMessage(player, Lang.DUNGEON_ENTER_ERROR_MAX_PLAYERS, replacer -> replacer.replace(this.replacePlaceholders()));
                return false;
            }

            if (!player.hasPermission(Perms.BYPASS_DUNGEON_ENTRANCE_COST)) {
                if (!this.canAffordEntrance(player)) {
                    if (notify) this.sendMessage(player, Lang.DUNGEON_ENTER_ERROR_COST, replacer -> replacer.replace(this.replacePlaceholders()));
                    return false;
                }
            }

            if (!player.hasPermission(Perms.BYPASS_DUNGEON_ENTRANCE_LEVEL)) {
                if (!this.hasGoodLevel(player)) {
                    if (notify) this.sendMessage(player, Lang.DUNGEON_ENTER_ERROR_LEVEL, replacer -> replacer.replace(this.replacePlaceholders()));
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void handlePlayerJoin(@NonNull DungeonPlayer dungeonPlayer, boolean forced) {
        this.handlePlayerJoin(dungeonPlayer, forced, () -> {});
    }

    @Override
    public void handlePlayerJoin(@NonNull DungeonPlayer dungeonPlayer, boolean forced, @NonNull Runnable onArrival) {
        Player player = dungeonPlayer.getPlayer();
        DungeonGamer gamer = (DungeonGamer) dungeonPlayer;
        Kit kit = gamer.getKit();

        // Save the player inventory, effects, game modes, etc. before teleporting to the arena.
        PlayerSnapshot snapshot = PlayerSnapshot.doSnapshot(player);

        if (!forced) {
            // Pay for the entrance (and kit) before cleaning.
            if (!player.hasPermission(Perms.BYPASS_DUNGEON_ENTRANCE_COST)) {
                this.payEntrance(player);
            }
            if (kit != null && KitUtils.isRentMode() && kit.hasCost() && !player.hasPermission(Perms.BYPASS_KIT_COST)) {
                kit.takeCosts(player);
            }
        }

        // A player joining a game already in progress used to be teleported twice - once to the lobby, then
        // again to the spawn as a spectator. Two teleports issued back to back are two region changes whose
        // continuations can interleave, so the destination is decided up front and the player moves once.
        boolean joinInProgress = this.state == GameState.INGAME;
        Location destination = joinInProgress ? this.getSpawnLocation() : this.getLobbyLocation();
        if (joinInProgress) gamer.setDead(true);

        // Now clear all player's active effects, god modes, etc.
        gamer.teleportThen(destination, () -> {
            player.setGameMode(joinInProgress ? GameMode.SPECTATOR : this.getGameMode());
            PlayerSnapshot.clear(player);
            Players.dispatchCommands(player, this.config.features().getEntranceCommands());
            UniParticle.of(Particle.CLOUD).play(player.getLocation(), 0.25, 0.15, 30);

            if (this.isKitsMode() && kit != null) {
                player.getInventory().clear();
                kit.give(player);
            }
            else {
                this.confiscateBadItems(player, snapshot.getConfiscate()); // TODO Permission?
            }

            this.sendMessage(player, Lang.DUNGEON_JOIN_LOBBY, replacer -> replacer.replace(this.replacePlaceholders()));

            // Disable external Scoreboard and God mode.
            gamer.manageExternalBoard(boardPlugin -> boardPlugin.disableBoard(player));
            gamer.manageExternalGod(godPlugin -> godPlugin.disableGod(player));

            if (this.config.gameSettings().isScoreboardEnabled() && DungeonUtils.hasPacketLibrary()) {
                gamer.addBoard();
            }

            onArrival.run();
        });

        this.broadcast(Lang.DUNGEON_JOIN_NOTIFY, replacer -> replacer.replace(this.replacePlaceholders()).replace(gamer.replacePlaceholders()));

        this.players.put(player.getUniqueId(), gamer);

        // this.updateSigns();
    }

    @Override
    public void handlePlayerLeave(@NonNull DungeonPlayer dungeonPlayer) {
        DungeonGamer gamer = (DungeonGamer) dungeonPlayer;
        Player player = gamer.getPlayer();

        player.closeInventory();
        gamer.removeBoard();
        this.players.remove(player.getUniqueId());

        if (this.state == GameState.INGAME && this.gameResult == null && gamer.isInGame()) {
            this.taskProgress.forEach((_, progress) -> progress.onPlayerLeft(gamer));
        }

        boolean wasInGame = this.state == GameState.INGAME;

        if (!this.isAboutToEnd() || this.gameResult == GameResult.DEFEAT) {
            gamer.takeDefeatRewards();
        }

        // Remove kit effects.
        Kit kit = gamer.getKit();
        if (kit != null) {
            kit.resetPotionEffects(player);
            kit.resetAttributeModifiers(player);
        }

        // Restore player data. This is now asynchronous (cross-world teleportAsync + inventory restore), and
        // everything below hands items to the player - so it MUST wait. Running the reward grant before the
        // restore completes would have the snapshot's setContents() overwrite the rewards a moment later.
        PlayerSnapshot.restore(gamer).thenRun(() -> {
            Players.dispatchCommands(player, this.config.features().getExitCommands());

            // Refund payments.
            if (!wasInGame) {
                if (!player.hasPermission(Perms.BYPASS_DUNGEON_ENTRANCE_COST)) {
                    this.refundEntrance(player);
                }
                if (kit != null && KitUtils.isRentMode() && kit.hasCost() && !player.hasPermission(Perms.BYPASS_KIT_COST)) {
                    kit.refundCosts(player);
                }
            }
            else {
                gamer.getRewards().forEach(reward -> reward.getReward().give(this, gamer));

                // Set cooldown only if dungeon have been started.
                if (!player.hasPermission(Perms.BYPASS_DUNGEON_COOLDOWN)) {
                    this.plugin.getDungeonManager().setJoinCooldown(player, this);
                }
            }

            // Enable back external Scoreboard and God mode.
            gamer.manageExternalBoard(boardPlugin -> boardPlugin.enableBoard(player));
            gamer.manageExternalGod(godPlugin -> godPlugin.enableGod(player));
        });
    }

    public void handlePlayerDeath(@NonNull DungeonGamer gamer) {
        boolean hasExtraLives = gamer.hasExtraLives();

        gamer.handleDeath();

        if (!hasExtraLives && this.config.gameSettings().isLeaveOnDeath()) {
            // Deferred so the death event finishes first. The retired callback is not optional: if the player
            // disconnects before the next tick their entity scheduler never runs again, and without a
            // fallback the instance would keep them in `players` and their snapshot in SNAPSHOTS forever.
            this.plugin.runTask(gamer.getPlayer(),
                () -> this.leavePlayer(gamer),
                () -> Scheduler.sync().run(task -> this.leavePlayer(gamer)));
        }

        DungeonPlayerDeathEvent event = new DungeonPlayerDeathEvent(this, gamer);
        this.broadcastEvent(event);
    }

    public void handleMobDeath(@NonNull DungeonEntity mob, @NonNull EntityDeathEvent event) {
        if (!this.config.gameSettings().isMobsDropLoot()) {
            event.getDrops().clear();
        }
        if (!this.config.gameSettings().isMobsDropXP()) {
            event.setDroppedExp(0);
        }

        DungeonMobKilledEvent mobDeathEvent = new DungeonMobKilledEvent(this, mob);

        LivingEntity entity = mob.getBukkitEntity();
        Player killer = entity.getKiller();
        DungeonGamer gamer = killer == null ? null : this.getPlayer(killer.getUniqueId());
        if (killer != null && gamer != null) {
            mobDeathEvent.setGamer(gamer);

            int streak = gamer.getKillStreak() + 1;
            gamer.addKill();
            gamer.setKillStreak(streak);
            gamer.setKillStreakDecay(Config.KILL_STREAKS_DECAY_TIME.get());

            if (DungeonUtils.isKillStreaksEnabled()) {
                KillStreak killStreak = DungeonUtils.getKillStreak(gamer.getKillStreak());
                if (killStreak != null) {
                    killStreak.run(this, gamer);
                }
            }
        }

        this.broadcastEvent(mobDeathEvent);
        this.eliminateMob(mob);
    }

    public void handleMobSpawn(@NonNull LivingEntity entity) {
        if (MobUitls.isPet(entity)) {
            if (!this.config.gameSettings().isPetsAllowed()) {
                entity.remove();
            }
            return;
        }

        if (this.hasMob(entity.getUniqueId())) return;

        MobProvider provider = MobRegistry.getProvider(entity);
        String mobId = provider == null ? null : provider.getMobId(entity);
        if (provider == null || mobId == null) {
            if (Config.MOBS_REMOVE_UNKNOWN_MOBS.get()) {
                entity.remove();
            }
            return;
        }

        boolean isTamed = entity instanceof Tameable tameable && tameable.isTamed();

        MobIdentifier identifier = MobIdentifier.from(provider, mobId);
        MobFaction faction = MobUitls.isExternalAlly(identifier) || isTamed ? MobFaction.ALLY : MobFaction.ENEMY;

        DungeonMob dungeonMob = new DungeonMob(this, entity, faction, provider, mobId);
        this.addMob(dungeonMob);
    }

    public void handleStageEnd() {
        this.stageCompleted = true;
        this.broadcastEvent(new DungeonStageFinishEvent(this, this.stage));
    }

    public boolean isReadyToStart() {
        return this.hasMinimumPlayers();
    }

    public boolean hasMinimumPlayers() {
        return this.countPlayers() >= this.config.gameSettings().getMinPlayers();
    }

    public void setLevel(@NonNull Level level) {
        if (this.isLevel(level)) return;

        this.level = level;
        this.updateListeners();
        this.broadcastEvent(new DungeonLevelStartEvent(this, level));
        this.broadcast(Lang.DUNGEON_GAME_LEVEL_CHANGED, replacer -> replacer.replace(this.replacePlaceholders()).replace(level.replacePlaceholders()));
    }

    public boolean isLevel(@NonNull Level level) {
        return this.level == level;
    }

    public void setStage(@NonNull Stage stage) {
        if (this.isStage(stage)) return;

        this.removeTasks();
        this.stage = stage;
        this.stageCompleted = false;
        this.updateListeners();
        this.addTasks(stage);
        this.broadcastEvent(new DungeonStageStartEvent(this, stage));
        this.broadcast(Lang.DUNGEON_GAME_STAGE_CHANGED, replacer -> replacer.replace(this.replacePlaceholders()).replace(stage.replacePlaceholders()));
    }

    public boolean isStage(@NonNull Stage stage) {
        return this.stage == stage;
    }



    public boolean isAboutToEnd() {
        return this.state == GameState.INGAME && this.countdown >= 0 && this.gameResult != null;
    }



    private void eliminateDeadMobs() {
        // isDead()/isValid() are live entity reads, so the check runs on the mob's own scheduler rather than
        // on the instance clock. The retired branch matters as much as the check: a mob whose entity has
        // already been removed has no scheduler left to run the check on, and without an explicit fallback
        // it would sit in mobByIdMap forever - which is exactly the leak this sweep exists to prevent.
        this.getMobs().forEach(mob -> this.plugin.runTask(mob.getBukkitEntity(),
            () -> {
                if (mob.isDead()) this.eliminateMob(mob);
            },
            () -> this.eliminateMob(mob)));
    }

    @Override
    public void killMobs() {
        // The redundant first pass (a bare remove() on every mob) is gone: eliminateMob already removes the
        // entity, and doing it twice meant two wrong-thread entity mutations instead of one correct one.
        this.getMobs().forEach(this::eliminateMob);
        this.mobByIdMap.clear();
    }

    @Override
    public boolean isAllyMob(@NonNull LivingEntity entity) {
        return this.getMobFaction(entity) == MobFaction.ALLY;
    }

    @Override
    public boolean isEnemyMob(@NonNull LivingEntity entity) {
        return this.getMobFaction(entity) == MobFaction.ENEMY;
    }

    @Override
    public boolean hasAllyMobs() {
        return this.hasMobsOfFaction(MobFaction.ALLY);
    }

    @Override
    public boolean hasEnemyMobs() {
        return this.hasMobsOfFaction(MobFaction.ENEMY);
    }

    @Override
    public boolean hasMobsOfFaction(@NonNull MobFaction faction) {
        return this.getMobs().stream().anyMatch(mob -> mob.isFaction(faction));
    }

    @Override
    @Nullable
    public MobFaction getMobFaction(@NonNull LivingEntity entity) {
        DungeonMob mob = this.getMob(entity);
        return mob == null ? null : mob.getFaction();
    }

    @Override
    public void eliminateMob(@NonNull DungeonEntity mob) {
        LivingEntity entity = mob.getBukkitEntity();

        // Was: `getLocation().getChunk()` - a synchronous chunk load whose only purpose was to force the
        // entity resident so that remove() would take effect. On Folia that is both wrong-thread and
        // unnecessary: scheduling on the entity guarantees it is loaded and owned when the task runs, and if
        // the entity is already gone the scheduler is retired and the task is simply dropped.
        this.plugin.runTask(entity, () -> {
            entity.setPersistent(false);
            if (!entity.isDead()) {
                entity.remove();
            }
        });

        this.stats.addMobKill(mob);
        this.removeMob(mob);
        DungeonEntityBridge.removeHolder(mob);

        // Same reasoning as spawnMob: elimination is reached from the mob's own scheduler and from
        // listeners, but the script actions this event triggers belong on the instance clock.
        this.plugin.runTask(() -> this.broadcastEvent(new DungeonMobEliminatedEvent(this, mob)));
    }

    public boolean spawnAllyMob(@NonNull EntityType entityType, @NonNull Location location, int level) {
        MobIdentifier identifier = MobUitls.getEggAllyIdentifier(entityType);
        if (identifier == null) return false;

        MobProvider provider = MobRegistry.getProviderByName(identifier.getProviderId());
        if (provider == null) return false;

        String mobId = identifier.getMobId();
        MobFaction faction = MobFaction.ALLY;

        this.spawnMob(provider, mobId, faction, location, level);
        return true;
    }

    @Override
    public void spawnMob(@NonNull MobProvider provider, @NonNull String mobId, @NonNull MobFaction faction, @NonNull DungeonSpawner spawner, int level, int amount) {
        if (spawner.isEmpty()) {
            ErrorHandler.error("Could not spawn mob '" + provider.getName() + ":" + mobId + "' at empty spawner '" + spawner.getId() + "'!", this);
            return;
        }

        for (int i = 0; i < amount; i++) {
            Location location = spawner.getRandomPosition().toLocation(this.world)
                .add(MobUitls.getRandomSpawnOffset(), 0, MobUitls.getRandomSpawnOffset());

            this.spawnMob(provider, mobId, faction, location, level);
        }
    }

    @Override
    public void spawnMob(@NonNull MobProvider provider, @NonNull String mobId, @NonNull MobFaction faction, @NonNull Location location, int level) {
        // Spawning is world mutation: it has to happen on the region that owns the spawn point, which for a
        // dungeon spanning several regions is not necessarily the one running the instance clock.
        this.plugin.runTask(location, () -> {
            LivingEntity mob = provider.spawn(this, mobId, faction, location, level, entity -> {
                // Probably don't need to store providerId and mobId, since these values are already stored in a DungeonMob.
            });
            if (mob == null) {
                ErrorHandler.error("Could not spawn mob '" + provider.getName() + ":" + mobId + "', spawned entity is null!", this);
                return;
            }

            DungeonMob dungeonMob = new DungeonMob(this, mob, faction, provider, mobId);

            // addMob tags and configures the freshly spawned entity, so it belongs here, on the region that
            // just created it and therefore owns it.
            this.addMob(dungeonMob);

            // The event fan-out does not. broadcastEvent walks the stage tasks and runs script actions, and
            // those expect the same context as every other event this instance emits. Hand it back to the
            // clock so the instance keeps exactly one thread driving its event chain.
            this.plugin.runTask(() -> this.broadcastEvent(new DungeonMobSpawnedEvent(this, dungeonMob)));
        });
    }

    @Override
    public void addMob(@NonNull DungeonEntity mob) {
        this.mobByIdMap.put(mob.getUniqueId(), (DungeonMob) mob);
        this.stats.addMobSpawn(mob);

        LivingEntity entity = mob.getBukkitEntity();

        MobUitls.setDungeonId(entity, this);
        entity.setPersistent(true);
        entity.setRemoveWhenFarAway(false);
        DungeonEntityBridge.addHolder(mob);
    }

    @Override
    public void removeMob(@NonNull DungeonEntity mob) {
        this.removeMob(mob.getUniqueId());
    }

    @Override
    public void removeMob(@NonNull UUID mobId) {
        this.mobByIdMap.remove(mobId);
    }

    @Override
    public boolean hasMob(@NonNull UUID mobId) {
        return this.mobByIdMap.containsKey(mobId);
    }

    @Override
    @Nullable
    public DungeonMob getMob(@NonNull LivingEntity entity) {
        return this.getMobById(entity.getUniqueId());
    }

    @Override
    @Nullable
    public DungeonMob getMobById(@NonNull UUID mobId) {
        return this.mobByIdMap.get(mobId);
    }

    @Override
    @NonNull
    public Set<DungeonMob> getAllyMobs() {
        return this.getMobs(MobFaction.ALLY);
    }

    @Override
    @NonNull
    public Set<DungeonMob> getEnemyMobs() {
        return this.getMobs(MobFaction.ENEMY);
    }

    @Override
    @NonNull
    public Set<DungeonMob> getMobs() {
        return new HashSet<>(this.mobByIdMap.values());
    }

    @Override
    @NonNull
    public Set<DungeonMob> getMobs(@NonNull MobFaction faction) {
        return this.queryMobs(mob -> mob.isFaction(faction));
    }

    @NonNull
    public Set<DungeonMob> queryMobs(@NonNull Predicate<CriterionMob> predicate) {
        return this.getMobs().stream().filter(predicate).collect(Collectors.toSet());
    }

    public int countMobs(@NonNull MobFaction faction) {
        return this.countMobs(mob -> MobCriterias.FACTION.predicate(faction).test(mob));
    }

    public int countMobs(@NonNull Predicate<CriterionMob> predicate) {
        return this.queryMobs(predicate).size();
    }



    public void addTasks(@NonNull Stage stage) {
        stage.getTasks().forEach(stageTask -> {
            if (stageTask.getParams().isAutoAdd()) {
                this.addTask(stageTask);
            }
        });
    }

    public void addTask(@NonNull StageTask stageTask) {
        TaskProgress progress = stageTask.createProgress(this);
        if (progress.isEmpty()) return;

        stageTask.getTask().onTaskAdd(this, stageTask, progress);

        this.taskProgress.put(stageTask, progress);
        this.stageCompleted = false;

        boolean isPersonal = stageTask.getParams().isPerPlayer();

        this.broadcast((isPersonal ? Lang.DUNGEON_TASK_CREATED_PERSONAL : Lang.DUNGEON_TASK_CREATED_GLOBAL), (player, replacer) -> replacer
            .replace(this.replacePlaceholders())
            .replace(Placeholders.GENERIC_NAME, stageTask.getParams().getDisplay())
            .replace(Placeholders.GENERIC_VALUE, progress.format(isPersonal ? player : null)));

        this.broadcastEvent(new DungeonTaskCreatedEvent(this, stageTask, progress));
    }

    public void removeTask(@NonNull StageTask stageTask) {
        TaskProgress progress = this.taskProgress.remove(stageTask);
        if (progress == null) return;

        stageTask.getTask().onTaskRemove(this, stageTask, progress);
    }

    public void removeTasks() {
        this.getTasks().forEach(this::removeTask);
    }

    @NonNull
    public Set<StageTask> getTasks() {
        return new HashSet<>(this.taskProgress.keySet());
    }

    public boolean hasTask(@NonNull StageTask stageTask) {
        return this.taskProgress.containsKey(stageTask);
    }

    public boolean hasTasks() {
        return !this.taskProgress.isEmpty();
    }

    public boolean isTaskCompleted(@NonNull StageTask stageTask) {
        TaskProgress progress = this.taskProgress.get(stageTask);
        return progress != null && progress.isCompleted();
    }

    public boolean isTasksCompleted() {
        return !this.taskProgress.isEmpty() && this.taskProgress.values().stream().allMatch(TaskProgress::isCompleted);
    }





    public void refillLootChests() {
        this.config.getLootChests().forEach(this::refillLootChest);
    }

    // Loot chests are block entities scattered across the arena, so each one is touched on the region that
    // owns its own block - not on whichever region happens to own the instance anchor.
    public void refillLootChest(@NonNull LootChest lootChest) {
        this.atLootChest(lootChest, () -> lootChest.generateLoot(this));
    }

    public void clearLootChests() {
        this.config.getLootChests().forEach(this::clearLootChest);
    }

    public void clearLootChest(@NonNull LootChest lootChest) {
        this.atLootChest(lootChest, () -> lootChest.clearLoot(this));
    }

    private void atLootChest(@NonNull LootChest lootChest, @NonNull Runnable runnable) {
        World world = this.world;
        if (world == null) return;

        this.plugin.runTask(lootChest.getBlockPos().toLocation(world), runnable);
    }





    public boolean isKitsMode() {
        return this.config.gameSettings().isKitsEnabled();
    }

    public boolean isKitAllowed(@NonNull Kit kit) {
        return this.config.gameSettings().isKitAllowed(kit.getId());
    }

    public boolean isKitAvailable(@NonNull Kit kit) {
        if (!this.isKitAllowed(kit)) {
            return false;
        }

        return !this.isKitLimitReached(kit);
    }

    public boolean isKitLimitReached(@NonNull Kit kit) {
        return this.countKitFreeSlots(kit) == 0;
    }

    public int getKitLimit(@NonNull Kit kit) {
        return this.config.gameSettings().getKitLimit(kit.getId());
    }

    public int countKitInUse(@NonNull Kit kit) {
        return (int) this.getPlayers().stream().filter(gamer -> gamer.isKit(kit)).count();
    }

    public int countKitFreeSlots(@NonNull Kit kit) {
        int limit = this.getKitLimit(kit);
        if (limit < 0) return -1;

        return limit - this.countKitInUse(kit);
    }


    @Override
    public boolean hasPlayer(@NonNull UUID playerId) {
        return this.players.containsKey(playerId);
    }

    @Override
    public boolean hasAlivePlayers() {
        return this.players.values().stream().anyMatch(DungeonPlayer::isAlive);
    }

    @Override
    public int countPlayers() {
        return this.players.size();
    }

    @Override
    public int countAlivePlayers() {
        return this.getAlivePlayers().size();
    }

    @Override
    public int countDeadPlayers() {
        return this.getDeadPlayers().size();
    }

    @Override
    @NonNull
    public DungeonGamer getRandomAlivePlayer() {
        return Rnd.get(this.getPlayers());
    }

    @Override
    @Nullable
    public DungeonGamer getPlayer(@NonNull UUID playerId) {
        return this.players.get(playerId);
    }

    @Override
    @NonNull
    public Set<DungeonGamer> getPlayers() {
        return new HashSet<>(this.players.values());
    }

    @NonNull
    public Set<DungeonGamer> getAlivePlayers() {
        return this.players.values().stream().filter(DungeonPlayer::isAlive).collect(Collectors.toSet());
    }

    @NonNull
    public Set<DungeonGamer> getDeadPlayers() {
        return this.players.values().stream().filter(DungeonPlayer::isDead).collect(Collectors.toSet());
    }




    public void addGroundItem(@NonNull Item item) {
        this.groundItems.removeIf(other -> !other.isValid());
        this.groundItems.add(item);

        if (this.state == GameState.INGAME && !this.config.gameSettings().isItemPickupAllowed()) {
            item.setPickupDelay(Short.MAX_VALUE);
            item.setOwner(UUID.randomUUID());
        }
    }

    public void burnGroundItems() {
        UniParticle particle = UniParticle.of(Particle.SMOKE);

        // isValid / isOnGround / getLocation / remove are all reads and writes of live entity state, so each
        // item is inspected on its own scheduler rather than in one sweep from the instance clock. The set
        // is concurrent precisely so these tasks can retire themselves out of it as they run.
        this.groundItems.forEach(item -> this.plugin.runTask(item, () -> {
            if (!item.isValid()) {
                this.groundItems.remove(item);
                return;
            }
            if (!item.isOnGround()) return;

            particle.play(item.getLocation(), 0.1, 0.05, 15);
            item.remove();
            this.groundItems.remove(item);
        }, () -> this.groundItems.remove(item))); // Retired: the item is already gone.
    }

    public void killGroundItems() {
        Set<Item> items = new HashSet<>(this.groundItems);
        this.groundItems.clear();
        items.forEach(item -> this.plugin.runTask(item, item::remove));
    }

    @NonNull
    public Set<Item> getGroundItems() {
        return this.groundItems;
    }




    public void resetSpotStates() {
        this.config.getSpots().forEach(this::resetSpotState);
    }

    public void resetSpotState(@NonNull Spot spot) {
        SpotState state = spot.getDefaultState();
        if (state == null) return;

        this.setSpotState(spot, state);
    }

    public void setSpotState(@NonNull Spot spot, @NonNull SpotState state) {
        if (!this.isActive()) return;

        spot.build(this.world, state);
        spot.setLastState(state);

        this.broadcastEvent(new DungeonSpotChangeEvent(this, spot, state));
    }




    @Override
    public boolean contains(@NonNull BlockPos blockPos) {
        return this.config.isInProtection(blockPos);
    }

    @Override
    public boolean contains(@NonNull Block block) {
        return this.config.isInProtection(block);
    }

    @Override
    public boolean contains(@NonNull Location location) {
        return this.config.isInProtection(location);
    }

    @Override
    public boolean contains(@NonNull Entity entity) {
        return this.config.isInProtection(entity);
    }



    @Override
    public long getTickCount() {
        return this.tickCount;
    }

    @Override
    @NonNull
    public String getId() {
        return this.config.getId();
    }

    @NonNull
    public DungeonConfig getConfig() {
        return this.config;
    }

    @NonNull
    public DungeonStats getStats() {
        return this.stats;
    }

    @NonNull
    public DungeonVariables getVariables() {
        return this.variables;
    }

    @Override
    @NonNull
    public GameState getState() {
        return this.state;
    }

    @NonNull
    public Location getLobbyLocation() {
        return this.config.getLobbyPos().toLocation(this.world);
    }

    /**
     *
     * @return Spawn location of the current dungeon level.
     */
    @NonNull
    public Location getSpawnLocation() {
        return this.level.getSpawnLocation(this.world);
    }

    public int getCountdown() {
        return this.countdown;
    }

    public void setCountdown(int countdown) {
        this.countdown = countdown;
    }

    public void setCountdown(int countdown, @NonNull GameResult gameResult) {
        this.countdown = countdown;
        if (this.state == GameState.INGAME) {
            this.gameResult = gameResult;
        }
    }

    public long getTimeLeft() {
        return this.timeLeft;
    }

    public void setTimeLeft(long timeLeft) {
        this.timeLeft = timeLeft;
    }

    @NonNull
    public Level getLevel() {
        return this.level;
    }

    @NonNull
    public Stage getStage() {
        return this.stage;
    }

    @NonNull
    public GameMode getGameMode() {
        return this.config.gameSettings().isAdventureMode() ? GameMode.ADVENTURE : GameMode.SURVIVAL;
    }

    @NonNull
    public List<DungeonEventReceiver> getEventReceivers() {
        return this.eventReceivers;
    }

    @NonNull
    public Map<StageTask, TaskProgress> getTaskProgress() {
        return this.taskProgress;
    }
}

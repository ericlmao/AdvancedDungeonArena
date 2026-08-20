package su.nightexpress.dungeons.dungeon.player;

import gg.moonrise.scheduler.Scheduler;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.dungeons.Placeholders;
import su.nightexpress.dungeons.api.compat.BoardPlugin;
import su.nightexpress.dungeons.api.compat.GodPlugin;
import su.nightexpress.dungeons.api.dungeon.Board;
import su.nightexpress.dungeons.api.dungeon.DungeonPlayer;
import su.nightexpress.dungeons.api.type.GameState;
import su.nightexpress.dungeons.config.Lang;
import su.nightexpress.dungeons.dungeon.feature.board.BoardLayout;
import su.nightexpress.dungeons.dungeon.feature.board.impl.PacketsBoard;
import su.nightexpress.dungeons.dungeon.feature.board.impl.ProtocolBoard;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.level.Level;
import su.nightexpress.dungeons.dungeon.reward.GameReward;
import su.nightexpress.dungeons.kit.impl.Kit;
import su.nightexpress.dungeons.registry.compat.BoardPluginRegistry;
import su.nightexpress.dungeons.registry.compat.GodPluginRegistry;
import su.nightexpress.dungeons.util.DungeonUtils;
import su.nightexpress.dungeons.nightcore.locale.entry.MessageLocale;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class DungeonGamer implements DungeonPlayer {

    //private final DungeonPlugin plugin;
    private final Player           player;
    private final DungeonInstance  dungeon;
    private final List<GameReward> rewards;

    private final GodPlugin   godPlugin;
    private final BoardPlugin boardPlugin;

    private GameState state;
    private Board board;
    private Kit   kit;

    private Location deathLocation;

    /** Snapshot of {@link #player}'s position, taken on the player's own scheduler. See {@link #tick()}. */
    private volatile Location lastKnownLocation;

    /**
     * This player's line on <i>other</i> players' scoreboards, rendered on this player's own scheduler.
     * <p>
     * Same reasoning as {@link #lastKnownLocation}: every board in the instance lists every participant, so
     * each render would otherwise read every peer's display name and state from a thread that does not own
     * them. Each player renders their own line instead and publishes the finished string. At most one
     * instance tick stale, which is the rate the boards refresh at anyway.
     */
    private volatile String boardEntry;

    private boolean dead;
    private long    deathTime;
    private int     lives;
    private int     killStreak;
    private long    killStreakDecay;
    private int     kills;
    private int     score;

    /** Written from the teleport completion stage, read from the teleport listener on another thread. */
    private volatile boolean teleporting;

    public DungeonGamer(@NotNull Player player, @NotNull DungeonInstance dungeon) {
        this.player = player;
        this.dungeon = dungeon;
        this.state = GameState.WAITING;
        this.rewards = new ArrayList<>();
        this.setDead(false);
        this.deathTime = -1L;
        this.setLives(dungeon.getConfig().gameSettings().getPlayerLives());

        this.godPlugin = GodPluginRegistry.getGodProvider(player);
        this.boardPlugin = BoardPluginRegistry.getBoardProvider(player);

        // Published up front so a peer rendering between this player joining and their first tick still has
        // a line for them. The constructor runs on the joining player's own thread, like tick() does.
        this.refreshBoardEntry();
    }

    private void refreshBoardEntry() {
        this.boardEntry = this.replacePlaceholders()
            .apply((this.isReady() ? Lang.UI_BOARD_PLAYER_READY : Lang.UI_BOARD_PLAYER_NOT_READY).text());
    }

    /**
     * This player's pre-rendered scoreboard line. Safe to read from any thread. See {@link #boardEntry}.
     */
    @NotNull
    public String getBoardEntry() {
        return this.boardEntry;
    }

    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return Placeholders.DUNGEON_GAMER.replacer(this);
    }

    @Override
    public boolean isReady() {
        return this.state == GameState.READY && !this.isDead();
    }

    @Override
    public boolean isInLobby() {
        return this.state != GameState.INGAME;
    }

    @Override
    public boolean isInGame() {
        return this.state == GameState.INGAME;
    }

    @Override
    public void tick() {
        // Published for code that needs this player's position but does not own this player - script area
        // tasks, chiefly. Reading player.getLocation() from another region's thread is exactly the kind of
        // cross-region access Folia exists to prevent, so the position is snapshotted here, on the only
        // thread allowed to read it, and consumers take the snapshot instead. It is at most one instance
        // tick (one second) stale, which is the same resolution the area tasks evaluate at anyway.
        this.lastKnownLocation = this.player.getLocation();
        this.refreshBoardEntry();

        if (this.isDead() && !this.dungeon.isAboutToEnd()) {
            (this.hasExtraLives() ? Lang.DUNGEON_STATUS_DEAD_LIVES : Lang.DUNGEON_STATUS_DEAD_NO_LIVES).message().send(this.player, replacer -> replacer
                .replace(this.dungeon.replacePlaceholders())
                .replace(this.replacePlaceholders())
            );
        }

        if (this.killStreakDecay-- <= 0) {
            this.setKillStreak(0);
            this.setKillStreakDecay(0);
        }

        if (this.kit != null) {
            this.kit.applyPotionEffects(this.player);
        }

        this.updateBoard();
    }

    @Override
    @NotNull
    public CompletableFuture<Boolean> teleport(@NotNull Location location) {
        // The `teleporting` flag is what tells DungeonGameListener#onDungeonPlayerTeleport not to cancel our
        // own boundary-crossing teleports. It therefore has to stay raised for the entire flight, not just
        // for the duration of this method - PlayerTeleportEvent fires while teleportAsync is still in
        // progress, and on Folia it fires on a different thread than this one. Hence `volatile`, and hence
        // clearing the flag from the completion stage rather than on the next line.
        this.teleporting = true;

        CompletableFuture<Boolean> arrival = new CompletableFuture<>();

        this.player.teleportAsync(location, PlayerTeleportEvent.TeleportCause.PLUGIN).whenComplete((success, error) -> {
            this.teleporting = false;

            boolean arrived = error == null && Boolean.TRUE.equals(success);

            // Hand the continuation back on the player's own scheduler. teleportAsync completes on whichever
            // region finished the move, and every caller goes on to mutate the player; completing the future
            // from inside an entity task is what makes those mutations legal.
            this.onPlayerThread(() -> arrival.complete(arrived));
        });

        return arrival;
    }

    @Override
    public void teleportThen(@NotNull Location location, @NotNull Runnable onArrival) {
        // teleport() already completes `arrival` on the player's scheduler, and completion is always at
        // least one tick away, so the callback attached here cannot run inline on the calling thread.
        this.teleport(location).thenRun(onArrival);
    }

    /**
     * Runs on the thread that owns this player, or on the global region if the player is gone.
     * <p>
     * The fallback is what keeps the leave path from stalling: an entity scheduler that has already retired
     * accepts nothing and reports nothing, so without it a player who disconnects mid-teleport would leave
     * their instance bookkeeping, refunds and rewards permanently unfinished.
     */
    private void onPlayerThread(@NotNull Runnable runnable) {
        Runnable fallback = () -> Scheduler.sync().run(task -> runnable.run());

        if (Scheduler.entity(this.player).run(task -> runnable.run(), fallback) == null) {
            fallback.run();
        }
    }

    @Override
    public void revive() {
        if (!this.isDead() || !this.hasExtraLives()) return;

        Level level = this.dungeon.getLevel();

        this.teleportThen(level.getSpawnLocation(this.dungeon.getWorld()), () -> {
            this.setDead(false);
            this.player.setGameMode(this.dungeon.getGameMode());

            if (this.kit != null) {
                this.kit.applyPotionEffects(this.player);
                this.kit.applyAttributeModifiers(this.player);
            }
            //this.player.playEffect(EntityEffect.TOTEM_RESURRECT);

            MessageLocale locale = this.hasExtraLives() ? Lang.DUNGEON_REVIVE_WITH_LIFES : Lang.DUNGEON_REVIVE_NO_LIFES;
            this.dungeon.sendMessage(this.player, locale, replacer -> replacer.replace(this.replacePlaceholders()));
        });
    }

    @Override
    public void handleDeath() {
        Location location = this.player.getLocation();
        if (!this.dungeon.contains(location)) {
            location = this.dungeon.getSpawnLocation();
        }

        this.setDeathLocation(location);
        this.setDead(true);
        this.deathTime = System.currentTimeMillis();
        this.takeExtraLive();
        this.takeDeathRewards();

        if (!this.hasExtraLives()) {
            if (this.dungeon.hasAlivePlayers()) {
                this.dungeon.sendMessage(this.player, Lang.DUNGEON_DEATH_NO_LIFES, replacer -> replacer.replace(this.replacePlaceholders()));
            }
        }
        else {
            if (this.kit != null) {
                this.kit.resetPotionEffects(this.player);
                this.kit.resetAttributeModifiers(this.player);
            }

            if (this.dungeon.hasAlivePlayers()) {
                this.dungeon.sendMessage(this.player, Lang.DUNGEON_DEATH_WITH_LIFES, replacer -> replacer.replace(this.replacePlaceholders()));
            }
        }

        this.setKillStreak(0);
        this.setKillStreakDecay(0);

        this.dungeon.broadcast(Lang.DUNGEON_GAME_PLAYER_DIED, replacer -> replacer
            .replace(this.dungeon.replacePlaceholders())
            .replace(this.replacePlaceholders())
        );
    }

    public void handleRespawn() {
        this.player.setGameMode(GameMode.SPECTATOR);
    }

    @Override
    public void manageExternalGod(@NotNull Consumer<GodPlugin> consumer) {
        if (this.godPlugin != null) {
            consumer.accept(this.godPlugin);
        }
    }

    @Override
    public void manageExternalBoard(@NotNull Consumer<BoardPlugin> consumer) {
        if (this.boardPlugin != null) {
            consumer.accept(this.boardPlugin);
        }
    }

    @Override
    public void addBoard() {
        BoardLayout layout = this.dungeon.getConfig().gameSettings().getBoardLayout();
        if (layout == null) return;

        if (DungeonUtils.hasPacketEvents()) {
            this.board = new PacketsBoard(this, layout);
        }
        else {
            this.board = new ProtocolBoard(this, layout);
        }

        this.board.create();
        this.board.update();
    }

    @Override
    public void removeBoard() {
        if (this.board != null) {
            this.board.remove();
            this.board = null;
        }
    }

    @Override
    public void updateBoard() {
        if (this.board != null) {
            this.board.update();
        }
    }

    @Override
    @NotNull
    public Player getPlayer() {
        return this.player;
    }

    @Override
    @NotNull
    public DungeonInstance getDungeon() {
        return this.dungeon;
    }

    @Override
    @NotNull
    public GameState getState() {
        return this.state;
    }

    @Override
    public void setState(@NotNull GameState state) {
        this.state = state;
    }

    @NotNull
    public List<GameReward> getRewards() {
        return this.rewards;
    }

    public void addReward(@NotNull GameReward reward) {
        this.rewards.add(reward);
    }

    public void takeDeathRewards() {
        this.rewards.removeIf(reward -> !reward.isKeepOnDeath());
    }

    public void takeDefeatRewards() {
        this.rewards.removeIf(reward -> !reward.isKeepOnDefeat());
    }

    public void clearRewards() {
        this.rewards.clear();
    }

    @Nullable
    public Kit getKit() {
        return this.kit;
    }

    public void setKit(@Nullable Kit kit) {
        this.kit = kit;
    }

    public boolean hasKit() {
        return this.kit != null;
    }

    public boolean isKit(@NotNull Kit kit) {
        return this.kit == kit;
    }

    @Override
    public boolean isDead() {
        return this.dead;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    /**
     * @return the player's position as of their last tick, safe to read from any thread; {@code null} until
     *         the player has ticked at least once.
     */
    @Override
    @Nullable
    public Location getLastKnownLocation() {
        return this.lastKnownLocation;
    }

    @Nullable
    public Location getDeathLocation() {
        return this.deathLocation;
    }

    public void setDeathLocation(@Nullable Location deathLocation) {
        this.deathLocation = deathLocation;
    }

    @Override
    public long getDeathTime() {
        return this.deathTime;
    }

    @Override
    public boolean isAlive() {
        return !this.isDead();
    }

    @Override
    public boolean hasExtraLives() {
        return this.lives > 0;
    }

    public int getLives() {
        return this.lives;
    }

    public void setLives(int lives) {
        this.lives = Math.max(0, lives);
    }

    @Override
    public void addExtraLive() {
        this.setLives(this.lives + 1);
    }

    @Override
    public void takeExtraLive() {
        this.setLives(this.lives - 1);
    }

    public int getScore() {
        return this.score;
    }

    public void setScore(int score) {
        this.score = Math.max(0, score);
    }

    public int getKillStreak() {
        return this.killStreak;
    }

    public void setKillStreak(int killStreak) {
        this.killStreak = Math.max(0, killStreak);
    }

    public long getKillStreakDecay() {
        return this.killStreakDecay;
    }

    public void setKillStreakDecay(long killStreakDecay) {
        this.killStreakDecay = killStreakDecay;
    }

    public int getKills() {
        return this.kills;
    }

    public void setKills(int kills) {
        this.kills = Math.max(0, kills);
    }

    public void addKill() {
        this.kills++;
    }

    public boolean isTeleporting() {
        return this.teleporting;
    }

    public void setTeleporting(boolean teleporting) {
        this.teleporting = teleporting;
    }
}

package su.nightexpress.dungeons.api.dungeon;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.dungeons.api.compat.BoardPlugin;
import su.nightexpress.dungeons.api.compat.GodPlugin;
import su.nightexpress.dungeons.api.type.GameState;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface DungeonPlayer {

    void tick();

    void revive();

    /**
     * Moves the player, asynchronously.
     * <p>
     * On Folia a cross-region or cross-world move cannot be done synchronously at all, so this is backed by
     * {@code Entity#teleportAsync}. The returned future completes on the player's own scheduler once the
     * move has finished, which means it is safe to mutate the player from the continuation - and it is
     * <b>not</b> safe to do so immediately after this call returns, because at that point the player has not
     * moved yet.
     *
     * @return a future completing with {@code true} if the player actually arrived.
     * @see #teleportThen(Location, Runnable)
     */
    @NotNull CompletableFuture<Boolean> teleport(@NotNull Location location);

    /**
     * Convenience form of {@link #teleport(Location)}: runs {@code onArrival} on the player's scheduler once
     * the move completes. Anything that reads or writes player state after a teleport belongs in here.
     */
    void teleportThen(@NotNull Location location, @NotNull Runnable onArrival);

    void handleDeath();

    void addBoard();

    void removeBoard();

    void updateBoard();

    void manageExternalGod(@NotNull Consumer<GodPlugin> consumer);

    void manageExternalBoard(@NotNull Consumer<BoardPlugin> consumer);

    boolean isAlive();

    boolean isDead();

    boolean isReady();

    boolean isInLobby();

    boolean isInGame();

    boolean hasExtraLives();

    long getDeathTime();

    /**
     * @return the player's position as of their last tick, or {@code null} if they have not ticked yet.
     *         Unlike {@code getPlayer().getLocation()} this is safe to read from any thread, which is what
     *         makes it usable from a dungeon's clock for a player who may be owned by a different region.
     */
    @Nullable Location getLastKnownLocation();

    @NotNull Player getPlayer();

    @NotNull Dungeon getDungeon();

    @NotNull GameState getState();

    void setState(@NotNull GameState state);

    int getLives();

    void setLives(int lives);

    void addExtraLive();

    void takeExtraLive();
}

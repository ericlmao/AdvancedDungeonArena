package su.nightexpress.dungeons.api.dungeon;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.api.mob.MobProvider;
import su.nightexpress.dungeons.api.type.GameState;
import su.nightexpress.dungeons.api.type.MobFaction;
import su.nightexpress.dungeons.nightcore.util.geodata.pos.BlockPos;

import java.util.Set;
import java.util.UUID;

public interface Dungeon {

    @NonNull World getWorld();

    @NonNull String getId();

    @NonNull GameState getState();

    void handlePlayerJoin(@NonNull DungeonPlayer player, boolean forced);

    /**
     * As {@link #handlePlayerJoin(DungeonPlayer, boolean)}, but reports when the player has physically
     * arrived in the dungeon.
     * <p>
     * Entering a dungeon teleports the player, and teleports complete asynchronously - the player is still
     * standing wherever they were when {@code handlePlayerJoin} returns. {@code onArrival} runs on the
     * player's own scheduler once the move has landed and their state has been set up, which is the first
     * point at which reading their position or inventory tells you anything about the dungeon.
     *
     * @param onArrival runs after arrival; not run at all if the player leaves mid-teleport.
     */
    void handlePlayerJoin(@NonNull DungeonPlayer player, boolean forced, @NonNull Runnable onArrival);

    void handlePlayerLeave(@NonNull DungeonPlayer player);


    boolean contains(@NonNull Entity entity);

    boolean contains(@NonNull Location location);

    boolean contains(@NonNull Block block);

    boolean contains(@NonNull BlockPos blockPos);



    boolean hasPlayer(@NonNull UUID playerId);

    boolean hasAlivePlayers();

    int countPlayers();

    int countAlivePlayers();

    int countDeadPlayers();

    @NonNull DungeonPlayer getRandomAlivePlayer();

    @Nullable DungeonPlayer getPlayer(@NonNull UUID playerId);

    @NonNull Set<? extends DungeonPlayer> getPlayers();

    @NonNull Set<? extends DungeonPlayer> getAlivePlayers();

    @NonNull Set<? extends DungeonPlayer> getDeadPlayers();

//    @Nullable DungeonPlayer getPlayer(@NonNull UUID playerId);
//
//    boolean isPlaying(@NonNull Player player);
//
//    boolean isPlaying(@NonNull UUID playerId);

    long getTickCount();






    void killMobs();

    boolean isAllyMob(@NonNull LivingEntity entity);

    boolean isEnemyMob(@NonNull LivingEntity entity);

    boolean hasAllyMobs();

    boolean hasEnemyMobs();

    boolean hasMobsOfFaction(@NonNull MobFaction faction);

    @Nullable MobFaction getMobFaction(@NonNull LivingEntity entity);

    void eliminateMob(@NonNull DungeonEntity mob);

    void spawnMob(@NonNull MobProvider provider, @NonNull String mobId, @NonNull MobFaction faction, @NonNull Location location, int level);

    void spawnMob(@NonNull MobProvider provider, @NonNull String mobId, @NonNull MobFaction faction, @NonNull DungeonSpawner spawner, int level, int amount);

    void addMob(@NonNull DungeonEntity mob);

    void removeMob(@NonNull DungeonEntity mob);

    void removeMob(@NonNull UUID mobId);

    boolean hasMob(@NonNull UUID mobId);

    @Nullable DungeonEntity getMob(@NonNull LivingEntity entity);

    @Nullable DungeonEntity getMobById(@NonNull UUID mobId);

    @NonNull Set<? extends DungeonEntity> getAllyMobs();

    @NonNull Set<? extends DungeonEntity> getEnemyMobs();

    @NonNull Set<? extends DungeonEntity> getMobs();

    @NonNull Set<? extends DungeonEntity> getMobs(@NonNull MobFaction faction);
}

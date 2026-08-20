package su.nightexpress.dungeons.dungeon.player;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.api.dungeon.DungeonPlayer;
import su.nightexpress.dungeons.config.Config;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.nightcore.util.EntityUtil;
import su.nightexpress.dungeons.nightcore.util.Players;
import su.nightexpress.dungeons.nightcore.util.geodata.pos.ExactPos;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerSnapshot {

    // Written from join/leave on whichever region thread owns the joining player - concurrent by necessity.
    private static final Map<UUID, PlayerSnapshot> SNAPSHOTS = new ConcurrentHashMap<>();

    private final String                   worldName;
    private final ExactPos                 blockPos;
    private final int                      foodLevel;
    private final float                    saturation;
    private final float                    exhaustion;
    private final double                   health;
    private final ItemStack[]              inventory;
    private final ItemStack[]              armor;
    private final Collection<PotionEffect> effects;
    private final GameMode                 gameMode;
    private final List<ItemStack>          confiscate;

    PlayerSnapshot(@NonNull Player player) {
        this.worldName = player.getWorld().getName();
        this.blockPos = ExactPos.from(player.getLocation());
        this.foodLevel = player.getFoodLevel();
        this.saturation = player.getSaturation();
        this.exhaustion = player.getExhaustion();
        this.health = player.getHealth();
        this.inventory = player.getInventory().getContents();
        this.armor = player.getInventory().getArmorContents();
        this.effects = player.getActivePotionEffects();
        this.gameMode = player.getGameMode();
        this.confiscate = new ArrayList<>();
    }

    @Nullable
    public static PlayerSnapshot get(@NonNull Player player) {
        return SNAPSHOTS.get(player.getUniqueId());
    }

    @NonNull
    public static PlayerSnapshot doSnapshot(@NonNull Player player) {
        PlayerSnapshot snapshot = new PlayerSnapshot(player);
        SNAPSHOTS.put(player.getUniqueId(), snapshot);
        return snapshot;
    }

    public static void clear(@NonNull Player player) {
        //player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setGliding(false);
        player.setSneaking(false);
        player.setSprinting(false);
        player.setFoodLevel(20);
        player.setSaturation(20F);
        player.setExhaustion(0F);
        player.setHealth(EntityUtil.getAttribute(player, Attribute.MAX_HEALTH));
        player.setFireTicks(0);
        player.leaveVehicle();
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
    }

    /**
     * Puts the player back the way they were before entering the dungeon.
     * <p>
     * This is a <b>cross-world</b> teleport followed by a dozen player mutations, which makes it the single
     * most dangerous sequence in the plugin on Folia: the player changes owning region part-way through, so
     * running the restore body against the old region is exactly how inventories get duplicated or lost.
     * Everything after the move therefore happens in the teleport continuation, on the player's scheduler.
     *
     * @return a future completing once the player has been fully restored, so that callers can order their
     *         own follow-up work (rewards, refunds, exit commands) after it.
     */
    @NonNull
    public static CompletableFuture<Void> restore(@NonNull DungeonPlayer gamer) {
        Player player = gamer.getPlayer();
        PlayerSnapshot snapshot = SNAPSHOTS.remove(player.getUniqueId());
        if (snapshot == null) return CompletableFuture.completedFuture(null);

        DungeonInstance arena = (DungeonInstance) gamer.getDungeon();

        World world = Bukkit.getWorld(snapshot.getWorldName());
        if (world == null) world = Bukkit.getWorlds().getFirst();

        // Shutdown path. Once the plugin is disabled no scheduler will ever run our continuation, so a purely
        // asynchronous restore would silently drop every player's inventory on /reload or server stop. Apply
        // the state inline instead and skip the move - getting the items back matters, the position does not.
        if (!arena.getPlugin().isEnabled()) {
            applyState(player, snapshot, arena);
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> restored = new CompletableFuture<>();

        gamer.teleportThen(snapshot.getBlockPos().toLocation(world), () -> {
            applyState(player, snapshot, arena);
            restored.complete(null);
        });

        return restored;
    }

    private static void applyState(@NonNull Player player, @NonNull PlayerSnapshot snapshot, @NonNull DungeonInstance arena) {
        player.setFoodLevel(snapshot.getFoodLevel());
        player.setSaturation(snapshot.getSaturation());
        player.setExhaustion(snapshot.getExhaustion());
        player.setHealth(Math.min(EntityUtil.getAttribute(player, Attribute.MAX_HEALTH), snapshot.getHealth()));
        player.setGameMode(snapshot.getGameMode());

        if (player.getGameMode() == GameMode.CREATIVE) {
            player.setAllowFlight(true);
            player.setFlying(true);
        }
        player.getActivePotionEffects().stream().map(PotionEffect::getType).forEach(player::removePotionEffect);
        player.addPotionEffects(snapshot.getPotionEffects());

        // Return player inventory before the game
        if (arena.getConfig().gameSettings().isKitsEnabled() || Config.DUNGEON_ALWAYS_RESTORE_INVENTORY.get()) {
            player.getInventory().setContents(snapshot.getInventory());
            player.getInventory().setArmorContents(snapshot.getArmor());
        }
        else {
            snapshot.getConfiscate().forEach(item -> Players.addItem(player, item));
        }
    }

    @NonNull
    public String getWorldName() {
        return this.worldName;
    }

    @NonNull
    public ExactPos getBlockPos() {
        return this.blockPos;
    }

    public int getFoodLevel() {
        return foodLevel;
    }

    public float getSaturation() {
        return saturation;
    }

    public float getExhaustion() {
        return exhaustion;
    }

    public double getHealth() {
        return health;
    }

    @NonNull
    public ItemStack[] getInventory() {
        return this.inventory;
    }

    public ItemStack[] getArmor() {
        return this.armor;
    }

    @NonNull
    public Collection<PotionEffect> getPotionEffects() {
        return this.effects;
    }

    @NonNull
    public GameMode getGameMode() {
        return this.gameMode;
    }

    @NonNull
    public List<ItemStack> getConfiscate() {
        return confiscate;
    }
}

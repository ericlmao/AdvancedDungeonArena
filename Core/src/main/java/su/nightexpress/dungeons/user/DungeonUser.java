package su.nightexpress.dungeons.user;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.dungeons.api.dungeon.Dungeon;
import su.nightexpress.dungeons.kit.impl.Kit;
import su.nightexpress.dungeons.nightcore.userdata.AbstractUser;
import su.nightexpress.dungeons.nightcore.util.TimeUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DungeonUser extends AbstractUser {

    // Both collections are written from region threads (a kit purchase, a dungeon entrance) and read from
    // the async save and synchronization tasks, which serialize them to JSON while the owning player is
    // still playing. Plain HashMap/HashSet resize under a concurrent read is how you get a torn payload or
    // a spinning save thread, so both are concurrent.
    private final Set<String>       purchasedKits;
    private final Map<String, Long> cooldownMap;

    // TODO Dungeon Keys like crate keys

    @NotNull
    public static DungeonUser create(@NotNull UUID uuid, @NotNull String name) {
        long dateCreated = System.currentTimeMillis();
        long lastLogin = System.currentTimeMillis();

        Set<String> kits = new HashSet<>();
        Map<String, Long> cooldownMap = new HashMap<>();

        return new DungeonUser(uuid, name, dateCreated, lastLogin, kits, cooldownMap);
    }

    public DungeonUser(@NotNull UUID uuid,
                       @NotNull String name,
                       long dateCreated,
                       long lastLogin,

                       @NotNull Set<String> purchasedKits,
                       @NotNull Map<String, Long> cooldownMap) {
        super(uuid, name, dateCreated, lastLogin);

        this.purchasedKits = ConcurrentHashMap.newKeySet();
        this.purchasedKits.addAll(purchasedKits);
        this.cooldownMap = new ConcurrentHashMap<>(cooldownMap);
        this.cooldownMap.values().removeIf(TimeUtil::isPassed);
    }

//    public boolean hasPurchasedKits() {
//        return !this.purchasedKits.isEmpty();
//    }

    @NotNull
    public Set<String> getPurchasedKits() {
        return this.purchasedKits;
    }

    public boolean addKit(@NotNull Kit kit) {
        return this.addKit(kit.getId());
    }

    public boolean addKit(@NotNull String kit) {
        return this.purchasedKits.add(kit.toLowerCase());
    }

    public boolean hasKit(@NotNull Kit kit) {
        return this.hasKit(kit.getId());
    }

    public boolean hasKit(@NotNull String kit) {
        return this.purchasedKits.contains(kit.toLowerCase());
    }

    public boolean removeKit(@NotNull Kit kit) {
        return this.removeKit(kit.getId());
    }

    public boolean removeKit(@NotNull String kit) {
        return this.purchasedKits.remove(kit.toLowerCase());
    }



    @NotNull
    public Map<String, Long> getCooldownMap() {
        this.cooldownMap.values().removeIf(date -> System.currentTimeMillis() > date);
        return this.cooldownMap;
    }

    public boolean isOnCooldown(@NotNull Dungeon arena) {
        return this.isOnCooldown(arena.getId());
    }

    public boolean isOnCooldown(@NotNull String arenaId) {
        return this.getArenaCooldown(arenaId) > System.currentTimeMillis();
    }

    public long getArenaCooldown(@NotNull Dungeon arena) {
        return this.getArenaCooldown(arena.getId());
    }

    public long getArenaCooldown(@NotNull String arenaId) {
        return this.getCooldownMap().getOrDefault(arenaId.toLowerCase(), 0L);
    }

    public void setArenaCooldown(@NotNull Dungeon arena, long expireDate) {
        this.setArenaCooldown(arena.getId(), expireDate);
    }

    public void setArenaCooldown(@NotNull String arenaId, long expireDate) {
        this.getCooldownMap().put(arenaId.toLowerCase(), expireDate);
    }
}

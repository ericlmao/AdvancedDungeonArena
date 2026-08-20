package su.nightexpress.dungeons.user;

import org.jspecify.annotations.NonNull;
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

    @NonNull
    public static DungeonUser create(@NonNull UUID uuid, @NonNull String name) {
        long dateCreated = System.currentTimeMillis();
        long lastLogin = System.currentTimeMillis();

        Set<String> kits = new HashSet<>();
        Map<String, Long> cooldownMap = new HashMap<>();

        return new DungeonUser(uuid, name, dateCreated, lastLogin, kits, cooldownMap);
    }

    public DungeonUser(@NonNull UUID uuid,
                       @NonNull String name,
                       long dateCreated,
                       long lastLogin,

                       @NonNull Set<String> purchasedKits,
                       @NonNull Map<String, Long> cooldownMap) {
        super(uuid, name, dateCreated, lastLogin);

        this.purchasedKits = ConcurrentHashMap.newKeySet();
        this.purchasedKits.addAll(purchasedKits);
        this.cooldownMap = new ConcurrentHashMap<>(cooldownMap);
        this.cooldownMap.values().removeIf(TimeUtil::isPassed);
    }

//    public boolean hasPurchasedKits() {
//        return !this.purchasedKits.isEmpty();
//    }

    @NonNull
    public Set<String> getPurchasedKits() {
        return this.purchasedKits;
    }

    public boolean addKit(@NonNull Kit kit) {
        return this.addKit(kit.getId());
    }

    public boolean addKit(@NonNull String kit) {
        return this.purchasedKits.add(kit.toLowerCase());
    }

    public boolean hasKit(@NonNull Kit kit) {
        return this.hasKit(kit.getId());
    }

    public boolean hasKit(@NonNull String kit) {
        return this.purchasedKits.contains(kit.toLowerCase());
    }

    public boolean removeKit(@NonNull Kit kit) {
        return this.removeKit(kit.getId());
    }

    public boolean removeKit(@NonNull String kit) {
        return this.purchasedKits.remove(kit.toLowerCase());
    }



    @NonNull
    public Map<String, Long> getCooldownMap() {
        this.cooldownMap.values().removeIf(date -> System.currentTimeMillis() > date);
        return this.cooldownMap;
    }

    public boolean isOnCooldown(@NonNull Dungeon arena) {
        return this.isOnCooldown(arena.getId());
    }

    public boolean isOnCooldown(@NonNull String arenaId) {
        return this.getArenaCooldown(arenaId) > System.currentTimeMillis();
    }

    public long getArenaCooldown(@NonNull Dungeon arena) {
        return this.getArenaCooldown(arena.getId());
    }

    public long getArenaCooldown(@NonNull String arenaId) {
        return this.getCooldownMap().getOrDefault(arenaId.toLowerCase(), 0L);
    }

    public void setArenaCooldown(@NonNull Dungeon arena, long expireDate) {
        this.setArenaCooldown(arena.getId(), expireDate);
    }

    public void setArenaCooldown(@NonNull String arenaId, long expireDate) {
        this.getCooldownMap().put(arenaId.toLowerCase(), expireDate);
    }
}

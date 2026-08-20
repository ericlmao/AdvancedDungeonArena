package su.nightexpress.dungeons.util;

import org.bukkit.Material;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.dungeons.api.dungeon.Dungeon;
import su.nightexpress.dungeons.config.Config;
import su.nightexpress.dungeons.config.Keys;
import su.nightexpress.dungeons.api.mob.MobIdentifier;
import su.nightexpress.dungeons.registry.pet.PetRegistry;
import su.nightexpress.dungeons.dungeon.feature.KillStreak;
import su.nightexpress.dungeons.nightcore.util.BukkitThing;
import su.nightexpress.dungeons.nightcore.util.Lists;
import su.nightexpress.dungeons.nightcore.util.PDCUtil;
import su.nightexpress.dungeons.nightcore.util.random.Rnd;

import java.util.*;

import static su.nightexpress.dungeons.nightcore.util.text.night.wrapper.TagWrappers.*;
import static su.nightexpress.dungeons.Placeholders.*;

public class MobUitls {

    private static final String SPAWN_EGG_SUFFIX = "_SPAWN_EGG";

    public static boolean isPet(@NotNull LivingEntity entity) {
        return PetRegistry.getProviders().stream().anyMatch(provider -> provider.isPet(entity));
    }

    @NotNull
    public static Map<EntityType, MobIdentifier> getDefaultEggAllies() {
        // Mobs are provided by MythicMobs only, so there is no sane cross-server default here.
        // Server owners have to map spawn eggs to their own MythicMobs mob ids.
        return new HashMap<>();
    }

    /**
     * Resolves the entity type a spawn egg item would summon.
     * Replaces the former NMS-based lookup, since only the Bukkit API is needed for it.
     */
    @Nullable
    public static EntityType getSpawnEggType(@NotNull ItemStack itemStack) {
        Material material = itemStack.getType();
        String name = material.name();
        if (!name.endsWith(SPAWN_EGG_SUFFIX)) return null;

        // Spawn eggs may carry an overridden entity via the 'entity_data' component.
        ItemMeta meta = itemStack.getItemMeta();
        if (meta instanceof SpawnEggMeta spawnEggMeta) {
            EntitySnapshot snapshot = spawnEggMeta.getSpawnedEntity();
            if (snapshot != null) return snapshot.getEntityType();
        }

        return BukkitThing.getEntityType(name.substring(0, name.length() - SPAWN_EGG_SUFFIX.length()).toLowerCase(Locale.ROOT));
    }

    @NotNull
    public static Map<String, KillStreak> getDefaultKillStreaks() {
        Map<String, KillStreak> map = new HashMap<>();

        // Was MessageTags.OUTPUT.wrap(10, 50) - the legacy lang system's bracket-data prefix.
        String title = "[type=\"title\",title_times=\"10:50:20\"]";
        String pentaText = title + LIGHT_CYAN.wrap(BOLD.wrap("Penta Kill!")) + BR + LIGHT_PURPLE.wrap("(+50$)");
        String text15 = title + YELLOW.wrap(BOLD.wrap("x" + GENERIC_AMOUNT + " Kill!")) + BR + LIGHT_YELLOW.wrap("(Heal)");

        map.put("5", new KillStreak("5", 5, false, pentaText, Lists.newList("eco give " + PLAYER_NAME + " 50")));
        map.put("15", new KillStreak("15", 15, false, text15, Lists.newList("heal " + PLAYER_NAME)));

        return map;
    }

    @Nullable
    public static MobIdentifier getEggAllyIdentifier(@NotNull EntityType entityType) {
        return Config.MOBS_EGG_ALLIES.get().get(entityType);
    }

    public static double getRandomSpawnOffset() {
        double origin = Config.MOBS_SPAWN_OFFSET.get();
        if (origin == 0D) return origin;

        double random = Rnd.getDouble(origin);
        if (Rnd.nextBoolean()) random = -random;

        return random;
    }

    public static boolean isExternalAlly(@NotNull MobIdentifier identifier) {
        return Config.MOBS_ALLIES_EXTERNAL.get().contains(identifier);
    }

    public static void setDungeonId(@NotNull LivingEntity entity, @NotNull Dungeon dungeon) {
        PDCUtil.set(entity, Keys.mobDungeonId, dungeon.getId());
    }

    @Nullable
    public static String getDungeonId(@NotNull LivingEntity entity) {
        return PDCUtil.getString(entity, Keys.mobDungeonId).orElse(null);
    }
}

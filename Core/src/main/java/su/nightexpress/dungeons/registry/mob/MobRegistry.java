package su.nightexpress.dungeons.registry.mob;

import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.api.mob.MobProvider;
import su.nightexpress.dungeons.hook.HookId;
import su.nightexpress.dungeons.registry.mob.provider.MythicMobProvider;
import su.nightexpress.dungeons.nightcore.util.Plugins;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MobRegistry {

    private static final Map<String, MobProvider> BY_ID_MAP = new HashMap<>();

    private static DungeonPlugin plugin;

    public static void load(@NonNull DungeonPlugin dungeonPlugin) {
        plugin = dungeonPlugin;

        // MythicMobs is the one and only mob engine supported by the plugin.
        if (Plugins.isInstalled(HookId.MYTHIC_MOBS)) {
            register(new MythicMobProvider());
        }
        else {
            plugin.warn("=".repeat(40));
            plugin.warn(HookId.MYTHIC_MOBS + " is not installed!");
            plugin.warn("It is the only supported mob engine, so no mobs will be available in dungeons.");
            plugin.warn("Install " + HookId.MYTHIC_MOBS + " to be able to spawn mobs.");
            plugin.warn("=".repeat(40));
        }
    }

    public static void clear() {
        BY_ID_MAP.clear();
        plugin = null;
    }

    public static void register(@NonNull MobProvider provider) {
        BY_ID_MAP.put(provider.getName(), provider);
        plugin.info("Registered mob provider: " + provider.getName());
    }

    public static boolean hasProviders() {
        return !BY_ID_MAP.isEmpty();
    }

    @Nullable
    public static MobProvider getProviderByName(@NonNull String name) {
        return BY_ID_MAP.get(name.toLowerCase());
    }

    @NonNull
    public static Map<String, MobProvider> getProviderByIdMap() {
        return BY_ID_MAP;
    }

    @NonNull
    public static Set<MobProvider> getProviders() {
        return new HashSet<>(BY_ID_MAP.values());
    }

    @Nullable
    public static MobProvider getProvider(@NonNull LivingEntity entity) {
        return getProviders().stream().filter(provider -> provider.isProducedBy(entity)).findFirst().orElse(null);
    }
}

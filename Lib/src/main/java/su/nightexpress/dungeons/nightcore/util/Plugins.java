package su.nightexpress.dungeons.nightcore.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

public class Plugins {

    public static final String VAULT           = "Vault";
    public static final String PLACEHOLDER_API = "PlaceholderAPI";
    public static final String FLOODGATE       = "floodgate";

    private static boolean hasFloodgate;
    private static boolean hasPlaceholderAPI;

    private Plugins() {
    }

    /**
     * Cached soft-dependency probe. Upstream ran this from the NightCore plugin's {@code onInit()};
     * {@code CoreBootstrap} calls it now.
     */
    public static void detectPlugins() {
        hasFloodgate = isInstalled(FLOODGATE);
        hasPlaceholderAPI = isInstalled(PLACEHOLDER_API);
    }

    public static boolean isInstalled(@NonNull String pluginName) {
        return Bukkit.getPluginManager().getPlugin(pluginName) != null;
    }

    public static boolean isLoaded(@NonNull String pluginName) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        return plugin != null && plugin.isEnabled();
    }

    public static boolean hasPlaceholderAPI() {
        return hasPlaceholderAPI;
    }

    public static boolean hasVault() {
        return isInstalled(VAULT);
    }

    public static boolean hasFloodgate() {
        return hasFloodgate;
    }
}

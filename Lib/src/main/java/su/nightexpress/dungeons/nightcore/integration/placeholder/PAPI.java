package su.nightexpress.dungeons.nightcore.integration.placeholder;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import me.clip.placeholderapi.PlaceholderAPI;
import su.nightexpress.dungeons.nightcore.util.Reflex;

/**
 * Slimmed version of nightcore's PAPI helper: only the two substitution entry points are vendored.
 * The {@code PluginExpansion}/{@code PlaceholderRegistry} machinery is not - this plugin registers its
 * own expansion in {@code hook.impl.PlaceholderHook}.
 */
public class PAPI {

    public static final String NAME = "PlaceholderAPI";

    private static final boolean PRESENT = Reflex.classExists("me.clip.placeholderapi.PlaceholderAPI");

    private PAPI() {
    }

    public static boolean isPresent() {
        return PRESENT;
    }

    @NonNull
    public static String setPlaceholders(@Nullable Player player, @NonNull String string) {
        return isPresent() ? PlaceholderAPI.setPlaceholders(player, string) : string;
    }

    @NonNull
    public static String setBracketPlaceholders(@Nullable Player player, @NonNull String string) {
        return isPresent() ? PlaceholderAPI.setBracketPlaceholders(player, string) : string;
    }
}

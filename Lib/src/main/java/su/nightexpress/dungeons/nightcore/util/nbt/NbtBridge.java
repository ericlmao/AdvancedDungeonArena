package su.nightexpress.dungeons.nightcore.util.nbt;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Static holder for the {@link NbtProvider} implementation, installed by the plugin during startup.
 */
public class NbtBridge {

    private static NbtProvider provider;

    private NbtBridge() {
    }

    public static void register(@NonNull NbtProvider nbtProvider) {
        provider = nbtProvider;
    }

    public static void clear() {
        provider = null;
    }

    public static boolean isAvailable() {
        return provider != null;
    }

    /**
     * The data version of the running server. Bukkit exposes this directly, so upstream's
     * {@code Version.getCurrent().getDataVersion()} table is not needed.
     */
    public static int currentDataVersion() {
        return Bukkit.getUnsafe().getDataVersion();
    }

    @Nullable
    public static String toTagString(@NonNull ItemStack itemStack) {
        return provider == null ? null : provider.toTagString(itemStack);
    }

    @Nullable
    public static ItemStack fromTagString(@NonNull String tagString, int sourceDataVersion) {
        return provider == null ? null : provider.fromTagString(tagString, sourceDataVersion);
    }
}

package su.nightexpress.dungeons.nightcore.util.nbt;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Version-specific ItemStack &lt;-&gt; SNBT bridge.
 * <p>
 * Upstream implemented this with raw reflection against obfuscated {@code net.minecraft} members
 * ({@code NbtIo}, {@code TagParser#parseCompoundFully}, and a hardcoded {@code References.ITEM_STACK}
 * field name that had to be edited every Minecraft release). Here it is an SPI implemented by the
 * mojang-mapped {@code MC_1_21_11} module, so there is no reflection and no obfuscated names.
 * <p>
 * The on-disk format (SNBT string + {@code DataVersion} int) is deliberately unchanged so existing
 * kit inventories and configured reward/loot items keep loading.
 */
public interface NbtProvider {

    /**
     * Serialises an ItemStack to the SNBT form written by {@code ItemTag}/{@code ItemNbt}.
     */
    @NonNull
    String toTagString(@NonNull ItemStack itemStack);

    /**
     * Parses SNBT back into an ItemStack, running the vanilla DataFixer from {@code sourceDataVersion}
     * up to the running server's data version.
     *
     * @param sourceDataVersion data version the tag was written with, or a non-positive value to skip fixing.
     */
    @Nullable
    ItemStack fromTagString(@NonNull String tagString, int sourceDataVersion);
}

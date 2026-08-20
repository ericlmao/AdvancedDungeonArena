package su.nightexpress.dungeons.nightcore.util;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import su.nightexpress.dungeons.nightcore.util.nbt.NbtBridge;

/**
 * SNBT (de)serialisation for ItemStacks, preserving the upstream on-disk format.
 * <p>
 * The reflection-based {@code compress}/{@code decompress} pair (BigInteger-base-32 of a gzip NBT
 * stream) is gone - it had no call sites in this plugin. Everything now goes through
 * {@link NbtBridge}.
 */
public class ItemNbt {

    private ItemNbt() {
    }

    /**
     * @return the item's SNBT compound, or {@code null} for air/empty stacks.
     */
    @Nullable
    public static String getTagString(@NonNull ItemStack item) {
        if (item.getType().isAir() || item.getAmount() <= 0) return null;

        return NbtBridge.toTagString(item);
    }

    @Nullable
    public static ItemStack fromTagString(@NonNull String tagString) {
        return new ItemTag(tagString, NbtBridge.currentDataVersion()).getItemStack();
    }

    @Nullable
    public static ItemStack fromTag(@NonNull ItemTag itemTag) {
        return itemTag.getItemStack();
    }

    @Nullable
    public static ItemTag getTag(@NonNull ItemStack item) {
        try {
            return ItemTag.of(item);
        }
        catch (IllegalStateException exception) {
            return null;
        }
    }
}

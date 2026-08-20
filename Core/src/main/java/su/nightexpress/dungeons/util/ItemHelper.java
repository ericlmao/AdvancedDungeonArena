package su.nightexpress.dungeons.util;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.nightcore.bridge.item.AdaptedItem;
import su.nightexpress.dungeons.nightcore.bridge.item.ItemAdapter;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.integration.item.ItemBridge;
import su.nightexpress.dungeons.nightcore.integration.item.adapter.IdentifiableItemAdapter;
import su.nightexpress.dungeons.nightcore.integration.item.data.ItemIdData;
import su.nightexpress.dungeons.nightcore.integration.item.impl.AdaptedCustomStack;
import su.nightexpress.dungeons.nightcore.integration.item.impl.AdaptedItemStack;
import su.nightexpress.dungeons.nightcore.integration.item.impl.AdaptedVanillaStack;
import su.nightexpress.dungeons.nightcore.util.ItemTag;

import java.util.Objects;
import java.util.Optional;

public class ItemHelper {

    @NonNull
    public static Optional<AdaptedItem> read(@NonNull FileConfig config, @NonNull String path) {
        String oldType = config.getString(path + ".Type");
        if (oldType != null) {
            AdaptedItem adaptedItem = null;

            if (oldType.equalsIgnoreCase("vanilla")) {
                if (config.contains(path + ".Tag")) {
                    ItemTag tag = ItemTag.read(config, path + ".Tag");
                    adaptedItem = new AdaptedVanillaStack(tag);
                    config.remove(path + ".Tag");
                }
            }
            else if (oldType.equalsIgnoreCase("custom")) {
                String handlerName = config.getString(path + ".Handler", "null");
                String itemId = config.getString(path + ".ItemId", "null");
                int amount = config.getInt(path + ".Amount");

                config.remove(path + ".Handler");
                config.remove(path + ".ItemId");
                config.remove(path + ".Amount");

                ItemAdapter<?> adapter = ItemBridge.getAdapter(handlerName);
                if (adapter instanceof IdentifiableItemAdapter identifiableItemAdapter) {
                    adaptedItem = new AdaptedCustomStack(identifiableItemAdapter, new ItemIdData(itemId, amount));
                }
                else if (adapter == null) {
                    config.set(path + ".Provider", handlerName);
                    config.set(path + ".Data.ID", itemId);
                    config.set(path + ".Data.Amount", amount);
                }
            }

            config.remove(path + ".Type");

            if (adaptedItem != null) {
                config.set(path, adaptedItem);
            }
        }

        return Optional.ofNullable(AdaptedItemStack.read(config, path));
    }

    @Nullable
    public static ItemStack toItemStack(@NonNull AdaptedItem item) {
        return item.itemStack().orElse(null);
    }

    public static boolean isCustom(@NonNull ItemStack itemStack) {
        ItemAdapter<?> adapter = ItemBridge.getAdapter(itemStack);
        return adapter != null && !adapter.isVanilla();
    }

    @NonNull
    public static AdaptedItem vanilla(@NonNull ItemStack itemStack) {
        return AdaptedVanillaStack.of(itemStack);
    }

    @NonNull
    public static AdaptedItem adapt(@NonNull ItemStack itemStack) {
        ItemAdapter<?> adapter = ItemBridge.getAdapterOrVanilla(itemStack);
        AdaptedItem item = adapter.adapt(itemStack).orElse(null);
        return Objects.requireNonNullElseGet(item, () -> vanilla(itemStack));
    }

    @NonNull
    public static AdaptedItem adapt(@NonNull ItemStack itemStack, boolean allowCustoms) {
        return allowCustoms ? adapt(itemStack) : vanilla(itemStack);
    }
}

package su.nightexpress.dungeons.dungeon.lootchest;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.util.ItemHelper;
import su.nightexpress.dungeons.nightcore.bridge.item.AdaptedItem;
import su.nightexpress.dungeons.nightcore.config.ConfigValue;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.config.Writeable;

public record LootItem(@NonNull String id, double weight, @NonNull AdaptedItem item) implements Writeable {

    public LootItem {
        id = id.toLowerCase();
    }

    @NonNull
    public static LootItem read(@NonNull FileConfig config, @NonNull String path, @NonNull String id) throws IllegalStateException {
        double weight = ConfigValue.create(path + ".Weight", 0D).read(config);
        AdaptedItem provider = ItemHelper.read(config, path + ".Item").orElseThrow(() -> new IllegalStateException("Invalid loot item")); // TODO More robust log

        return new LootItem(id, weight, provider);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".Weight", this.weight);
        config.set(path + ".Item", this.item);
    }
}

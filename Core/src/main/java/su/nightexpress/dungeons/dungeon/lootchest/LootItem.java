package su.nightexpress.dungeons.dungeon.lootchest;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.util.ItemHelper;
import su.nightexpress.dungeons.nightcore.bridge.item.AdaptedItem;
import su.nightexpress.dungeons.nightcore.config.ConfigValue;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.config.Writeable;

public class LootItem implements Writeable {

    private final String id;
    private final double weight;
    private final AdaptedItem item;

    public LootItem(@NonNull String id, double weight, @NonNull AdaptedItem item) {
        this.id = id.toLowerCase();
        this.weight = weight;
        this.item = item;
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

    @NonNull
    public String getId() {
        return this.id;
    }

    public double getWeight() {
        return this.weight;
    }

    @NonNull
    public AdaptedItem getItem() {
        return this.item;
    }
}

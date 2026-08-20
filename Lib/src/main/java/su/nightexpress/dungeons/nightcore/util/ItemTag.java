package su.nightexpress.dungeons.nightcore.util;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.nightcore.config.ConfigValue;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.config.Writeable;
import su.nightexpress.dungeons.nightcore.util.nbt.NbtBridge;

public class ItemTag implements Writeable {

    private static final String EMPTY = "{}";

    private final String tag;
    private final int    dataVersion;

    public ItemTag(@NonNull String tag, int dataVersion) {
        this.tag = tag;
        this.dataVersion = dataVersion;
    }

    @NonNull
    public static ItemTag read(@NonNull FileConfig config, @NonNull String path) {
        String value = ConfigValue.create(path + ".Value", EMPTY).read(config);
        int dataVersion = ConfigValue.create(path + ".DataVersion", -1).read(config);

        return new ItemTag(value, dataVersion);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".Value", this.tag);
        config.set(path + ".DataVersion", this.dataVersion);
    }

    @NonNull
    public static ItemTag of(@NonNull ItemStack itemStack) {
        String tag = NbtBridge.toTagString(itemStack);
        if (tag == null) throw new IllegalStateException("No NBT provider registered.");

        return new ItemTag(tag, NbtBridge.currentDataVersion());
    }

    @NonNull
    public static String getTagString(@NonNull ItemStack itemStack) {
        return of(itemStack).getTag();
    }

    @NonNull
    public static String getTagStringEncoded(@NonNull ItemStack itemStack) {
        return Strings.toBase64(getTagString(itemStack));
    }

    @Nullable
    public ItemStack getItemStack() {
        if (this.isEmpty()) return null;

        return NbtBridge.fromTagString(this.tag, this.dataVersion);
    }

    public boolean isEmpty() {
        return this.tag.isBlank() || this.tag.equalsIgnoreCase(EMPTY);
    }

    @NonNull
    public String getTag() {
        return this.tag;
    }

    public int getDataVersion() {
        return this.dataVersion;
    }
}

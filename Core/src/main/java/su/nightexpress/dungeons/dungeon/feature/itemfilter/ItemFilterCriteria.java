package su.nightexpress.dungeons.dungeon.feature.itemfilter;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.nightcore.config.ConfigValue;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.config.Writeable;
import su.nightexpress.dungeons.nightcore.util.BukkitThing;
import su.nightexpress.dungeons.nightcore.util.ItemUtil;
import su.nightexpress.dungeons.nightcore.util.Lists;
import su.nightexpress.dungeons.nightcore.util.text.night.NightMessage;

import java.util.Collections;
import java.util.List;

public record ItemFilterCriteria(@NonNull List<String> names, @NonNull List<String> lores, @NonNull List<Material> materials) implements Writeable {

    @NonNull
    public static ItemFilterCriteria read(@NonNull FileConfig config, @NonNull String path) {
        List<Material> materials = Lists.modify(config.getStringList(path + ".Materials"), BukkitThing::getMaterial);
        List<String> names = ConfigValue.create(path + ".Names", Collections.emptyList()).read(config);
        List<String> lores = ConfigValue.create(path + ".Lores", Collections.emptyList()).read(config);

        return new ItemFilterCriteria(names, lores, materials);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".Materials", Lists.modify(this.materials, BukkitThing::getAsString));
        config.set(path + ".Names", this.names);
        config.set(path + ".Lores", this.lores);
    }

    public boolean matches(@NonNull ItemStack itemStack) {
        Material material = itemStack.getType();
        if (!this.materials.isEmpty() && !this.materials.contains(material)) return false;

        String name = NightMessage.stripTags(ItemUtil.getNameSerialized(itemStack));
        if (!this.names.isEmpty() && this.names.stream().noneMatch(name::contains)) return false;

        String lore = NightMessage.stripTags(String.join("\n", ItemUtil.getLoreSerialized(itemStack)));
        return this.lores.isEmpty() || this.lores.stream().anyMatch(lore::contains);
    }
}

package su.nightexpress.dungeons.config;

import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.DungeonPlugin;

public class Keys {

    public static NamespacedKey dummyItem;
    public static NamespacedKey dungeonWand;
    public static NamespacedKey kitModifier;
    public static NamespacedKey kitItem;
    public static NamespacedKey mobFaction;
    public static NamespacedKey mobDungeonId;

    public static void load(@NonNull DungeonPlugin plugin) {
        dummyItem = new NamespacedKey(plugin, "dummy_item");
        dungeonWand = new NamespacedKey(plugin, "dungeon_wand");
        kitModifier = new NamespacedKey(plugin, "kit_modifier");
        kitItem = new NamespacedKey(plugin, "kit_item");
        mobFaction = new NamespacedKey(plugin, "mob_faction");
        mobDungeonId = new NamespacedKey(plugin, "mob_dungeon_id");
    }

    public static void clear() {
        dummyItem = null;
        dungeonWand = null;
        kitModifier = null;
        kitItem = null;
        mobFaction = null;
        mobDungeonId = null;
    }
}

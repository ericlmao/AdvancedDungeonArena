package su.nightexpress.dungeons;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.data.DataHandler;
import su.nightexpress.dungeons.dungeon.DungeonManager;
import su.nightexpress.dungeons.dungeon.DungeonSetup;
import su.nightexpress.dungeons.user.UserManager;
import su.nightexpress.dungeons.kit.KitManager;
import su.nightexpress.dungeons.nms.DungeonNMS;

public class DungeonsAPI {

    private static DungeonPlugin plugin;

    static void load(@NonNull DungeonPlugin dungeonPlugin) {
        plugin = dungeonPlugin;
    }

    static void clear() {
        plugin = null;
    }

    @NonNull
    public static DungeonPlugin getPlugin() {
        return plugin;
    }

    @NonNull
    public static UserManager getUserManager() {
        return plugin.getUserManager();
    }

    @NonNull
    public static DataHandler getDataHandler() {
        return plugin.getDataHandler();
    }

    @NonNull
    public static DungeonManager getDungeonManager() {
        return plugin.getDungeonManager();
    }

    @NonNull
    public static DungeonSetup getDungeonSetup() {
        return plugin.getDungeonSetup();
    }

    @NonNull
    public static KitManager getKitManager() {
        return plugin.getKitManager();
    }

    @NonNull
    public static DungeonNMS getArenaNMS() {
        return plugin.getInternals();
    }
}

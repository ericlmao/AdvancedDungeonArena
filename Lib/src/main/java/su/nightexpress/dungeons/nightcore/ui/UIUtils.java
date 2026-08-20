package su.nightexpress.dungeons.nightcore.ui;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import su.nightexpress.dungeons.nightcore.NightPlugin;
import su.nightexpress.dungeons.nightcore.ui.menu.confirmation.ConfirmMenu;
import su.nightexpress.dungeons.nightcore.ui.menu.confirmation.Confirmation;

public class UIUtils {

    private static ConfirmMenu confirmMenu;

    private UIUtils() {
    }

    public static void load(@NonNull NightPlugin plugin) {
        confirmMenu = new ConfirmMenu(plugin);
    }

    public static void clear() {
        if (confirmMenu != null) {
            confirmMenu.clear();
            confirmMenu = null;
        }
    }

    public static void openConfirmation(@NonNull Player player, @NonNull Confirmation confirmation) {
        if (confirmMenu == null) return;

        confirmMenu.open(player, confirmation);
    }
}

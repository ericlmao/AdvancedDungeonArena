package su.nightexpress.dungeons.nightcore.ui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import su.nightexpress.dungeons.nightcore.NightPlugin;
import su.nightexpress.dungeons.nightcore.manager.AbstractListener;
import su.nightexpress.dungeons.nightcore.ui.menu.MenuRegistry;
import su.nightexpress.dungeons.nightcore.ui.menu.MenuViewer;
import su.nightexpress.dungeons.nightcore.ui.menu.click.ClickResult;

/**
 * Drives {@code ui.menu} click handling. Upstream this was registered by the NightCore plugin's
 * {@code CoreManager}; here {@code CoreBootstrap} owns it. The chat-driven {@code Dialog} handlers are
 * gone along with the dialog subsystem.
 */
public class UIListener extends AbstractListener<NightPlugin> {

    /** Upstream default of {@code CoreConfig.MENU_CLICK_COOLDOWN}, in milliseconds. */
    private static final int CLICK_COOLDOWN = 200;

    public UIListener(@NonNull NightPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        MenuRegistry.closeMenu(player);
        MenuRegistry.terminate(player);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMenuItemClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        MenuViewer viewer = MenuRegistry.getViewer(player);
        if (viewer == null) return;

        if (!viewer.canClickAgain(CLICK_COOLDOWN)) {
            event.setCancelled(true);
            return;
        }

        Inventory inventory = event.getInventory();
        ItemStack item = event.getCurrentItem();

        int slot = event.getRawSlot();
        boolean isMenu = slot < inventory.getSize();
        ClickResult result = new ClickResult(slot, item, isMenu);

        viewer.getMenu().onClick(viewer, result, event);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMenuItemDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();

        MenuViewer viewer = MenuRegistry.getViewer(player);
        if (viewer == null) return;

        viewer.getMenu().onDrag(viewer, event);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMenuClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        MenuViewer viewer = MenuRegistry.getViewer(player);
        if (viewer == null) return;

        viewer.getMenu().onClose(viewer, event);
    }
}

package su.nightexpress.dungeons.nightcore.ui.menu.item;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.nightcore.ui.menu.MenuViewer;

@Deprecated
public interface ItemClick {

    void onClick(@NonNull MenuViewer viewer, @NonNull InventoryClickEvent event);
}

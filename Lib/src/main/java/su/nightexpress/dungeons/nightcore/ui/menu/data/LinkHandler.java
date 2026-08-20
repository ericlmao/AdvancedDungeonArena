package su.nightexpress.dungeons.nightcore.ui.menu.data;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.nightcore.ui.menu.MenuViewer;

@Deprecated
public interface LinkHandler<T> {

    void handle(@NonNull MenuViewer viewer, @NonNull InventoryClickEvent event, @NonNull T obj);
}

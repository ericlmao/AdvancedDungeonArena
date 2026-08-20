package su.nightexpress.dungeons.nightcore.ui.menu.confirmation;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.nightcore.NightPlugin;
import su.nightexpress.dungeons.nightcore.config.ConfigValue;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.ui.menu.MenuViewer;
import su.nightexpress.dungeons.nightcore.ui.menu.data.ConfigBased;
import su.nightexpress.dungeons.nightcore.ui.menu.data.MenuLoader;
import su.nightexpress.dungeons.nightcore.ui.menu.item.ItemHandler;
import su.nightexpress.dungeons.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.dungeons.nightcore.util.Placeholders;
import su.nightexpress.dungeons.nightcore.util.bukkit.NightItem;

import static su.nightexpress.dungeons.nightcore.util.text.night.wrapper.TagWrappers.*;

public class ConfirmMenu extends LinkedMenu<NightPlugin, Confirmation> implements ConfigBased {

    public static final String DIR_UI    = "/ui/";
    public static final String FILE_NAME = "confirmation.yml";

    private int iconSlot;

    public ConfirmMenu(@NonNull NightPlugin plugin) {
        super(plugin, MenuType.HOPPER, BLACK.wrap("Are you sure?"));

        this.load(FileConfig.loadOrExtract(plugin, DIR_UI, FILE_NAME));
    }

    @Override
    protected void onPrepare(@NonNull MenuViewer viewer, @NonNull InventoryView view) {
        Confirmation confirmation = this.getLink(viewer);
        NightItem icon = confirmation.getIcon();
        if (icon != null) {
            this.addItem(viewer, icon.toMenuItem().setSlots(this.iconSlot).setPriority(100));
        }
    }

    @Override
    protected void onReady(@NonNull MenuViewer viewer, @NonNull Inventory inventory) {

    }

    @Override
    public void loadConfiguration(@NonNull FileConfig config, @NonNull MenuLoader loader) {
        this.iconSlot = ConfigValue.create("Settings.IconSlot", 2).read(config);

        loader.addDefaultItem(NightItem.asCustomHead(Placeholders.SKIN_WRONG_MARK)
            .setDisplayName(LIGHT_RED.wrap(BOLD.wrap("Cancel")))
            .toMenuItem()
            .setPriority(10)
            .setSlots(0)
            .setHandler(new ItemHandler("decline", (viewer, event) -> {
                this.getLink(viewer).handleReturn(viewer, event);
            }))
        );

        loader.addDefaultItem(NightItem.asCustomHead(Placeholders.SKIN_CHECK_MARK)
            .setDisplayName(LIGHT_GREEN.wrap(BOLD.wrap("Accept")))
            .toMenuItem()
            .setPriority(10)
            .setSlots(4)
            .setHandler(new ItemHandler("accept", (viewer, event) -> {
                this.getLink(viewer).handleAccept(viewer, event);
            }))
        );
    }
}

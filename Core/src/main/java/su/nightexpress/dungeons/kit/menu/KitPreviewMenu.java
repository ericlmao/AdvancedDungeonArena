package su.nightexpress.dungeons.kit.menu;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.config.Config;
import su.nightexpress.dungeons.user.DungeonUser;
import su.nightexpress.dungeons.kit.KitUtils;
import su.nightexpress.dungeons.kit.impl.Kit;
import su.nightexpress.dungeons.nightcore.config.ConfigValue;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.ui.menu.MenuViewer;
import su.nightexpress.dungeons.nightcore.ui.menu.data.ConfigBased;
import su.nightexpress.dungeons.nightcore.ui.menu.data.MenuLoader;
import su.nightexpress.dungeons.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.dungeons.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.dungeons.nightcore.util.bukkit.NightItem;

import java.util.stream.IntStream;

import static su.nightexpress.dungeons.Placeholders.KIT_NAME;
import static su.nightexpress.dungeons.nightcore.util.text.night.wrapper.TagWrappers.BLACK;

@SuppressWarnings("UnstableApiUsage")
public class KitPreviewMenu extends LinkedMenu<DungeonPlugin, KitPreviewMenu.Data> implements ConfigBased {

    public static final String FILE_NAME = "kit_preview.yml";

    private int[] itemSlots;
    private int[] armorSlots;

    public record Data(@NonNull Kit kit, @NonNull DungeonInstance dungeon){}

    public KitPreviewMenu(@NonNull DungeonPlugin plugin) {
        super(plugin, MenuType.GENERIC_9X6, BLACK.wrap("Kit Preview: " + KIT_NAME));

        this.load(FileConfig.loadOrExtract(plugin, Config.DIR_MENU, FILE_NAME));
    }

    public void open(@NonNull Player player, @NonNull Kit kit, @NonNull DungeonInstance dungeon) {
        this.open(player, new Data(kit, dungeon));
    }

    private void handleReturn(@NonNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        DungeonUser user = plugin.getUserManager().getOrFetch(player);
        Data data = this.getLink(player);

        if (!KitUtils.isRentMode() && (user.hasKit(data.kit) || !data.kit.hasCost())) {
            this.runNextTick(player, () -> plugin.getKitManager().openSelector(player, data.dungeon));
        }
        else {
            this.runNextTick(player, () -> plugin.getKitManager().openShop(player, data.dungeon));
        }
    }

    @Override
    @NonNull
    protected String getTitle(@NonNull MenuViewer viewer) {
        return this.getLink(viewer).kit.replacePlaceholders().apply(this.title);
    }

    @Override
    public void onPrepare(@NonNull MenuViewer viewer, @NonNull InventoryView view) {
        Player player = viewer.getPlayer();
        Kit kit = this.getLink(player).kit;

        ItemStack[] items = kit.getItems();
        for (int itemIndex = 0; itemIndex < Kit.INVENTORY_SIZE; itemIndex++) {
            if (itemIndex >= itemSlots.length) break;

            ItemStack itemStack = items[itemIndex];
            if (itemStack == null) continue;

            this.addItem(viewer, NightItem.fromItemStack(itemStack).toMenuItem().setSlots(this.itemSlots[itemIndex]));
        }

        int armorIndex = 0;
        for (EquipmentSlot slot : KitUtils.ARMOR_SLOTS) {
            if (armorIndex >= armorSlots.length) break;

            this.addItem(viewer, NightItem.fromItemStack(kit.getEquipment(slot)).toMenuItem().setSlots(this.armorSlots[armorIndex++]));
        }

        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
    }

    @Override
    protected void onReady(@NonNull MenuViewer viewer, @NonNull Inventory inventory) {

    }

//    @Override
//    public void onClose(@NonNull MenuViewer viewer, @NonNull InventoryCloseEvent event) {
//        this.handleReturn(viewer);
//        super.onClose(viewer, event);
//    }

    @Override
    public void loadConfiguration(@NonNull FileConfig config, @NonNull MenuLoader loader) {
        this.itemSlots = ConfigValue.create("Item_Slots",
            new int[]{45,46,47,48,49,50,51,52,53,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44}
        ).read(config);

        this.armorSlots = ConfigValue.create("Armor_Slots",
            new int[]{0,1,2,3}
        ).read(config);


        loader.addDefaultItem(NightItem.fromType(Material.BLACK_STAINED_GLASS_PANE).setHideTooltip(true).toMenuItem().setSlots(IntStream.range(9, 18).toArray()));

        loader.addDefaultItem(MenuItem.buildReturn(this, 8, (viewer, _) -> {
            this.handleReturn(viewer);
        }).setPriority(10));
    }
}

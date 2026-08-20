package su.nightexpress.dungeons.nightcore.util;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import su.nightexpress.dungeons.nightcore.util.bridge.RegistryType;

/**
 * Tooltip component hiding, on Paper's {@code TOOLTIP_DISPLAY} data component.
 * <p>
 * Replaces the {@code Software#hideComponents}/{@code getHiddenComponents} bridge methods (and, before
 * 1.21.5, the {@code ItemFlag} route).
 */
public class TooltipUtil {

    private static Set<DataComponentType> commonComponentsToHide;

    private TooltipUtil() {
    }

    /**
     * Every data component except the ones that carry the item's own presentation. Mirrors upstream's
     * {@code PaperBridge#commonComponentsToHide}.
     */
    @NonNull
    public static Set<DataComponentType> commonComponentsToHide() {
        if (commonComponentsToHide == null) {
            Set<DataComponentType> types = BukkitThing.getAll(RegistryType.Paper.DATA_COMPONENT_TYPE);
            types.remove(DataComponentTypes.LORE);
            types.remove(DataComponentTypes.ITEM_NAME);
            types.remove(DataComponentTypes.ITEM_MODEL);
            types.remove(DataComponentTypes.CUSTOM_NAME);
            types.remove(DataComponentTypes.CUSTOM_MODEL_DATA);
            types.remove(DataComponentTypes.TOOLTIP_DISPLAY);
            types.remove(DataComponentTypes.TOOLTIP_STYLE);
            commonComponentsToHide = types;
        }
        return commonComponentsToHide;
    }

    @NonNull
    public static Set<String> commonComponentNamesToHide() {
        return commonComponentsToHide().stream().map(BukkitThing::getAsString).collect(Collectors.toSet());
    }

    @NonNull
    public static Set<String> getHiddenComponents(@NonNull ItemStack itemStack) {
        TooltipDisplay tooltipDisplay = itemStack.getData(DataComponentTypes.TOOLTIP_DISPLAY);
        if (tooltipDisplay == null) return Collections.emptySet();

        return tooltipDisplay.hiddenComponents().stream().map(BukkitThing::getAsString).collect(Collectors.toSet());
    }

    public static void hideComponents(@NonNull ItemStack itemStack, @NonNull Set<String> componentNames) {
        Set<DataComponentType> componentTypes = componentNames.stream()
            .map(name -> BukkitThing.getByString(RegistryType.Paper.DATA_COMPONENT_TYPE, name))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        hideComponents0(itemStack, componentTypes);
    }

    public static void hideAllComponents(@NonNull ItemStack itemStack) {
        hideComponents0(itemStack, commonComponentsToHide());
    }

    private static void hideComponents0(@NonNull ItemStack itemStack, @NonNull Set<DataComponentType> types) {
        try {
            itemStack.setData(DataComponentTypes.TOOLTIP_DISPLAY,
                TooltipDisplay.tooltipDisplay().hiddenComponents(types).build());
        }
        catch (NoSuchElementException exception) {
            // A component type that is not tooltip-displayable; nothing to hide.
        }
    }
}

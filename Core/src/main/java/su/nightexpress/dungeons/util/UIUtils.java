package su.nightexpress.dungeons.util;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.potion.PotionEffect;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.config.Lang;
import su.nightexpress.dungeons.nightcore.bridge.currency.Currency;
import su.nightexpress.dungeons.nightcore.integration.currency.EconomyBridge;
import su.nightexpress.dungeons.nightcore.util.LangUtil;
import su.nightexpress.dungeons.nightcore.locale.entry.TextLocale;
import su.nightexpress.dungeons.nightcore.util.NumberUtil;
import su.nightexpress.dungeons.nightcore.util.placeholder.Replacer;

import static su.nightexpress.dungeons.Placeholders.*;

public class UIUtils {

//    private static ConfirmMenu confirmMenu;
//
//    public static void load(@NonNull DungeonPlugin plugin) {
//        confirmMenu = new ConfirmMenu(plugin);
//    }
//
//    public static void clear() {
//        confirmMenu.clear();
//        confirmMenu = null;
//    }
//
//    public static void openConfirmation(@NonNull Player player, @NonNull Confirmation confirmation) {
//        confirmMenu.open(player, confirmation);
//    }

    @NonNull
    public static String formatCostEntry(@NonNull String currencyId, double amount) {
        Currency currency = EconomyBridge.getCurrency(currencyId);
        if (currency == null) return currencyId;

        return currency.format(amount);
    }

    @NonNull
    public static String formatPotionEffectEntry(@NonNull PotionEffect effect) {
        return Replacer.create()
            .replace(GENERIC_NAME, LangUtil.getSerializedName(effect.getType()))
            .replace(GENERIC_AMOUNT, NumberUtil.toRoman(effect.getAmplifier() + 1))
            .apply(Lang.UI_POTION_EFFECT_ENTRY.text());
    }

    @NonNull
    public static String formatAttributeEntry(@NonNull Attribute attribute, @NonNull AttributeModifier modifier) {
        double amount = modifier.getAmount();
        boolean scalar = modifier.getOperation() == AttributeModifier.Operation.ADD_SCALAR;
        boolean negative = amount < 0D;

        TextLocale valueString;
        if (scalar) {
            valueString = negative ? Lang.UI_ATTRIBUTE_NEGATIVE_SCALAR : Lang.UI_ATTRIBUTE_POSITIVE_SCALAR;
        }
        else {
            valueString = negative ? Lang.UI_ATTRIBUTE_NEGATIVE_PLAIN : Lang.UI_ATTRIBUTE_POSITIVE_PLAIN;
        }

        return Replacer.create()
            .replace(GENERIC_NAME, () -> Lang.ATTRIBUTE.getLocalized(attribute))
            .replace(GENERIC_AMOUNT, () -> valueString.text().replace(GENERIC_VALUE, NumberUtil.format(scalar ? (amount * 100D) : amount)))
            .apply(Lang.UI_ATTRIBUTE_ENTRY.text());
    }
}

package su.nightexpress.dungeons.nightcore.util;

import org.bukkit.Material;
import org.bukkit.Translatable;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;
import org.jspecify.annotations.NonNull;

import su.nightexpress.dungeons.nightcore.util.text.night.wrapper.TagWrappers;

/**
 * Upstream routed every {@code getTranslationKey} call through the Spigot/Paper {@code Software} bridge.
 * On Paper all five types implement {@link Translatable}, so the bridge collapses to a single method.
 */
public class LangUtil {

    private LangUtil() {
    }

    @NonNull
    public static String getTranslationKey(@NonNull Material material) {
        return material.translationKey();
    }

    @NonNull
    public static String getTranslationKey(@NonNull Attribute attribute) {
        return attribute.translationKey();
    }

    @NonNull
    public static String getTranslationKey(@NonNull Enchantment enchantment) {
        return enchantment.translationKey();
    }

    @NonNull
    public static String getTranslationKey(@NonNull EntityType entityType) {
        return entityType.translationKey();
    }

    @NonNull
    public static String getTranslationKey(@NonNull PotionEffectType effectType) {
        return effectType.translationKey();
    }

    @NonNull
    public static String getSerializedName(@NonNull Material material) {
        return TagWrappers.LANG.apply(getTranslationKey(material));
    }

    @NonNull
    public static String getSerializedName(@NonNull Attribute attribute) {
        return TagWrappers.LANG.apply(getTranslationKey(attribute));
    }

    @NonNull
    public static String getSerializedName(@NonNull Enchantment enchantment) {
        return TagWrappers.LANG_OR.apply(getTranslationKey(enchantment), BukkitThing.getValue(enchantment));
    }

    @NonNull
    public static String getSerializedName(@NonNull EntityType entityType) {
        return TagWrappers.LANG.apply(getTranslationKey(entityType));
    }

    @NonNull
    public static String getSerializedName(@NonNull PotionEffectType effectType) {
        return TagWrappers.LANG.apply(getTranslationKey(effectType));
    }

    @NonNull
    public static String getEnchantmentLevelLang(int level) {
        return TagWrappers.LANG_OR.apply("enchantment.level." + level, String.valueOf(level));
    }
}

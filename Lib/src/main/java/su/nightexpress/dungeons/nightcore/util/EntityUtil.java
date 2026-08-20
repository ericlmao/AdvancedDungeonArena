package su.nightexpress.dungeons.nightcore.util;

import org.bukkit.Nameable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import net.kyori.adventure.text.Component;
import su.nightexpress.dungeons.nightcore.util.text.night.NightMessage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class EntityUtil {

    /**
     * Fake (packet-only) entity ids.
     * <p>
     * Upstream reflected into {@code net.minecraft.world.entity.Entity#ENTITY_COUNTER}. Paper exposes no
     * public equivalent, so this counts down from {@link Integer#MAX_VALUE} instead: the server allocates
     * real ids upwards from 0, so the two ranges cannot realistically meet.
     */
    private static final java.util.concurrent.atomic.AtomicInteger FAKE_ENTITY_IDS =
        new java.util.concurrent.atomic.AtomicInteger(Integer.MAX_VALUE);

    public static int nextEntityId() {
        return FAKE_ENTITY_IDS.decrementAndGet();
    }


    public static final EquipmentSlot[] EQUIPMENT_SLOTS = {EquipmentSlot.HAND, EquipmentSlot.OFF_HAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public static void setCustomName(@NonNull Entity entity, @Nullable String name) {
        setCustomName((Nameable) entity, name);
    }

    public static void setCustomName(@NonNull Entity entity, @Nullable Component name) {
        setCustomName((Nameable) entity, name);
    }

    @NonNull
    public static String getNameSerialized(@NonNull Entity entity) {
        String customName = getNameSerialized((Nameable) entity);
        if (customName != null) return customName;

        return LangUtil.getSerializedName(entity.getType());
    }

    public static void setCustomName(@NonNull Nameable nameable, @Nullable String name) {
        setCustomName(nameable, name == null ? null : NightMessage.parse(name));
    }

    public static void setCustomName(@NonNull Nameable nameable, @Nullable Component name) {
        nameable.customName(name);
    }

    @Nullable
    public static String getNameSerialized(@NonNull Nameable nameable) {
        Component customName = nameable.customName();
        return customName == null ? null : NightMessage.serialize(customName);
    }


    @Deprecated
    public static double getAttribute(@NonNull LivingEntity entity, @NonNull Attribute attribute) {
        return getAttributeValue(entity, attribute);
    }

    @Deprecated
    public static double getAttributeBase(@NonNull LivingEntity entity, @NonNull Attribute attribute) {
        return getAttributeBaseValue(entity, attribute);
    }

    public static double getAttributeValue(@NonNull LivingEntity entity, @NonNull Attribute attribute) {
        AttributeInstance instance = entity.getAttribute(attribute);
        return instance == null ? 0D : instance.getValue();
    }

    public static double getAttributeBaseValue(@NonNull LivingEntity entity, @NonNull Attribute attribute) {
        AttributeInstance instance = entity.getAttribute(attribute);
        return instance == null ? 0D : instance.getBaseValue();
    }

    public static void modifyAttribute(@NonNull LivingEntity entity, @NonNull Attribute attribute,
                                       @NonNull Consumer<AttributeInstance> consumer) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) return;

        consumer.accept(instance);
    }

    public static double getMaxHealth(@NonNull LivingEntity entity) {
        return getAttributeValue(entity, Attribute.MAX_HEALTH);
    }

    public static void addHealth(@NonNull LivingEntity entity, double amount) {
        setHealth(entity, entity.getHealth() + Math.abs(amount));
    }

    public static void removeHealth(@NonNull LivingEntity entity, double amount) {
        setHealth(entity, entity.getHealth() - Math.abs(amount));
    }

    public static void setHealth(@NonNull LivingEntity entity, double value) {
        double maxHealth = getMaxHealth(entity);
        double health = Math.clamp(value, 0, maxHealth);

        entity.setHealth(health);
    }

    @Nullable
    public static ItemStack getItemInSlot(@NonNull LivingEntity entity, @NonNull EquipmentSlot slot) {
        if (entity instanceof Player player) {
            return player.getInventory().getItem(slot);
        }

        EntityEquipment equipment = entity.getEquipment();
        return equipment == null ? null : equipment.getItem(slot);
    }

    @NonNull
    public static Map<EquipmentSlot, ItemStack> getEquippedItems(@NonNull LivingEntity entity) {
        return getEquippedItems(entity, EQUIPMENT_SLOTS);
    }

    @NonNull
    public static Map<EquipmentSlot, ItemStack> getEquippedItems(@NonNull LivingEntity entity,
                                                                 @NonNull EquipmentSlot... slots) {
        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) return Collections.emptyMap();

        Map<EquipmentSlot, ItemStack> map = new HashMap<>();
        for (EquipmentSlot slot : slots) {
            if (slot.name().equalsIgnoreCase("BODY")) continue; // from 1.20.6

            map.put(slot, equipment.getItem(slot));
        }
        return map;
    }

    @NonNull
    public static Map<EquipmentSlot, ItemStack> getEquippedHands(@NonNull LivingEntity entity) {
        return getEquippedItems(entity, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
    }

    @NonNull
    public static Map<EquipmentSlot, ItemStack> getEquippedArmor(@NonNull LivingEntity entity) {
        return getEquippedItems(entity, EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST,
            EquipmentSlot.HEAD);
    }

    @Nullable
    public static BlockFace getDirection(@NonNull Entity entity) {
        float yaw = Math.round(entity.getLocation().getYaw() / 90F);

        if ((yaw == -4.0F) || (yaw == 0.0F) || (yaw == 4.0F)) {
            return BlockFace.SOUTH;
        }
        if ((yaw == -1.0F) || (yaw == 3.0F)) {
            return BlockFace.EAST;
        }
        if ((yaw == -2.0F) || (yaw == 2.0F)) {
            return BlockFace.NORTH;
        }
        if ((yaw == -3.0F) || (yaw == 1.0F)) {
            return BlockFace.WEST;
        }
        return null;
    }
}

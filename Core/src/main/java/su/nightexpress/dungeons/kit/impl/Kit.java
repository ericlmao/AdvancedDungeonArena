package su.nightexpress.dungeons.kit.impl;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.Placeholders;
import su.nightexpress.dungeons.config.Keys;
import su.nightexpress.dungeons.config.Perms;
import su.nightexpress.dungeons.kit.KitUtils;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.integration.currency.EconomyBridge;
import su.nightexpress.dungeons.nightcore.manager.AbstractFileData;
import su.nightexpress.dungeons.nightcore.util.BukkitThing;
import su.nightexpress.dungeons.nightcore.util.ItemNbt;
import su.nightexpress.dungeons.nightcore.util.Players;
import su.nightexpress.dungeons.nightcore.util.bukkit.NightItem;

import java.io.File;
import java.util.*;
import java.util.function.UnaryOperator;

import static org.bukkit.attribute.AttributeModifier.Operation;

@SuppressWarnings("UnstableApiUsage")
public class Kit extends AbstractFileData<DungeonPlugin> {

    public static final int EFFECT_DURATION = -1;
    public static final int INVENTORY_SIZE = 36;

    private final Map<String, Double>               costMap;
    private final Map<Attribute, AttributeModifier> attributeMap;
    private final Map<EquipmentSlot, ItemStack>     equipment;
    private final ItemStack[]                       items;

    private String       name;
    private List<String> description;
    private NightItem    icon;
    private boolean permissionRequired;

    private List<String>      commands;
    private Set<PotionEffect> potionEffects;

    public Kit(@NonNull DungeonPlugin plugin, @NonNull File file) {
        super(plugin, file);
        this.costMap = new HashMap<>();
        this.attributeMap = new HashMap<>();
        this.commands = new ArrayList<>();
        this.potionEffects = new HashSet<>();
        this.equipment = new HashMap<>();
        this.items = new ItemStack[INVENTORY_SIZE];
    }

    @Override
    protected boolean onLoad(@NonNull FileConfig config) {
        this.setName(config.getString("Name", this.getId()));
        this.setDescription(config.getStringList("Description"));
        this.setPermissionRequired(config.getBoolean("Permission_Required"));
        this.setIcon(config.getCosmeticItem("Icon"));

        this.setCommands(config.getStringList("Commands"));

        for (String sId : config.getSection("Cost")) {
            double amount = config.getDouble("Cost." + sId);
            if (amount <= 0) continue;

            this.costMap.put(sId.toLowerCase(), amount);
        }

        for (String sId : config.getSection("Attributes")) {
            Attribute attribute = BukkitThing.getAttribute(sId);
            if (attribute == null) continue;

            double amount = config.getDouble("Attributes." + sId + ".Amount");
            boolean multiplier = config.getBoolean("Attributes." + sId + ".Multiplier", false);
            Operation operation = multiplier ? Operation.ADD_SCALAR : Operation.ADD_NUMBER;

            this.setAttribute(attribute, operation, amount);
        }

        for (String sId : config.getSection("Potion_Effects")) {
            PotionEffectType effectType = BukkitThing.getPotionEffect(sId);
            if (effectType == null) continue;

            int amplifier = config.getInt("Potion_Effects." + sId);
            if (amplifier <= 0) continue;

            PotionEffect effect = new PotionEffect(effectType, EFFECT_DURATION, amplifier - 1);
            this.potionEffects.add(effect);
        }

        for (int index = 0; index < INVENTORY_SIZE; index++) {
            String tag = config.getString("Inventory." + index);
            this.items[index] = this.readItem(tag);
        }

        for (EquipmentSlot slot : KitUtils.ARMOR_SLOTS) {
            String tag = config.getString("Equipment." + slot.name());
            this.setEquipment(slot, this.readItem(tag));
        }

        return true;
    }

    @Override
    protected void onSave(@NonNull FileConfig config) {
        config.set("Name", this.name);
        config.set("Description", this.description);
        config.set("Permission_Required", this.permissionRequired);
        config.set("Icon", this.getIcon());
        config.set("Commands", this.commands);

        config.remove("Cost");
        this.costMap.forEach((id, amount) -> config.set("Cost." + id, amount));

        config.remove("Attributes");
        this.attributeMap.forEach((attribute, modifier) -> {
            String path = "Attributes." + BukkitThing.toString(attribute);

            config.set(path + ".Amount", modifier.getAmount());
            config.set(path + ".Multiplier", modifier.getOperation() == Operation.ADD_SCALAR);
        });

        config.remove("Potion_Effects");
        this.potionEffects.forEach(effect -> {
            config.set("Potion_Effects." + BukkitThing.toString(effect.getType()), effect.getAmplifier() + 1);
        });

        config.remove("Equipment");
        for (EquipmentSlot slot : KitUtils.ARMOR_SLOTS) {
            ItemStack itemStack = this.getEquipment(slot);
            if (itemStack.getType().isAir()) continue;

            config.set("Equipment." + slot.name(), ItemNbt.getTagString(itemStack));
        }

        config.remove("Inventory");
        for (int index = 0; index < INVENTORY_SIZE; index++) {
            ItemStack itemStack = this.items[index];
            if (itemStack == null || itemStack.getType().isAir()) continue;

            config.set("Inventory." + index, ItemNbt.getTagString(itemStack));
        }
    }

    @NonNull
    private ItemStack readItem(@Nullable String tag) {
        ItemStack itemStack = null;

        if (tag != null && !tag.isBlank()) {
            itemStack = ItemNbt.fromTagString(tag);
        }

        return Objects.requireNonNullElseGet(itemStack, () -> new ItemStack(Material.AIR));
    }

    @NonNull
    public UnaryOperator<String> replacePlaceholders() {
        return Placeholders.KIT.replacer(this);
    }

    @NonNull
    public String getPermission() {
        return Perms.PREFIX_KIT + this.getId();
    }

    public boolean hasPermission(@NonNull Player player) {
        return !this.permissionRequired || player.hasPermission(this.getPermission());
    }

    public boolean hasCost() {
        return this.costMap.keySet().stream().anyMatch(EconomyBridge::hasCurrency);
    }

    public boolean hasAttributes() {
        return !this.attributeMap.isEmpty();
    }

    public boolean hasPotionEffects() {
        return !this.potionEffects.isEmpty();
    }

    public boolean canAfford(@NonNull Player player) {
        if (!this.hasCost()) return true;
        if (player.hasPermission(Perms.BYPASS_KIT_COST)) return true;

        return this.costMap.entrySet().stream().allMatch(entry -> EconomyBridge.hasEnough(player, entry.getKey(), entry.getValue()));
    }

    public void takeCosts(@NonNull Player player) {
        this.costMap.forEach((id, amount) -> EconomyBridge.withdraw(player, id, amount));
    }

    public void refundCosts(@NonNull Player player) {
        this.costMap.forEach((id, amount) -> EconomyBridge.deposit(player, id, amount));
    }

    public void applyPotionEffects(@NonNull Player player) {
        this.getPotionEffects().forEach(effect -> {
            if (!player.hasPotionEffect(effect.getType())) {
                player.addPotionEffect(effect);
            }
        });
    }

    public void resetPotionEffects(@NonNull Player player) {
        this.getPotionEffects().forEach(effect -> {
            player.removePotionEffect(effect.getType());
        });
    }

    public void applyAttributeModifiers(@NonNull Player player) {
        this.attributeMap.forEach((attribute, modifier) -> {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance == null) return;

            instance.addModifier(modifier);
        });
    }

    public void resetAttributeModifiers(@NonNull Player player) {
        this.attributeMap.forEach((attribute, modifier) -> {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance == null) return;

            instance.removeModifier(modifier);
        });
    }

    public void give(@NonNull Player player) {
        PlayerInventory inventory = player.getInventory();

        for (int index = 0; index < this.items.length; index++) {
            if (index >= INVENTORY_SIZE) continue;

            ItemStack item = new ItemStack(this.items[index]);
            inventory.setItem(index, KitUtils.assignKit(this, item));
        }

        this.equipment.forEach((slot, armor) -> {
            ItemStack item = new ItemStack(armor);
            inventory.setItem(slot, KitUtils.assignKit(this, item));
        });

        Players.dispatchCommands(player, this.commands);
    }

    @NonNull
    public String getName() {
        return this.name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public List<String> getDescription() {
        return this.description;
    }

    public void setDescription(@NonNull List<String> description) {
        this.description = description;
    }

    @NonNull
    public NightItem getIcon() {
        return this.icon.copy();
    }

    public void setIcon(@NonNull NightItem icon) {
        this.icon = icon.copy();
    }

    @NonNull
    public Map<String, Double> getCostMap() {
        return this.costMap;
    }

    public void setCost(@NonNull String id, double amount) {
        this.costMap.put(id.toLowerCase(), amount);
    }

    public boolean isPermissionRequired() {
        return this.permissionRequired;
    }

    public void setPermissionRequired(boolean permissionRequired) {
        this.permissionRequired = permissionRequired;
    }

    @NonNull
    public List<String> getCommands() {
        return this.commands;
    }

    public void setCommands(@NonNull List<String> commands) {
        this.commands = commands;
    }

    @NonNull
    public Map<Attribute, AttributeModifier> getAttributeMap() {
        return this.attributeMap;
    }

    public void setAttribute(@NonNull Attribute attribute, @NonNull Operation operation, double amount) {
        this.setAttribute(attribute, new AttributeModifier(Keys.kitModifier, amount, operation, EquipmentSlotGroup.ANY));
    }

    public void setAttribute(@NonNull Attribute attribute, @NonNull AttributeModifier modifier) {
        this.attributeMap.put(attribute, modifier);
    }

    @Nullable
    public AttributeModifier getAttributeModifier(@NonNull Attribute attribute) {
        return this.attributeMap.get(attribute);
    }

    @NonNull
    public Set<PotionEffect> getPotionEffects() {
        return this.potionEffects;
    }

    public void setPotionEffects(@NonNull Set<PotionEffect> potionEffects) {
        this.potionEffects = potionEffects;
    }

    @NonNull
    public Map<EquipmentSlot, ItemStack> getEquipment() {
        return this.equipment;
    }

    @NonNull
    public ItemStack getEquipment(@NonNull EquipmentSlot slot) {
        return this.equipment.computeIfAbsent(slot, k -> new ItemStack(Material.AIR));
    }

    public void setEquipment(@NonNull EquipmentSlot slot, @Nullable ItemStack item) {
        this.equipment.put(slot, Objects.requireNonNullElseGet(item, () -> new ItemStack(Material.AIR)));
    }

    @NonNull
    public ItemStack[] getItems() {
        return this.items;
    }

    public void setItems(@NonNull List<ItemStack> items) {
        this.setItems(items.toArray(new ItemStack[0]));
    }

    public void setItems(@NonNull ItemStack[] items) {
        for (int index = 0; index < INVENTORY_SIZE; index++) {
            ItemStack item = index >= items.length ? null : items[index];
            if (item == null) item = new ItemStack(Material.AIR);

            this.items[index] = item;
        }
    }
}

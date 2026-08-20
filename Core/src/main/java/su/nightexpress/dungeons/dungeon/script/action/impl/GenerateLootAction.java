package su.nightexpress.dungeons.dungeon.script.action.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.dungeon.lootchest.LootChest;
import su.nightexpress.dungeons.dungeon.script.action.Action;
import su.nightexpress.dungeons.dungeon.script.action.ActionId;
import su.nightexpress.dungeons.util.ErrorHandler;
import su.nightexpress.dungeons.nightcore.config.ConfigValue;
import su.nightexpress.dungeons.nightcore.config.FileConfig;

import java.util.List;

// The ids are a List, not the String[] the config layer hands over: an array component would give this
// record reference equality and an unreadable toString, which is exactly the trap records exist to avoid.
public record GenerateLootAction(boolean specific, @NonNull List<String> lootChestIds) implements Action {

    @NonNull
    public static GenerateLootAction load(@NonNull FileConfig config, @NonNull String path) {
        boolean specific = ConfigValue.create(path + ".Specific", false).read(config);
        String[] lootChestIds = ConfigValue.create(path + ".LootChestIds", new String[]{"null"}).read(config);

        return new GenerateLootAction(specific, List.of(lootChestIds));
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".Specific", this.specific);
        config.setStringArray(path + ".LootChestIds", this.lootChestIds.toArray(String[]::new));
    }

    @NonNull
    @Override
    public String getName() {
        return ActionId.GENERATE_LOOT;
    }

    @Override
    public void perform(@NonNull DungeonInstance dungeon, @NonNull DungeonGameEvent event) {
        if (!this.specific) {
            dungeon.refillLootChests();
            return;
        }

        for (String lootId : this.lootChestIds) {
            LootChest lootChest = dungeon.getConfig().getLootChestById(lootId);
            if (lootChest == null) {
                ErrorHandler.error("Invalid loot chest '" + lootId + "'!", this, dungeon);
                continue;
            }

            dungeon.refillLootChest(lootChest);
        }
    }
}

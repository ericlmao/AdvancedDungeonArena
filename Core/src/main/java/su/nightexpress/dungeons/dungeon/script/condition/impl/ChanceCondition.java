package su.nightexpress.dungeons.dungeon.script.condition.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.dungeon.script.condition.Condition;
import su.nightexpress.dungeons.dungeon.script.condition.ConditionId;
import su.nightexpress.dungeons.nightcore.config.ConfigValue;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.util.random.Rnd;

public record ChanceCondition(double chance) implements Condition {

    @NonNull
    public static ChanceCondition load(@NonNull FileConfig config, @NonNull String path) {
        double chance = ConfigValue.create(path + ".Chance", 50D).read(config);

        return new ChanceCondition(chance);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".Chance", this.chance);
    }

    @NonNull
    @Override
    public String getName() {
        return ConditionId.CHANCE;
    }

    @Override
    public boolean test(@NonNull DungeonInstance dungeon, @NonNull DungeonGameEvent event) {
        return Rnd.chance(this.chance);
    }
}

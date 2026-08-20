package su.nightexpress.dungeons.dungeon.script.condition.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.mob.MobIdentifier;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.dungeon.event.game.MobEvent;
import su.nightexpress.dungeons.dungeon.script.condition.Condition;
import su.nightexpress.dungeons.dungeon.script.condition.ConditionId;
import su.nightexpress.dungeons.nightcore.config.FileConfig;

public record MobIdCondition(@NonNull MobIdentifier identifier) implements Condition {

    @NonNull
    public static MobIdCondition load(@NonNull FileConfig config, @NonNull String path) {
        MobIdentifier identifier = MobIdentifier.read(config, path + ".MobId");

        return new MobIdCondition(identifier);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".MobId", this.identifier);
    }

    @NonNull
    @Override
    public String getName() {
        return ConditionId.MOB_ID;
    }

    @Override
    public boolean test(@NonNull DungeonInstance dungeon, @NonNull DungeonGameEvent event) {
        if (event instanceof MobEvent mobEvent) {
            return mobEvent.getDungeonMob().isMob(this.identifier);
        }
        return false;
    }
}

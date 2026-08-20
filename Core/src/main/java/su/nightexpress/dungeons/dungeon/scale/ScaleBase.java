package su.nightexpress.dungeons.dungeon.scale;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;

public interface ScaleBase {

    @NonNull String getName();

    double getBaseValue(@NonNull DungeonInstance instance);

    //double getScaled(@NonNull DungeonInstance instance, double original);
}

package su.nightexpress.dungeons.registry.level;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public interface LevelProvider {

    @NonNull String getName();

    int getLevel(@NonNull Player player);
}

package su.nightexpress.dungeons.api.compat;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public interface BoardPlugin {

    boolean isBoardEnabled(@NonNull Player player);

    void disableBoard(@NonNull Player player);

    void enableBoard(@NonNull Player player);
}

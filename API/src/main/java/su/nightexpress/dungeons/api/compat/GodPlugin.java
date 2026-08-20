package su.nightexpress.dungeons.api.compat;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public interface GodPlugin {

    boolean isGodEnabled(@NonNull Player player);

    void disableGod(@NonNull Player player);

    void enableGod(@NonNull Player player);
}

package su.nightexpress.dungeons.nightcore.util.sound;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

/**
 * Was {@code bridge.wrap.NightSound} upstream; moved here now that the bridge module is gone.
 */
public interface NightSound {

    boolean isSilent();

    void play(@NonNull Player player);

    void play(@NonNull Location location);

    @NonNull
    String serialize();

    @NonNull
    String getName();

    float getVolume();

    float getPitch();
}

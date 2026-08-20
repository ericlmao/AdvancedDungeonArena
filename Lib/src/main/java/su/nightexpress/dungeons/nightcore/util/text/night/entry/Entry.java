package su.nightexpress.dungeons.nightcore.util.text.night.entry;

import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;

public interface Entry {

    @NonNull
    Component toComponent();
}

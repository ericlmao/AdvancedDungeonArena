package su.nightexpress.dungeons.nightcore.util.text.night.entry;

import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;

public class KeybindEntry extends ChildEntry {

    private final String key;

    public KeybindEntry(@NonNull EntryGroup parent, @NonNull String key) {
        super(parent);
        this.key = key;
    }

    @Override
    public int textLength() {
        return 1;
    }

    @Override
    @NonNull
    public Component toComponent() {
        return Component.keybind(this.key, this.parent.style());
    }
}

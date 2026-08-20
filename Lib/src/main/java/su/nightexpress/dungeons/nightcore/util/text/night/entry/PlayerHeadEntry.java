package su.nightexpress.dungeons.nightcore.util.text.night.entry;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import org.jspecify.annotations.NonNull;

public class PlayerHeadEntry extends ChildEntry {

    private final PlayerHeadObjectContents contents;

    public PlayerHeadEntry(@NonNull EntryGroup parent, @NonNull PlayerHeadObjectContents contents) {
        super(parent);
        this.contents = contents;
    }

    @Override
    public int textLength() {
        return 1;
    }

    @Override
    @NonNull
    public Component toComponent() {
        return Component.object().style(this.parent.style()).contents(this.contents).build();
    }
}

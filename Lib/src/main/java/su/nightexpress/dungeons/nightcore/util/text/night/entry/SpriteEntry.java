package su.nightexpress.dungeons.nightcore.util.text.night.entry;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.ObjectContents;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SpriteEntry extends ChildEntry {

    private final Key atlas;
    private final Key sprite;

    public SpriteEntry(@NonNull EntryGroup parent, @Nullable Key atlas, @NonNull Key sprite) {
        super(parent);
        this.atlas = atlas;
        this.sprite = sprite;
    }

    @Override
    public int textLength() {
        return 1;
    }

    @Override
    @NonNull
    public Component toComponent() {
        return Component.object()
            .style(this.parent.style())
            .contents(this.atlas == null ? ObjectContents.sprite(this.sprite) : ObjectContents.sprite(this.atlas, this.sprite))
            .build();
    }
}

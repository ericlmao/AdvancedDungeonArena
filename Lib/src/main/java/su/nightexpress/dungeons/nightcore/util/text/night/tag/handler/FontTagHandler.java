package su.nightexpress.dungeons.nightcore.util.text.night.tag.handler;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import net.kyori.adventure.key.Key;
import su.nightexpress.dungeons.nightcore.util.text.night.entry.EntryGroup;

public class FontTagHandler extends ClassicTagHandler {

    @Override
    protected void onHandleOpen(@NonNull EntryGroup group, @Nullable String tagContent) {
        if (tagContent == null) return;

        Key font;
        try {
            font = Key.key(tagContent);
        }
        catch (IllegalArgumentException exception) {
            return;
        }

        group.editStyle(builder -> builder.font(font));
    }

    @Override
    protected void onHandleClose(@NonNull EntryGroup group) {

    }
}

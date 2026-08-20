package su.nightexpress.dungeons.nightcore.util.text.night.tag.handler;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import net.kyori.adventure.text.format.ShadowColor;
import su.nightexpress.dungeons.nightcore.util.text.night.entry.EntryGroup;

public class ShadowOffTagHandler extends ClassicTagHandler {

    @Override
    protected void onHandleOpen(@NonNull EntryGroup group, @Nullable String tagContent) {

    }

    @Override
    protected void onHandleClose(@NonNull EntryGroup group) {
        group.editStyle(builder -> builder.shadowColor(ShadowColor.shadowColor(0, 0, 0, 0)));
    }
}

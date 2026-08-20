package su.nightexpress.dungeons.nightcore.util.text.night.tag.handler;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import su.nightexpress.dungeons.nightcore.util.text.night.entry.EntryGroup;

public class ResetTagHandler extends ClassicTagHandler {

    @Override
    protected void onHandleOpen(@NonNull EntryGroup group, @Nullable String tagContent) {
        group.setStyle(EntryGroup.EMPTY_STYLE);
        group.setStyleLocked(true);
    }

    @Override
    protected void onHandleClose(@NonNull EntryGroup group) {

    }
}

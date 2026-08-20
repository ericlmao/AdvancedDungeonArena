package su.nightexpress.dungeons.nightcore.util.text.night.tag.handler;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import su.nightexpress.dungeons.nightcore.util.text.night.ParserUtils;
import su.nightexpress.dungeons.nightcore.util.text.night.entry.EntryGroup;

public class InsertionTagHandler extends ClassicTagHandler {

    @Override
    protected void onHandleOpen(@NonNull EntryGroup group, @Nullable String tagContent) {
        if (tagContent == null) return;

        String insertion = ParserUtils.unquoted(tagContent);
        group.editStyle(builder -> builder.insertion(insertion));
    }

    @Override
    protected void onHandleClose(@NonNull EntryGroup group) {

    }
}

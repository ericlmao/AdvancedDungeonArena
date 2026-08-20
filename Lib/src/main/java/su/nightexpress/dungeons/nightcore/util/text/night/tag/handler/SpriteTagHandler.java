package su.nightexpress.dungeons.nightcore.util.text.night.tag.handler;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import net.kyori.adventure.key.Key;
import su.nightexpress.dungeons.nightcore.util.text.night.ParserUtils;
import su.nightexpress.dungeons.nightcore.util.text.night.entry.EntryGroup;
import su.nightexpress.dungeons.nightcore.util.text.night.entry.SpriteEntry;
import su.nightexpress.dungeons.nightcore.util.text.night.tag.TagContent;

public class SpriteTagHandler extends ClassicTagHandler {

    @Override
    protected void onHandleOpen(@NonNull EntryGroup group, @Nullable String tagContent) {
        if (tagContent == null) return;

        TagContent content = ParserUtils.parseInnerContent(tagContent);

        String atlas = null;
        String sprite;

        if (content.hasBoth()) {
            atlas = content.first();
            sprite = content.second();
        }
        else {
            sprite = ParserUtils.unquoted(content.first());
        }

        try {
            Key atlasKey = atlas == null ? null : Key.key(ParserUtils.unquoted(atlas));

            group.appendEntry(new SpriteEntry(group, atlasKey, Key.key(ParserUtils.unquoted(sprite))));
        }
        catch (IllegalArgumentException exception) {
            // Malformed atlas/sprite key: render nothing rather than blowing up the whole message.
        }
    }

    @Override
    protected void onHandleClose(@NonNull EntryGroup group) {

    }

    @Override
    public boolean canBeClosed() {
        return false;
    }
}

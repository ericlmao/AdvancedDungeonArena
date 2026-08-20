package su.nightexpress.dungeons.nightcore.util.text.night.tag.handler;

import java.util.UUID;
import java.util.regex.Pattern;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import su.nightexpress.dungeons.nightcore.util.text.night.ParserUtils;
import su.nightexpress.dungeons.nightcore.util.text.night.entry.EntryGroup;
import su.nightexpress.dungeons.nightcore.util.text.night.entry.PlayerHeadEntry;
import su.nightexpress.dungeons.nightcore.util.text.night.tag.TagContent;

public class HeadTagHandler extends ClassicTagHandler {

    private static final Pattern UUIDv4_PATTERN = Pattern.compile(
        "[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ABCD][0-9a-f]{3}-[0-9a-f]{12}", Pattern.CASE_INSENSITIVE);

    @Override
    protected void onHandleOpen(@NonNull EntryGroup group, @Nullable String tagContent) {
        if (tagContent == null) return;

        TagContent content = ParserUtils.parseInnerContent(tagContent);

        String data = ParserUtils.unquoted(content.first());
        boolean hat = !content.hasBoth() || Boolean.parseBoolean(content.second());

        PlayerHeadObjectContents.Builder builder = net.kyori.adventure.text.object.ObjectContents.playerHead().hat(hat);

        if (UUIDv4_PATTERN.matcher(data).matches()) {
            builder.id(UUID.fromString(data));
        }
        else if (data.contains("/")) {
            Key texture;
            try {
                texture = Key.key(data);
            }
            catch (IllegalArgumentException _) {
                return;
            }
            builder.texture(texture);
        }
        else {
            builder.name(data);
        }

        group.appendEntry(new PlayerHeadEntry(group, builder.build()));
    }

    @Override
    protected void onHandleClose(@NonNull EntryGroup group) {

    }

    @Override
    public boolean canBeClosed() {
        return false;
    }
}

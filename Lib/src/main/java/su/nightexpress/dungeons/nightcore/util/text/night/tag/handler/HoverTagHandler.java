package su.nightexpress.dungeons.nightcore.util.text.night.tag.handler;

import java.util.Locale;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import net.kyori.adventure.text.event.HoverEvent;
import su.nightexpress.dungeons.nightcore.util.ItemNbt;
import su.nightexpress.dungeons.nightcore.util.Strings;
import su.nightexpress.dungeons.nightcore.util.text.night.ParserUtils;
import su.nightexpress.dungeons.nightcore.util.text.night.TextParser;
import su.nightexpress.dungeons.nightcore.util.text.night.entry.EntryGroup;
import su.nightexpress.dungeons.nightcore.util.text.night.tag.TagContent;

public class HoverTagHandler extends ClassicTagHandler {

    @Override
    protected void onHandleOpen(@NonNull EntryGroup group, @Nullable String tagContent) {
        if (tagContent == null) return;

        TagContent content = ParserUtils.parseInnerContent(tagContent);

        String value = content.second();
        if (value == null) return;

        HoverEvent<?> hoverEvent = switch (content.first().toLowerCase(Locale.ROOT)) {
            case "show_text" -> HoverEvent.showText(TextParser.parse(value));
            case "show_item" -> {
                ItemStack itemStack = ItemNbt.fromTagString(Strings.fromBase64(value));
                yield itemStack == null ? null : itemStack.asHoverEvent();
            }
            default -> null;
        };
        if (hoverEvent == null) return;

        group.editStyle(builder -> builder.hoverEvent(hoverEvent));
    }

    @Override
    protected void onHandleClose(@NonNull EntryGroup group) {

    }
}

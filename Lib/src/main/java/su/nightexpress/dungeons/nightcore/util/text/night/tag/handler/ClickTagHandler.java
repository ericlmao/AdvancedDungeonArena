package su.nightexpress.dungeons.nightcore.util.text.night.tag.handler;

import java.util.Locale;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import net.kyori.adventure.text.event.ClickEvent;
import su.nightexpress.dungeons.nightcore.util.Numbers;
import su.nightexpress.dungeons.nightcore.util.text.night.ParserUtils;
import su.nightexpress.dungeons.nightcore.util.text.night.entry.EntryGroup;
import su.nightexpress.dungeons.nightcore.util.text.night.tag.TagContent;

public class ClickTagHandler extends ClassicTagHandler {

    @Override
    protected void onHandleOpen(@NonNull EntryGroup group, @Nullable String tagContent) {
        if (tagContent == null) return;

        TagContent content = ParserUtils.parseInnerContent(tagContent);

        String value = content.second();
        if (value == null) return;

        ClickEvent clickEvent = switch (content.first().toLowerCase(Locale.ROOT)) {
            case "copy_to_clipboard" -> ClickEvent.copyToClipboard(value);
            case "suggest_command" -> ClickEvent.suggestCommand(value);
            case "run_command" -> ClickEvent.runCommand(value);
            case "change_page" -> ClickEvent.changePage(Numbers.getIntegerAbs(value));
            case "open_file" -> ClickEvent.openFile(value);
            case "open_url" -> ClickEvent.openUrl(value);
            default -> null;
        };
        if (clickEvent == null) return;

        group.editStyle(builder -> builder.clickEvent(clickEvent));
    }

    @Override
    protected void onHandleClose(@NonNull EntryGroup group) {

    }
}

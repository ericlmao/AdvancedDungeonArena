package su.nightexpress.dungeons.nightcore.util.text.night.tag.handler;

import java.awt.Color;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import net.kyori.adventure.text.format.TextColor;
import su.nightexpress.dungeons.nightcore.util.text.night.ParserUtils;
import su.nightexpress.dungeons.nightcore.util.text.night.entry.EntryGroup;

public class ColorTagHandler extends ClassicTagHandler {

    @Override
    protected void onHandleOpen(@NonNull EntryGroup group, @Nullable String tagContent) {
        if (tagContent == null) return;

        Color color = ParserUtils.colorFromSchemeOrHex(tagContent);
        if (color == null) return;

        group.editStyle(builder -> builder.color(TextColor.color(color.getRed(), color.getGreen(), color.getBlue())));
    }

    @Override
    protected void onHandleClose(@NonNull EntryGroup group) {

    }
}

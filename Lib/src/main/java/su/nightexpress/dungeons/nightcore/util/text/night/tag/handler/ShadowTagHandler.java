package su.nightexpress.dungeons.nightcore.util.text.night.tag.handler;

import java.awt.Color;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import net.kyori.adventure.text.format.ShadowColor;
import su.nightexpress.dungeons.nightcore.util.Numbers;
import su.nightexpress.dungeons.nightcore.util.text.night.ParserUtils;
import su.nightexpress.dungeons.nightcore.util.text.night.entry.EntryGroup;
import su.nightexpress.dungeons.nightcore.util.text.night.tag.TagContent;

public class ShadowTagHandler extends ClassicTagHandler {

    private static final float DEFAULT_ALPHA = 0.25f;
    private static final float MAX_ALPHA     = 1f;

    @Override
    protected void onHandleOpen(@NonNull EntryGroup group, @Nullable String tagContent) {
        if (tagContent == null || tagContent.length() < 7) return;

        TagContent content = ParserUtils.parseInnerContent(tagContent);
        String alphaRaw = content.second();

        Color color = ParserUtils.colorFromSchemeOrHex(content.first());
        if (color == null) return;

        float alpha = alphaRaw == null ? DEFAULT_ALPHA : Math.min(MAX_ALPHA, Numbers.getFloatAbs(alphaRaw));

        ShadowColor shadowColor = ShadowColor.shadowColor(color.getRed(), color.getGreen(), color.getBlue(),
            Math.round(alpha * 255f));

        group.editStyle(builder -> builder.shadowColor(shadowColor));
    }

    @Override
    protected void onHandleClose(@NonNull EntryGroup group) {

    }
}

package su.nightexpress.dungeons.nightcore.util.text.night.tag.handler;

import java.awt.Color;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import net.kyori.adventure.text.format.TextColor;
import su.nightexpress.dungeons.nightcore.util.text.night.entry.EntryGroup;

public class NamedColorTagHandler extends ClassicTagHandler {

    private final String    colorName;
    private final Color     color;
    private final TextColor textColor;

    public NamedColorTagHandler(@NonNull String colorName, @NonNull Color color) {
        this.colorName = colorName;
        this.color = color;
        this.textColor = TextColor.color(color.getRed(), color.getGreen(), color.getBlue());
    }

    @NonNull
    public String getColorName() {
        return this.colorName;
    }

    @NonNull
    public Color getColor() {
        return this.color;
    }

    @Override
    protected void onHandleOpen(@NonNull EntryGroup group, @Nullable String tagContent) {
        group.editStyle(builder -> builder.color(this.textColor));
    }

    @Override
    protected void onHandleClose(@NonNull EntryGroup group) {

    }
}

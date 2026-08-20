package su.nightexpress.dungeons.nightcore.util.text.night.tag.handler;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import net.kyori.adventure.text.format.TextDecoration;
import su.nightexpress.dungeons.nightcore.util.text.night.entry.EntryGroup;

public class DecorationTagHandler extends ClassicTagHandler {

    private final TextDecoration decoration;
    private final boolean        state;

    DecorationTagHandler(@NonNull TextDecoration decoration, boolean state) {
        this.decoration = decoration;
        this.state = state;
    }

    @NonNull
    public static DecorationTagHandler normal(@NonNull TextDecoration decoration, boolean state) {
        return new DecorationTagHandler(decoration, state);
    }

    @Override
    protected void onHandleOpen(@NonNull EntryGroup group, @Nullable String tagContent) {
        group.editStyle(builder -> builder.decoration(this.decoration, this.state));
    }

    @Override
    protected void onHandleClose(@NonNull EntryGroup group) {

    }

    public boolean isInverted() {
        return !this.state;
    }
}

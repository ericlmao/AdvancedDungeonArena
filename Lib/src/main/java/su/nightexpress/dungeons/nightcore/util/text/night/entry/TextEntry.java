package su.nightexpress.dungeons.nightcore.util.text.night.entry;

import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;

public class TextEntry extends ChildEntry {

    private final String text;

    public TextEntry(@NonNull EntryGroup parent, @NonNull String text) {
        super(parent);
        this.text = text;
    }

    @NonNull
    public String text() {
        return this.text;
    }

    @Override
    public int textLength() {
        int length = 0;
        for (int index = 0; index < this.text.length(); index++) {
            if (Character.isWhitespace(this.text.charAt(index))) continue;

            length++;
        }

        return length;
    }

    @Override
    @NonNull
    public Component toComponent() {
        if (this.text.equals("\n")) return Component.newline();

        return Component.text(this.text, this.parent.style());
    }

    @Override
    public String toString() {
        return "TextEntry{text='" + this.text + "'}";
    }
}

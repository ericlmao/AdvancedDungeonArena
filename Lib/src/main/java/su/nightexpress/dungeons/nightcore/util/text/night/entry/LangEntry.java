package su.nightexpress.dungeons.nightcore.util.text.night.entry;

import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class LangEntry extends ChildEntry {

    private final String key;
    private final String fallback;

    public LangEntry(@NonNull EntryGroup parent, @NonNull String key, @Nullable String fallback) {
        super(parent);
        this.key = key;
        this.fallback = fallback;
    }

    @NonNull
    public String getKey() {
        return this.key;
    }

    @Nullable
    public String getFallback() {
        return this.fallback;
    }

    @Override
    public int textLength() {
        return 1;
    }

    @Override
    @NonNull
    public Component toComponent() {
        return Component.translatable().key(this.key).fallback(this.fallback).style(this.parent.style()).build();
    }
}

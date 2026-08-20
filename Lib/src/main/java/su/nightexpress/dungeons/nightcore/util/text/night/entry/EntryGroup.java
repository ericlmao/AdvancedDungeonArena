package su.nightexpress.dungeons.nightcore.util.text.night.entry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import su.nightexpress.dungeons.nightcore.util.text.night.ParserResolvers;

public class EntryGroup implements Entry {

    /**
     * Base style for every parsed text node.
     * <p>
     * Every decoration is explicitly set to {@code FALSE} instead of {@code NOT_SET}. This mirrors the
     * upstream {@code NightStyle.EMPTY} behaviour and is what makes parsed item names/lore render without
     * the vanilla italic default.
     */
    public static final Style EMPTY_STYLE = Style.style()
        .decoration(TextDecoration.BOLD, TextDecoration.State.FALSE)
        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        .decoration(TextDecoration.OBFUSCATED, TextDecoration.State.FALSE)
        .decoration(TextDecoration.STRIKETHROUGH, TextDecoration.State.FALSE)
        .decoration(TextDecoration.UNDERLINED, TextDecoration.State.FALSE)
        .build();

    private final String          name;
    private final List<Entry>     childrens;
    private final ParserResolvers resolvers;

    private EntryGroup parent;
    private Style      style;
    private boolean    styleLocked;

    public EntryGroup(@NonNull String name) {
        this.name = name;
        this.childrens = new ArrayList<>();
        this.resolvers = new ParserResolvers();
        this.style = EMPTY_STYLE;
    }

    public void closeResolvers() {
        this.resolvers.removeAll().forEach(resolver -> resolver.handleClose(this));
    }

    @NonNull
    public List<EntryGroup> getChildGroups() {
        List<EntryGroup> groups = new ArrayList<>();
        groups.add(this);

        this.childrens.forEach(entry -> {
            if (entry instanceof EntryGroup group) {
                groups.addAll(group.getChildGroups());
            }
        });

        return groups;
    }

    @NonNull
    public EntryGroup downward(@NonNull String name) {
        EntryGroup group = new EntryGroup(name);
        group.parent = this;
        group.style = this.style;
        return this.addChildren(group);
    }

    @NonNull
    public EntryGroup upward(@NonNull String parentName) {
        EntryGroup upperGroup = this.parent;
        while (upperGroup != null && !upperGroup.name.equalsIgnoreCase(parentName)) {
            upperGroup = upperGroup.parent;
        }

        return Objects.requireNonNullElse(upperGroup, this);
    }

    @NonNull
    public EntryGroup backTo(@NonNull String parentName) {
        if (this.name.equalsIgnoreCase(parentName)) return this;

        return this.upward(parentName);
    }

    @NonNull
    public EntryGroup upward() {
        return Objects.requireNonNullElse(this.parent, this);
    }

    @NonNull
    public TextEntry appendTextEntry(@NonNull String text) {
        return this.addChildren(new TextEntry(this, text));
    }

    @NonNull
    public LangEntry appendLangEntry(@NonNull String key, @Nullable String fallback) {
        return this.addChildren(new LangEntry(this, key, fallback));
    }

    @NonNull
    public KeybindEntry appendKeybindEntry(@NonNull String key) {
        return this.addChildren(new KeybindEntry(this, key));
    }

    @NonNull
    public <T extends ChildEntry> T appendEntry(@NonNull T entry) {
        return this.addChildren(entry);
    }

    @NonNull
    private <T extends Entry> T addChildren(@NonNull T child) {
        this.childrens.add(child);
        return child;
    }

    @Override
    @NonNull
    public Component toComponent() {
        List<Component> children = this.childrens.stream().map(Entry::toComponent).toList();

        return Component.empty().children(children);
    }

    @NonNull
    public ParserResolvers getResolvers() {
        return this.resolvers;
    }

    @NonNull
    public String name() {
        return this.name;
    }

    @Nullable
    public EntryGroup parent() {
        return this.parent;
    }

    @NonNull
    public Style style() {
        return this.style;
    }

    public void setStyle(@NonNull Style style) {
        if (this.styleLocked) return;

        this.style = style;
    }

    public void setStyle(@NonNull UnaryOperator<Style> consumer) {
        if (this.styleLocked) return;

        this.setStyle(consumer.apply(this.style()));
    }

    /**
     * Convenience for the common {@code style -> style.toBuilder()...build()} pattern used by tag handlers.
     */
    public void editStyle(java.util.function.Consumer<Style.Builder> consumer) {
        this.setStyle(style -> {
            Style.Builder builder = style.toBuilder();
            consumer.accept(builder);
            return builder.build();
        });
    }

    public boolean isStyleLocked() {
        return this.styleLocked;
    }

    public void setStyleLocked(boolean styleLocked) {
        this.styleLocked = styleLocked;
    }

    @NonNull
    public List<Entry> getChildrens() {
        return this.childrens;
    }

    public void setChildrens(@NonNull List<Entry> childrens) {
        this.childrens.clear();
        this.childrens.addAll(childrens);
    }
}

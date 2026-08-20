package su.nightexpress.dungeons.nightcore.util.text.night;

import org.jspecify.annotations.NonNull;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import su.nightexpress.dungeons.nightcore.util.text.night.tag.TagPool;

public class NightMessage {

    @NonNull
    public static Component parse(@NonNull String string) {
        return TextParser.parse(string);
    }

    @NonNull
    public static Component parse(@NonNull String string, @NonNull TagPool tagPool) {
        return TextParser.parse(string, tagPool);
    }

    /**
     * Removes all known tags from the given string. Does not affect legacy color codes.
     * 
     * @param string A string to remove tags from.
     * @return String with no tags formations.
     */
    @NonNull
    public static String stripTags(@NonNull String string) {
        return stripTags(string, TagPool.NONE);
    }

    /**
     * Removes tags from the given string according to a TagPool configuration. Does not affect legacy color codes.
     * 
     * @param string  A string to remove tags from.
     * @param tagPool List of allowed tags.
     * @return String with allowed tags only in the original tag format <tag>Text</tag>.
     */
    @NonNull
    public static String stripTags(@NonNull String string, @NonNull TagPool tagPool) {
        return TextParser.strip(string, tagPool);
    }

    @NonNull
    public static String asJson(@NonNull String string) {
        return JSONComponentSerializer.json().serialize(parse(string));
    }

    @NonNull
    public static String asLegacy(@NonNull String string) {
        return LegacyComponentSerializer.legacySection().serialize(parse(string));
    }

    /**
     * Inverse of {@link #parse(String)}: renders a Component back into this plugin's tag markup.
     * <p>
     * MiniMessage's own vocabulary overlaps ours for colours and decorations; the strip pass drops the
     * forms our parser cannot read back (notably MiniMessage's {@code <!b>}-style inverted decorations).
     */
    @NonNull
    public static String serialize(@NonNull Component component) {
        return stripTags(MiniMessage.miniMessage().serialize(component), TagPool.NO_INVERTED_DECORATIONS);
    }
}

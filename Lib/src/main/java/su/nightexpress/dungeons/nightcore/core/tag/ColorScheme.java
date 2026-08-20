package su.nightexpress.dungeons.nightcore.core.tag;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.NonNull;

import su.nightexpress.dungeons.nightcore.util.Lists;
import su.nightexpress.dungeons.nightcore.util.text.night.ParserUtils;
import su.nightexpress.dungeons.nightcore.util.text.night.tag.TagShortNames;

/**
 * A named set of colour tags.
 * <p>
 * Upstream read these from nightcore's own {@code color_schemes.yml}. That file belonged to the NightCore
 * plugin and disappears together with the dependency, so both schemes are inlined here instead. The
 * {@link #CUSTOM} palette is byte-for-byte the upstream {@code custom} scheme and is the one this plugin
 * uses; the values are what {@code <gray>}, {@code <soft_gray>}, {@code <lgray>} and friends resolve to.
 * <p>
 * Both schemes carry the legacy {@code l*}/{@code d*} aliases ({@code lgray}, {@code lcyan}, ...), which
 * upstream only registered on the {@code custom} scheme. Configs written against either vocabulary render.
 */
public class ColorScheme {

    public static final String DEFAULT = "default";
    public static final String CUSTOM  = "custom";

    private final String          id;
    private final List<ColorCode> colors;

    public ColorScheme(@NonNull String id, @NonNull List<ColorCode> colors) {
        this.id = id.toLowerCase();
        this.colors = colors;
    }

    @NonNull
    public static List<ColorScheme> getDefaultSchemes() {
        return Lists.newList(vanillaScheme(), customScheme());
    }

    @NonNull
    public static ColorScheme vanillaScheme() {
        List<ColorCode> colors = Lists.newList(
            new ColorCode(TagShortNames.BLACK, new Color(0, 0, 0)),
            new ColorCode(TagShortNames.WHITE, new Color(255, 255, 255)),

            new ColorCode(TagShortNames.GRAY, new Color(170, 170, 170)),
            new ColorCode(TagShortNames.SOFT_GRAY, new Color(170, 170, 170)),
            new ColorCode(TagShortNames.DARK_GRAY, new Color(70, 70, 70)),

            new ColorCode(TagShortNames.RED, new Color(255, 85, 85)),
            new ColorCode(TagShortNames.SOFT_RED, new Color(255, 85, 85)),
            new ColorCode(TagShortNames.DARK_RED, new Color(170, 0, 0)),

            new ColorCode(TagShortNames.GREEN, new Color(85, 255, 85)),
            new ColorCode(TagShortNames.SOFT_GREEN, new Color(85, 255, 85)),
            new ColorCode(TagShortNames.DARK_GREEN, new Color(0, 170, 0)),

            new ColorCode(TagShortNames.BLUE, new Color(85, 85, 255)),
            new ColorCode(TagShortNames.SOFT_BLUE, new Color(85, 85, 255)),
            new ColorCode(TagShortNames.DARK_BLUE, new Color(0, 0, 170)),

            new ColorCode(TagShortNames.YELLOW, new Color(255, 255, 85)),
            new ColorCode(TagShortNames.SOFT_YELLOW, new Color(255, 255, 85)),
            new ColorCode(TagShortNames.DARK_YELLOW, new Color(180, 180, 50)),

            new ColorCode(TagShortNames.ORANGE, new Color(255, 170, 0)),
            new ColorCode(TagShortNames.SOFT_ORANGE, new Color(230, 170, 50)),
            new ColorCode(TagShortNames.GOLD, new Color(255, 170, 0)),

            new ColorCode(TagShortNames.AQUA, new Color(85, 255, 255)),
            new ColorCode(TagShortNames.SOFT_AQUA, new Color(85, 255, 255)),
            new ColorCode(TagShortNames.DARK_AQUA, new Color(0, 170, 170)),

            new ColorCode(TagShortNames.PURPLE, new Color(255, 85, 255)),
            new ColorCode(TagShortNames.SOFT_PURPLE, new Color(255, 85, 255)),
            new ColorCode(TagShortNames.LIGHT_PURPLE, new Color(255, 85, 255)),
            new ColorCode(TagShortNames.DARK_PURPLE, new Color(170, 0, 170)),

            new ColorCode(TagShortNames.PINK, new Color(230, 50, 120)),
            new ColorCode(TagShortNames.SOFT_PINK, new Color(230, 90, 150))
        );
        colors.addAll(legacyAliases());

        return new ColorScheme(DEFAULT, colors);
    }

    @NonNull
    public static ColorScheme customScheme() {
        List<ColorCode> colors = Lists.newList(
            new ColorCode(TagShortNames.BLACK, new Color(0, 0, 0)),
            new ColorCode(TagShortNames.WHITE, new Color(255, 255, 255)),

            new ColorCode(TagShortNames.GRAY, new Color(161, 161, 161)),
            new ColorCode(TagShortNames.SOFT_GRAY, new Color(180, 180, 180)),
            new ColorCode(TagShortNames.DARK_GRAY, new Color(70, 70, 70)),

            new ColorCode(TagShortNames.RED, new Color(230, 50, 50)),
            new ColorCode(TagShortNames.SOFT_RED, new Color(230, 75, 75)),
            new ColorCode(TagShortNames.DARK_RED, new Color(150, 50, 50)),

            new ColorCode(TagShortNames.GREEN, new Color(50, 230, 50)),
            new ColorCode(TagShortNames.SOFT_GREEN, new Color(120, 230, 80)),
            new ColorCode(TagShortNames.DARK_GREEN, new Color(50, 120, 50)),

            new ColorCode(TagShortNames.BLUE, new Color(50, 120, 230)),
            new ColorCode(TagShortNames.SOFT_BLUE, new Color(50, 170, 230)),
            new ColorCode(TagShortNames.DARK_BLUE, new Color(50, 50, 150)),

            new ColorCode(TagShortNames.YELLOW, new Color(230, 230, 50)),
            new ColorCode(TagShortNames.SOFT_YELLOW, new Color(250, 240, 160)),
            new ColorCode(TagShortNames.DARK_YELLOW, new Color(180, 180, 50)),

            new ColorCode(TagShortNames.ORANGE, new Color(230, 120, 50)),
            new ColorCode(TagShortNames.SOFT_ORANGE, new Color(230, 170, 50)),
            new ColorCode(TagShortNames.GOLD, new Color(230, 170, 50)),

            new ColorCode(TagShortNames.AQUA, new Color(50, 230, 230)),
            new ColorCode(TagShortNames.SOFT_AQUA, new Color(150, 230, 230)),
            new ColorCode(TagShortNames.DARK_AQUA, new Color(50, 120, 120)),

            new ColorCode(TagShortNames.PURPLE, new Color(120, 50, 230)),
            new ColorCode(TagShortNames.SOFT_PURPLE, new Color(150, 90, 230)),
            new ColorCode(TagShortNames.LIGHT_PURPLE, new Color(150, 90, 230)),
            new ColorCode(TagShortNames.DARK_PURPLE, new Color(75, 50, 150)),

            new ColorCode(TagShortNames.PINK, new Color(230, 50, 120)),
            new ColorCode(TagShortNames.SOFT_PINK, new Color(230, 90, 150))
        );
        colors.addAll(legacyAliases());
        // Upstream's `custom` scheme re-declared these after the base entries, so the alias value wins.
        colors.add(new ColorCode("dark_gray", ParserUtils.colorFromHexString("#6c6c62")));
        colors.add(new ColorCode("light_purple", ParserUtils.colorFromHexString("#e39fff")));

        return new ColorScheme(CUSTOM, colors);
    }

    /**
     * Back-compat aliases for the pre-2.x {@code Tags.*} vocabulary ({@code <lgray>}, {@code <cyan>}, ...).
     * User configs are full of these, so they are registered unconditionally.
     */
    @NonNull
    private static List<ColorCode> legacyAliases() {
        return new ArrayList<>(Lists.newList(
            new ColorCode("cyan", ParserUtils.colorFromHexString("#31eace")),
            new ColorCode("dgray", ParserUtils.colorFromHexString("#6c6c62")),
            new ColorCode("lgray", ParserUtils.colorFromHexString("#d4d9d8")),
            new ColorCode("light_gray", ParserUtils.colorFromHexString("#d4d9d8")),
            new ColorCode("lgreen", ParserUtils.colorFromHexString("#91f251")),
            new ColorCode("light_green", ParserUtils.colorFromHexString("#91f251")),
            new ColorCode("lyellow", ParserUtils.colorFromHexString("#ffeea2")),
            new ColorCode("light_yellow", ParserUtils.colorFromHexString("#ffeea2")),
            new ColorCode("lorange", ParserUtils.colorFromHexString("#fdba5e")),
            new ColorCode("light_orange", ParserUtils.colorFromHexString("#fdba5e")),
            new ColorCode("lred", ParserUtils.colorFromHexString("#fd5e5e")),
            new ColorCode("light_red", ParserUtils.colorFromHexString("#fd5e5e")),
            new ColorCode("lblue", ParserUtils.colorFromHexString("#5e9dfd")),
            new ColorCode("light_blue", ParserUtils.colorFromHexString("#5e9dfd")),
            new ColorCode("lcyan", ParserUtils.colorFromHexString("#5edefd")),
            new ColorCode("light_cyan", ParserUtils.colorFromHexString("#5edefd")),
            new ColorCode("lpurple", ParserUtils.colorFromHexString("#e39fff")),
            new ColorCode("lpink", ParserUtils.colorFromHexString("#fd8ddb")),
            new ColorCode("light_pink", ParserUtils.colorFromHexString("#fd8ddb"))
        ));
    }

    @NonNull
    public String getId() {
        return this.id;
    }

    @NonNull
    public List<ColorCode> getColors() {
        return this.colors;
    }
}

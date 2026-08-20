package su.nightexpress.dungeons.nightcore.core.tag;

import org.jspecify.annotations.NonNull;

import net.kyori.adventure.text.format.TextDecoration;
import su.nightexpress.dungeons.nightcore.util.text.night.tag.TagHandlerRegistry;
import su.nightexpress.dungeons.nightcore.util.text.night.tag.TagShortNames;
import su.nightexpress.dungeons.nightcore.util.text.night.tag.handler.ClickTagHandler;
import su.nightexpress.dungeons.nightcore.util.text.night.tag.handler.ColorTagHandler;
import su.nightexpress.dungeons.nightcore.util.text.night.tag.handler.DecorationTagHandler;
import su.nightexpress.dungeons.nightcore.util.text.night.tag.handler.FontTagHandler;
import su.nightexpress.dungeons.nightcore.util.text.night.tag.handler.GradientTagHandler;
import su.nightexpress.dungeons.nightcore.util.text.night.tag.handler.HeadTagHandler;
import su.nightexpress.dungeons.nightcore.util.text.night.tag.handler.HoverTagHandler;
import su.nightexpress.dungeons.nightcore.util.text.night.tag.handler.InsertionTagHandler;
import su.nightexpress.dungeons.nightcore.util.text.night.tag.handler.KeybindTagHandler;
import su.nightexpress.dungeons.nightcore.util.text.night.tag.handler.LangTagHandler;
import su.nightexpress.dungeons.nightcore.util.text.night.tag.handler.PlaceholderTagHandler;
import su.nightexpress.dungeons.nightcore.util.text.night.tag.handler.ResetTagHandler;
import su.nightexpress.dungeons.nightcore.util.text.night.tag.handler.ShadowOffTagHandler;
import su.nightexpress.dungeons.nightcore.util.text.night.tag.handler.ShadowTagHandler;
import su.nightexpress.dungeons.nightcore.util.text.night.tag.handler.SpriteTagHandler;

/**
 * Registers the tag vocabulary understood by {@code util.text.night.TextParser}.
 * <p>
 * Upstream this lived in {@code core.tag.TagManager}, an {@code AbstractManager} owned by the NightCore
 * plugin that also read {@code color_schemes.yml} from disk. Both the manager coupling and the config file
 * are gone: the colour scheme is inlined in {@link ColorScheme}.
 */
public class TagBootstrap {

    private static ColorScheme colorScheme;

    private TagBootstrap() {
    }

    public static void load() {
        colorScheme = ColorScheme.customScheme();

        registerColorTags();
        registerTags();
    }

    public static void unload() {
        colorScheme = null;
        TagHandlerRegistry.clear();
    }

    @NonNull
    public static ColorScheme getColorScheme() {
        return colorScheme == null ? ColorScheme.customScheme() : colorScheme;
    }

    private static void registerColorTags() {
        colorScheme.getColors().forEach(code -> TagHandlerRegistry.register(code::createHandler, code.name()));
    }

    private static void registerTags() {
        TagHandlerRegistry.register(ColorTagHandler::new, TagShortNames.COLOR, "color", "colour");
        TagHandlerRegistry.register(GradientTagHandler::new, TagShortNames.GRADIENT);
        TagHandlerRegistry.register(ShadowTagHandler::new, TagShortNames.SHADOW_COLOR);
        TagHandlerRegistry.register(ShadowOffTagHandler::new, TagShortNames.ANTI_SHADOW);
        TagHandlerRegistry.register(FontTagHandler::new, TagShortNames.FONT);
        TagHandlerRegistry.register(LangTagHandler::new, TagShortNames.LANG, TagShortNames.LANG_OR, "tr", "translate",
            "tr_or", "translate_or");
        TagHandlerRegistry.register(HoverTagHandler::new, TagShortNames.HOVER);
        TagHandlerRegistry.register(ClickTagHandler::new, TagShortNames.CLICK);
        TagHandlerRegistry.register(ResetTagHandler::new, TagShortNames.RESET, "reset");
        TagHandlerRegistry.register(KeybindTagHandler::new, TagShortNames.KEYBIND);
        TagHandlerRegistry.register(InsertionTagHandler::new, TagShortNames.INSERTION);
        TagHandlerRegistry.register(SpriteTagHandler::new, TagShortNames.SPRITE);
        TagHandlerRegistry.register(HeadTagHandler::new, TagShortNames.HEAD);

        TagHandlerRegistry.register(() -> DecorationTagHandler.normal(TextDecoration.BOLD, true),
            TagShortNames.BOLD, "bold");
        TagHandlerRegistry.register(() -> DecorationTagHandler.normal(TextDecoration.ITALIC, true),
            TagShortNames.ITALIC, "italic", "em");
        TagHandlerRegistry.register(() -> DecorationTagHandler.normal(TextDecoration.OBFUSCATED, true),
            TagShortNames.OBFUSCATED, "obfuscated");
        TagHandlerRegistry.register(() -> DecorationTagHandler.normal(TextDecoration.STRIKETHROUGH, true),
            TagShortNames.STRIKETHROUGH, "strikethrough");
        TagHandlerRegistry.register(() -> DecorationTagHandler.normal(TextDecoration.UNDERLINED, true),
            TagShortNames.UNDERLINED, "underlined");

        TagHandlerRegistry.register(() -> DecorationTagHandler.normal(TextDecoration.BOLD, false),
            TagShortNames.UNBOLD, "!bold");
        TagHandlerRegistry.register(() -> DecorationTagHandler.normal(TextDecoration.ITALIC, false),
            TagShortNames.UNITALIC, "!italic", "!em");
        TagHandlerRegistry.register(() -> DecorationTagHandler.normal(TextDecoration.OBFUSCATED, false),
            TagShortNames.UNOBFUSCATED, "!obfuscated");
        TagHandlerRegistry.register(() -> DecorationTagHandler.normal(TextDecoration.STRIKETHROUGH, false),
            TagShortNames.UNSTRIKETHROUGH, "!strikethrough");
        TagHandlerRegistry.register(() -> DecorationTagHandler.normal(TextDecoration.UNDERLINED, false),
            TagShortNames.UNUNDERLINED, "!underlined");

        TagHandlerRegistry.register(() -> new PlaceholderTagHandler("\n"), TagShortNames.BR, "newline");
    }
}

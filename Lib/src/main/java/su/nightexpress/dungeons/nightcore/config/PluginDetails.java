package su.nightexpress.dungeons.nightcore.config;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.nightcore.NightPlugin;
import su.nightexpress.dungeons.nightcore.configuration.ConfigProperty;
import su.nightexpress.dungeons.nightcore.configuration.ConfigTypes;
import su.nightexpress.dungeons.nightcore.util.text.night.wrapper.TagWrappers;

import java.util.Locale;

public class PluginDetails implements Writeable {

    private final String   name;
    private final String   prefix;
    private final String[] commandAliases;
    private final String   language;

    private Class<?> configClass;
    @Deprecated
    private Class<?> langClass;
    private Class<?> permissionsClass;

    public PluginDetails(
                         @NonNull String name,
                         @NonNull String prefix,
                         @NonNull String[] commandAliases,
                         @NonNull String language
    ) {
        this.name = name;
        this.prefix = prefix;
        this.commandAliases = commandAliases;
        this.language = language.toLowerCase();
    }

    @NonNull
    public static PluginDetails create(@NonNull String name, @NonNull String[] commandAliases) {
        // Was Tags.LIGHT_YELLOW/BOLD/DARK_GRAY/GRAY; the modern vocabulary renders identically
        // because the colour scheme registers the legacy `l*` aliases.
        String prefix = TagWrappers.SOFT_YELLOW.wrap(TagWrappers.BOLD.wrap(name))
            + TagWrappers.DARK_GRAY.wrap(" » ") + TagWrappers.GRAY.opening();
        String language = Locale.getDefault().getLanguage();

        return new PluginDetails(name, prefix, commandAliases, language);
    }

    @NonNull
    public static PluginDetails read(@NonNull NightPlugin plugin, @NonNull FileConfig config,
                                     @NonNull PluginDetails defaults) {
        //FileConfig config = plugin.getConfig();
        //PluginDetails defaults = plugin.getDetails();

        String pluginName = ConfigValue.create("Plugin.Name", defaults.getName(),
            "Localized plugin name. It's used in messages and with internal placeholders.")
            .read(config);

        String pluginPrefix = ConfigProperty.of(ConfigTypes.STRING_OR_EMPTY, "Plugin.Prefix", defaults.getPrefix(),
            "Plugin prefix. Used in messages."
        ).resolveWithDefaults(config);

        String[] commandAliases = ConfigValue.create("Plugin.Command_Aliases", defaults.getCommandAliases(),
            "Command names that will be registered as main plugin commands.",
            "Do not leave this empty. Split multiple names with a comma.")
            .read(config);

        String languageCode = ConfigValue.create("Plugin.Language", defaults.getLanguage(),
            "Sets the plugin language.",
            "Basically it tells the plugin to use certain messages config from the 'lang' sub-folder.",
            "If specified language is not available, default one (English) will be used instead.",
            "[Default is System Locale]")
            .read(config);

        return new PluginDetails(pluginName, pluginPrefix, commandAliases, languageCode)
            .setConfigClass(defaults.getConfigClass())
            .setLangClass(defaults.getLangClass())
            .setPermissionsClass(defaults.getPermissionsClass());
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".Plugin.Name", this.name);
        config.set(path + ".Plugin.Prefix", this.prefix);
        config.setStringArray(path + ".Plugin.Command_Aliases", this.commandAliases);
        config.set(path + ".Plugin.Language", this.language);
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getPrefix() {
        return prefix;
    }

    @NonNull
    public String[] getCommandAliases() {
        return commandAliases;
    }

    @NonNull
    public String getLanguage() {
        return language;
    }

    @Nullable
    @Deprecated
    public Class<?> getConfigClass() {
        return configClass;
    }

    @Deprecated
    public PluginDetails setConfigClass(@Nullable Class<?> configClass) {
        this.configClass = configClass;
        return this;
    }

    @Nullable
    @Deprecated
    public Class<?> getLangClass() {
        return langClass;
    }

    @Deprecated
    public PluginDetails setLangClass(@Nullable Class<?> langClass) {
        this.langClass = langClass;
        return this;
    }

    @Deprecated
    @Nullable
    public Class<?> getPermissionsClass() {
        return permissionsClass;
    }

    @Deprecated
    public PluginDetails setPermissionsClass(@Nullable Class<?> permissionsClass) {
        this.permissionsClass = permissionsClass;
        return this;
    }
}

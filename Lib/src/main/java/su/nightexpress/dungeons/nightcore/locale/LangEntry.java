package su.nightexpress.dungeons.nightcore.locale;

import org.bukkit.Keyed;
import org.bukkit.Sound;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.nightcore.NightPlugin;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.configuration.ConfigType;
import su.nightexpress.dungeons.nightcore.locale.entry.*;
import su.nightexpress.dungeons.nightcore.locale.message.MessageData;
import su.nightexpress.dungeons.nightcore.util.bridge.RegistryType;

import java.util.Objects;
import java.util.function.Function;

public class LangEntry<T extends LangValue> implements LangElement {

    protected final ConfigType<T> type;

    protected final String path;
    protected final T      defaultValue;

    protected T value;

    public LangEntry(@NonNull ConfigType<T> type, @NonNull String path, @NonNull T defaultValue) {
        this.type = type;
        this.path = path;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    @Override
    public void load(@NonNull NightPlugin plugin, @NonNull FileConfig config) {
        this.value = config.get(this.type, this.path, this.defaultValue);
    }

    @NonNull
    public static Builder builder(@NonNull String path) {
        return new Builder(path);
    }

    public static IconLocale.@NonNull Builder iconBuilder(@NonNull String path) {
        return new IconLocale.Builder(path);
    }

    @Override
    @NonNull
    public String getPath() {
        return this.path;
    }

    @NonNull
    public T getDefaultValue() {
        return this.defaultValue;
    }

    @NonNull
    public T value() {
        return Objects.requireNonNullElse(this.value, this.defaultValue);
    }

    public static class Builder {

        private final String path;

        public Builder(@NonNull String path) {
            this.path = path;
        }

        @NonNull
        public MessageLocale message(@NonNull MessageData data, @NonNull String... text) {
            return MessageLocale.message(this.path, data, text);
        }

        @NonNull
        public MessageLocale chatMessage(@NonNull String text) {
            return chatMessage(new String[]{text});
        }

        @NonNull
        public MessageLocale chatMessage(@NonNull String... text) {
            return MessageLocale.chat(this.path, text);
        }

        @NonNull
        public MessageLocale chatMessage(@NonNull Sound sound, @NonNull String... text) {
            return MessageLocale.chat(this.path, sound, text);
        }

        @NonNull
        public MessageLocale titleMessage(@NonNull String title, @NonNull String subtitle) {
            return MessageLocale.title(this.path, title, subtitle);
        }

        @NonNull
        public MessageLocale titleMessage(@NonNull String title, @NonNull String subtitle, @NonNull Sound sound) {
            return MessageLocale.title(this.path, title, subtitle, sound);
        }

        @NonNull
        public MessageLocale titleMessage(@NonNull String title, @NonNull String subtitle, int fade, int stay) {
            return MessageLocale.title(this.path, title, subtitle, fade, stay);
        }

        @NonNull
        public MessageLocale titleMessage(@NonNull String title, @NonNull String subtitle, int fade, int stay,
                                          @NonNull Sound sound) {
            return MessageLocale.title(this.path, title, subtitle, fade, stay, sound);
        }

        @NonNull
        public MessageLocale actionBarMessage(@NonNull String text) {
            return MessageLocale.actionBar(this.path, text);
        }

        @NonNull
        public MessageLocale actionBarMessage(@NonNull String text, @NonNull Sound sound) {
            return MessageLocale.actionBar(this.path, text, sound);
        }

        @NonNull
        public BooleanLocale bool(@NonNull String onTrue, @NonNull String onFalse) {
            return BooleanLocale.create(this.path, onTrue, onFalse);
        }

        @NonNull
        public TextLocale text(@NonNull String text) {
            return TextLocale.create(this.path, text);
        }

        @NonNull
        public TextLocale text(@NonNull String... text) {
            return TextLocale.create(this.path, text);
        }

        @NonNull
        public <E extends Enum<E>> EnumLocale<E> enumeration(@NonNull Class<E> clazz) {
            return EnumLocale.create(this.path, clazz);
        }

        @NonNull
        public <E extends Enum<E>> EnumLocale<E> enumeration(@NonNull Class<E> clazz,
                                                             @NonNull Function<E, String> defaultMapper) {
            return EnumLocale.create(this.path, clazz, defaultMapper);
        }

        @NonNull
        public <E extends Keyed> RegistryLocale<E> registry(@NonNull RegistryType<E> type) {
            return RegistryLocale.create(this.path, type);
        }

    }
}

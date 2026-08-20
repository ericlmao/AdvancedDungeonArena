package su.nightexpress.dungeons.api.mob;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.nightcore.config.ConfigValue;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.config.Writeable;

public record MobIdentifier(@NonNull String providerId, @NonNull String mobId) implements Writeable {

    private static final String DELIMITER = ":";

    @NonNull
    public static MobIdentifier from(@NonNull MobProvider provider, @NonNull String mobId) {
        return new MobIdentifier(provider.getName(), mobId);
    }

    @NonNull
    public static MobIdentifier read(@NonNull FileConfig config, @NonNull String path) {
        String string = ConfigValue.create(path, "null" + DELIMITER + "null").read(config);
        return deserialize(string);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path, this.serialize());
    }

    @NonNull
    public static MobIdentifier deserialize(@NonNull String string) {
        String[] split = string.split(DELIMITER);
        if (split.length < 2) throw new IllegalStateException("String " + string + " does not have required params!");

        return new MobIdentifier(split[0], split[1]);
    }

    @NonNull
    public String serialize() {
        return this.providerId + DELIMITER + this.mobId;
    }
}

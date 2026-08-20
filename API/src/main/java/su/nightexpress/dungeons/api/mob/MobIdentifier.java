package su.nightexpress.dungeons.api.mob;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.nightcore.config.ConfigValue;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.config.Writeable;

import java.util.Objects;

public class MobIdentifier implements Writeable {

    private static final String DELIMITER = ":";

    private final String providerId;
    private final String mobId;

    public MobIdentifier(@NonNull String providerId, @NonNull String mobId) {
        this.providerId = providerId;
        this.mobId = mobId;
    }

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

    @NonNull
    public String getProviderId() {
        return this.providerId;
    }

    @NonNull
    public String getMobId() {
        return this.mobId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof MobIdentifier that)) return false;
        return Objects.equals(providerId, that.providerId) && Objects.equals(mobId, that.mobId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(providerId, mobId);
    }
}

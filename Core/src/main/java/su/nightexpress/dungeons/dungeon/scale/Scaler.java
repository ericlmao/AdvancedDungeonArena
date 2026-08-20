package su.nightexpress.dungeons.dungeon.scale;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.config.Writeable;

public class Scaler implements Writeable {

    private final double value;
    private final ScaleType type;

    public Scaler(double value, @NonNull ScaleType type) {
        this.value = value;
        this.type = type;
    }

    @NonNull
    public static Scaler read(@NonNull FileConfig config, @NonNull String path) {
        double value = config.getDouble(path + ".Value", 0D);
        ScaleType type = config.getEnum(path + ".Type", ScaleType.class, ScaleType.PLAIN);

        return new Scaler(value, type);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".Value", this.value);
        config.set(path + ".Type", this.type.name());
        // TODO Limit (valut * base)
    }

    public double scale(@NonNull DungeonInstance instance, @NonNull ScaleBase scaleBase, double original) {
        double base = scaleBase.getBaseValue(instance);

        return switch (this.type) {
            case MULTIPLIER -> original * (1D + this.value * base);
            case PLAIN -> original + (this.value * base);
        };
    }

    public double getValue() {
        return this.value;
    }

    @NonNull
    public ScaleType getType() {
        return this.type;
    }
}

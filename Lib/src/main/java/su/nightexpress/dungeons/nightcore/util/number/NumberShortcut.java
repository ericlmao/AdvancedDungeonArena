package su.nightexpress.dungeons.nightcore.util.number;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.config.Writeable;
import su.nightexpress.dungeons.nightcore.core.CoreSettings;

public class NumberShortcut implements Writeable {

    private final int    magnitude;
    private final String symbol;

    public NumberShortcut(int magnitude, @NonNull String symbol) {
        this.magnitude = magnitude;
        this.symbol = symbol;
    }

    @NonNull
    public static NumberShortcut read(@NonNull FileConfig config, @NonNull String path) {
        int magnitude = config.getInt(path + ".Magnitude");
        String name = config.getString(path + ".Name", String.valueOf(magnitude));

        return new NumberShortcut(magnitude, name);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".Magnitude", this.magnitude);
        config.set(path + ".Name", this.symbol);
    }

    public int getMagnitude() {
        return this.magnitude;
    }

    public double getMultiplier() {
        return Math.pow(CoreSettings.NUMBER_SHORTCUT_STEP, this.magnitude);
    }

    @NonNull
    public String getSymbol() {
        return this.symbol;
    }

    @Override
    public String toString() {
        return "NumberShortcut{" +
            "magnitude=" + magnitude +
            ", symbol='" + symbol + '\'' +
            '}';
    }
}

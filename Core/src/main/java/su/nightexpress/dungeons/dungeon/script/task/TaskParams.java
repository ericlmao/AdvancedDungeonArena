package su.nightexpress.dungeons.dungeon.script.task;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.nightcore.config.ConfigValue;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.config.Writeable;
import su.nightexpress.dungeons.nightcore.util.wrapper.UniInt;

public record TaskParams(@NonNull String display, @NonNull UniInt amount, boolean perPlayer, boolean autoAdd) implements Writeable {

    @NonNull
    public static TaskParams read(@NonNull FileConfig config, @NonNull String path) {
        String display = config.getString(path + ".Display", "null");
        UniInt amount = UniInt.read(config, path + ".Amount");
        boolean perPlayer = config.getBoolean(path + ".PerPlayer");
        boolean autoAdd = ConfigValue.create(path + ".AutoAdd", true).read(config);

        return new TaskParams(display, amount, perPlayer, autoAdd);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".Display", this.display);
        this.amount.write(config, path + ".Amount");
        config.set(path + ".PerPlayer", this.perPlayer);
        config.set(path + ".AutoAdd", this.autoAdd);
    }
}

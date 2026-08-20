package su.nightexpress.dungeons.dungeon.script.task;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.nightcore.config.ConfigValue;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.config.Writeable;
import su.nightexpress.dungeons.nightcore.util.wrapper.UniInt;

public class TaskParams implements Writeable {

    private final String display;
    private final UniInt amount;
    private final boolean perPlayer;
    private final boolean autoAdd;

    public TaskParams(String display, UniInt amount, boolean perPlayer, boolean autoAdd) {
        this.display = display;
        this.amount = amount;
        this.perPlayer = perPlayer;
        this.autoAdd = autoAdd;
    }

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

    @NonNull
    public String getDisplay() {
        return this.display;
    }

    @NonNull
    public UniInt getAmount() {
        return this.amount;
    }

    public boolean isPerPlayer() {
        return this.perPlayer;
    }

    public boolean isAutoAdd() {
        return this.autoAdd;
    }
}

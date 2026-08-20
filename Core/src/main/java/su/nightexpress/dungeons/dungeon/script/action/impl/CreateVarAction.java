package su.nightexpress.dungeons.dungeon.script.action.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.script.action.Action;
import su.nightexpress.dungeons.dungeon.script.action.ActionId;
import su.nightexpress.dungeons.nightcore.config.ConfigValue;
import su.nightexpress.dungeons.nightcore.config.FileConfig;

@Deprecated
public class CreateVarAction implements Action {

    private final String  name;
    private final double  initialValue;
    private final boolean limited;
    private final double minValue;
    private final double maxValue;

    public CreateVarAction(@NonNull String name, double initialValue, boolean limited, double minValue, double maxValue) {
        this.name = name.toLowerCase();
        this.initialValue = initialValue;
        this.limited = limited;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @NonNull
    public static CreateVarAction load(@NonNull FileConfig config, @NonNull String path) {
        String name = ConfigValue.create(path + ".Name", "null").read(config);
        double initialValue = ConfigValue.create(path + ".InitialValue", 0D).read(config);
        boolean limited = ConfigValue.create(path + ".Limited", false).read(config);
        double minValue = ConfigValue.create(path + ".MinValue", -1).read(config);
        double maxValue = ConfigValue.create(path + ".MaxValue", -1).read(config);

        return new CreateVarAction(name, initialValue, limited, minValue, maxValue);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".Name", this.name);
        config.set(path + ".InitialValue", this.initialValue);
        config.set(path + ".Limited", this.limited);
        config.set(path + ".MinValue", this.minValue);
        config.set(path + ".MaxValue", this.maxValue);
    }

    @NonNull
    @Override
    public String getName() {
        return ActionId.CREATE_VAR;
    }

    @Override
    public void perform(@NonNull DungeonInstance dungeon, @NonNull DungeonGameEvent event) {
        if (this.limited) {
            dungeon.getVariables().createLimitedVariable(this.name, this.initialValue, this.minValue, this.maxValue);
        }
        else {
            dungeon.getVariables().createUnlimitedVariable(this.name, this.initialValue);
        }
    }
}

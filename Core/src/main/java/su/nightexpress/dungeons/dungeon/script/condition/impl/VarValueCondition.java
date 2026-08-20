package su.nightexpress.dungeons.dungeon.script.condition.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.game.Variable;
import su.nightexpress.dungeons.dungeon.script.condition.ConditionId;
import su.nightexpress.dungeons.dungeon.script.condition.type.NumberCompareCondition;
import su.nightexpress.dungeons.dungeon.script.number.NumberComparator;
import su.nightexpress.dungeons.util.ErrorHandler;
import su.nightexpress.dungeons.nightcore.config.ConfigValue;
import su.nightexpress.dungeons.nightcore.config.FileConfig;

public class VarValueCondition extends NumberCompareCondition {

    private final String varName;

    public VarValueCondition(@NonNull String varName, @NonNull NumberComparator comparator, double compareValue) {
        super(comparator, compareValue);
        this.varName = varName;
    }

    @NonNull
    public static VarValueCondition load(@NonNull FileConfig config, @NonNull String path) {
        NumberData numberData = readNumberData(config, path);
        String varName = ConfigValue.create(path + ".Variable", "null").read(config);

        return new VarValueCondition(varName, numberData.comparator(), numberData.compareValue());
    }

    @Override
    protected void writeAdditional(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".Variable", this.varName);
    }

    @NonNull
    @Override
    public String getName() {
        return ConditionId.VAR_VALUE;
    }

    @Override
    protected double getDungeonValue(@NonNull DungeonInstance dungeon) {
        Variable variable = dungeon.getVariables().getVariable(this.varName);
        if (variable == null) {
            ErrorHandler.error("Could not compare '" + this.varName + "' variable value: Variable not found.", this, dungeon);
            return 0D;
        }

        return variable.getValue();
    }
}

package su.nightexpress.dungeons.dungeon.script.action.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.game.DungeonVariables;
import su.nightexpress.dungeons.dungeon.game.Variable;
import su.nightexpress.dungeons.dungeon.script.action.Action;
import su.nightexpress.dungeons.dungeon.script.action.ActionId;
import su.nightexpress.dungeons.util.ErrorHandler;
import su.nightexpress.dungeons.nightcore.config.ConfigValue;
import su.nightexpress.dungeons.nightcore.config.FileConfig;

public record ModifyVarAction(@NonNull String varName, @NonNull Operation operation, double value) implements Action {

    public enum Operation {
        PLUS, MINUS, MULTIPLY, DIVIDE, SET
    }

    public ModifyVarAction {
        varName = varName.toLowerCase();
    }

    @NonNull
    public static ModifyVarAction load(@NonNull FileConfig config, @NonNull String path) {
        String varName = ConfigValue.create(path + ".Variable", "null").read(config);
        Operation operation = ConfigValue.create(path + ".Operation", Operation.class, Operation.PLUS).read(config);
        double amount = ConfigValue.create(path + ".Value", 0D).read(config);

        return new ModifyVarAction(varName, operation, amount);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".Variable", this.varName);
        config.set(path + ".Operation", this.operation);
        config.set(path + ".Value", this.value);
    }

    @NonNull
    @Override
    public String getName() {
        return ActionId.MODIFY_VAR;
    }

    @Override
    public void perform(@NonNull DungeonInstance dungeon, @NonNull DungeonGameEvent event) {
        DungeonVariables variables = dungeon.getVariables();
        Variable variable = variables.getVariable(this.varName);

        if (variable == null) {
            ErrorHandler.error("Variable '" + this.varName + "' does not exist.", this, dungeon);
            return;
        }

        variable.modify(this::modifyAmount);
    }

    private double modifyAmount(double current) {
        return switch (this.operation) {
            case PLUS -> current + this.value;
            case MINUS -> current - this.value;
            case MULTIPLY -> current * this.value;
            case DIVIDE -> current / this.value;
            case SET -> this.value;
        };
    }
}

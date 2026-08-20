package su.nightexpress.dungeons.dungeon.script.action.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.game.Variable;
import su.nightexpress.dungeons.dungeon.script.action.Action;
import su.nightexpress.dungeons.dungeon.script.action.ActionId;
import su.nightexpress.dungeons.util.ErrorHandler;
import su.nightexpress.dungeons.nightcore.config.FileConfig;

import java.util.List;

public record ResetVariableAction(@NonNull List<String> varNames) implements Action {

    @NonNull
    public static ResetVariableAction load(@NonNull FileConfig config, @NonNull String path) {
        List<String> varNames = config.getStringList(path + ".Variables");

        return new ResetVariableAction(varNames);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".Variables", this.varNames);
    }

    @NonNull
    @Override
    public String getName() {
        return ActionId.RESET_VARIABLE;
    }

    @Override
    public void perform(@NonNull DungeonInstance dungeon, @NonNull DungeonGameEvent event) {
        this.varNames.forEach(name -> {
            Variable variable = dungeon.getVariables().getVariable(name);
            if (variable == null) {
                ErrorHandler.error("Could not reset '" + name + "' variable: Variable not defined.", this, dungeon);
                return;
            }

            variable.reset();
        });
    }
}

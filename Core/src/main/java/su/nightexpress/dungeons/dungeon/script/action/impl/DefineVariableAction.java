package su.nightexpress.dungeons.dungeon.script.action.impl;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.script.action.Action;
import su.nightexpress.dungeons.dungeon.script.action.ActionId;
import su.nightexpress.dungeons.util.ErrorHandler;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.util.NumberUtil;
import su.nightexpress.dungeons.nightcore.util.wrapper.UniDouble;

import java.util.ArrayList;
import java.util.List;

public class DefineVariableAction implements Action {

    private static final String DELIMITER = ";";

    private final List<VarDefinition> definitions;

    public record VarDefinition(@NonNull String name, double initial, @Nullable UniDouble bounds){

        @NonNull
        public String serialize() {
            if (this.bounds != null) {
                return this.initial + DELIMITER + this.bounds.getMinValue() + DELIMITER + this.bounds.getMaxValue();
            }
            return String.valueOf(this.initial);
        }
    }

    public DefineVariableAction(@NonNull List<VarDefinition> definitions) {
        this.definitions = definitions;
    }

    @NonNull
    public static DefineVariableAction load(@NonNull FileConfig config, @NonNull String path) {
        List<VarDefinition> definitions = new ArrayList<>();

        config.getSection(path + ".Variables").forEach(name -> {
            String rawData = config.getString(path + ".Variables." + name);
            if (rawData == null) return;

            String[] split = rawData.split(DELIMITER);
            Double initial = NumberUtil.parseDouble(split[0]).orElse(null);
            if (initial == null) {
                ErrorHandler.error("Could not define variable '" + name + "': Invalid initial value '" + split[0] + "'.", config, path);
                return;
            }

            UniDouble bounds = null;
            if (split.length >= 3) {
                Double min = NumberUtil.parseDouble(split[1]).orElse(null);
                Double max = NumberUtil.parseDouble(split[2]).orElse(null);

                if (min == null || max == null) {
                    ErrorHandler.error("Could not set bounds for the '" + name + "' variable: Invalid min/max value: '" + split[1] + "', '" + split[2] + "'", config, path);
                }
                else {
                    bounds = UniDouble.of(min, max);
                }
            }

            definitions.add(new VarDefinition(name, initial, bounds));
        });

        return new DefineVariableAction(definitions);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.remove(path + ".Variables");
        this.definitions.forEach(definition -> {
            config.set(path + ".Variables." + definition.name(), definition.serialize());
        });
    }

    @NonNull
    @Override
    public String getName() {
        return ActionId.DEFINE_VARIABLE;
    }

    @Override
    public void perform(@NonNull DungeonInstance dungeon, @NonNull DungeonGameEvent event) {
        this.definitions.forEach(definition -> {
            UniDouble bounds = definition.bounds();
            if (bounds != null) {
                dungeon.getVariables().createLimitedVariable(definition.name(), definition.initial(), bounds.getMinValue(), bounds.getMaxValue());
            }
            else {
                dungeon.getVariables().createUnlimitedVariable(definition.name(), definition.initial());
            }
        });
    }
}

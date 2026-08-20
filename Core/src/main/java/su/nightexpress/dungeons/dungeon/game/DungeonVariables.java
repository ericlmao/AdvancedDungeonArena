package su.nightexpress.dungeons.dungeon.game;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.Placeholders;
import su.nightexpress.dungeons.nightcore.util.NumberUtil;
import su.nightexpress.dungeons.nightcore.util.placeholder.Replacer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

public class DungeonVariables {

    private final Map<String, Variable> variableMap;

    public DungeonVariables() {
        this.variableMap = new HashMap<>();
    }

    @NonNull
    public UnaryOperator<String> replacePlaceholders() {
        Replacer replacer = Replacer.create();

        this.variableMap.forEach((name, variable) -> {
            double value = variable.getValue();

            replacer
                .replace(Placeholders.DUNGEON_VAR_RAW.apply(name), String.valueOf(value))
                .replace(Placeholders.DUNGEON_VAR.apply(name), NumberUtil.format(value));
        });

        return replacer::apply;
    }

    public void clear() {
        this.variableMap.clear();
    }

    @NonNull
    public Optional<Variable> variable(@NonNull String name) {
        return Optional.ofNullable(this.getVariable(name));
    }

    @Nullable
    public Variable getVariable(@NonNull String name) {
        return this.variableMap.get(name);
    }

    public boolean hasVariable(@NonNull String name) {
        return this.getVariable(name) != null;
    }

    public void createLimitedVariable(@NonNull String name, double initial, double min, double max) {
        this.createVariable(name, new Variable(initial, true, min, max));
    }

    public void createUnlimitedVariable(@NonNull String name, double initial) {
        this.createVariable(name, new Variable(initial, false, -1, -1));
    }

    private void createVariable(@NonNull String name, @NonNull Variable variable) {
        this.variableMap.put(name, variable);
    }

    public void removeVariable(@NonNull String name) {
        this.variableMap.remove(name);
    }
}

package su.nightexpress.dungeons.dungeon.script.condition.type;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.dungeon.script.condition.Condition;
import su.nightexpress.dungeons.dungeon.script.number.NumberComparator;
import su.nightexpress.dungeons.dungeon.script.number.NumberComparators;
import su.nightexpress.dungeons.util.ErrorHandler;
import su.nightexpress.dungeons.nightcore.config.FileConfig;

public abstract class NumberCompareCondition implements Condition {

    protected final NumberComparator comparator;
    protected final double compareValue;

    public NumberCompareCondition(@NonNull NumberComparator comparator, double compareValue) {
        this.comparator = comparator;
        this.compareValue = compareValue;
    }

    public record NumberData(NumberComparator comparator, double compareValue){}

    @NonNull
    public static NumberData readNumberData(@NonNull FileConfig config, @NonNull String path) {
        String operatorStr = config.getString(path + ".Operator", "null");
        NumberComparator comparator = NumberComparators.getComparator(operatorStr);
        if (comparator == null) {
            ErrorHandler.error("Invalid number comparing operator '" + operatorStr + "'!", config, path);
            comparator = NumberComparators.DUMMY;
        }

        double compareValue = config.getDouble(path + ".Value");

        return new NumberData(comparator, compareValue);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".Operator", this.comparator.getName());
        config.set(path + ".Value", this.compareValue);
        this.writeAdditional(config, path);
    }

    protected abstract void writeAdditional(@NonNull FileConfig config, @NonNull String path);

    @Override
    public boolean test(@NonNull DungeonInstance dungeon, @NonNull DungeonGameEvent event) {
        double dungeonValue = this.getDungeonValue(dungeon);

        return this.comparator.test(dungeonValue, this.compareValue);
    }

    protected abstract double getDungeonValue(@NonNull DungeonInstance dungeon);
}

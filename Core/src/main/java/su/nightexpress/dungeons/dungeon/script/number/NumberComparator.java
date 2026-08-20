package su.nightexpress.dungeons.dungeon.script.number;

import org.jspecify.annotations.NonNull;

import java.util.function.BiPredicate;

public interface NumberComparator {

    @NonNull String getName();

    boolean test(double value, double compareWith);

    @NonNull
    static NumberComparator create(@NonNull String name, @NonNull BiPredicate<Double, Double> predicate) {
        return new NumberComparator() {

            @NonNull
            @Override
            public String getName() {
                return name;
            }

            @Override
            public boolean test(double value, double compareWith) {
                return predicate.test(value, compareWith);
            }
        };
    }
}

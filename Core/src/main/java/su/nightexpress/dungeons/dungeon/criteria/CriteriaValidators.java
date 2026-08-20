package su.nightexpress.dungeons.dungeon.criteria;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.criteria.CriteriaValidator;
import su.nightexpress.dungeons.nightcore.util.Enums;

import java.util.function.Function;

public class CriteriaValidators {

    public static final CriteriaValidator<Boolean> BOOLEAN = create(Boolean::parseBoolean, Object::toString);

    public static final CriteriaValidator<String> STRING = create(string -> string, string -> string);

    public static <E extends Enum<E>> CriteriaValidator<E> forEnum(@NonNull Class<E> clazz) {
        return create(string -> Enums.parse(string, clazz).orElseThrow(), Enum::name);
    }

    @NonNull
    private static <T> CriteriaValidator<T> create(@NonNull Function<String, T> deserializer, @NonNull Function<T, String> serializer) {
        return new CriteriaValidator<>() {
            @NonNull
            @Override
            public T deserialize(@NonNull String string) {
                return deserializer.apply(string);
            }

            @NonNull
            @Override
            public String serialize(@NonNull T value) {
                return serializer.apply(value);
            }
        };
    }
}

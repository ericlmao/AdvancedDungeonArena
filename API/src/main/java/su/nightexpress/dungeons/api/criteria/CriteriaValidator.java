package su.nightexpress.dungeons.api.criteria;

import org.jspecify.annotations.NonNull;

public interface CriteriaValidator<T> {

    @NonNull T deserialize(@NonNull String string);

    @NonNull String serialize(@NonNull T value);
}

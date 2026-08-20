package su.nightexpress.dungeons.nightcore.util.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.jspecify.annotations.NullMarked;

/**
 * Captures a generic type at compile time (super-type-token). Was {@code bridge.reflect.TypeReference}.
 */
@NullMarked
public abstract class TypeReference<T> {

    private final Type type;

    protected TypeReference() {
        Type superclass = this.getClass().getGenericSuperclass();
        if (superclass instanceof Class<?>) {
            throw new IllegalArgumentException("Missing type parameter.");
        }
        this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }

    public Type getType() {
        return this.type;
    }
}

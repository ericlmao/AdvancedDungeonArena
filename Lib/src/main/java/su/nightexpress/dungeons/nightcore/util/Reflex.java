package su.nightexpress.dungeons.nightcore.util;

import org.jspecify.annotations.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Narrow reflection helper. Its only jobs are harvesting static constants out of holder classes
 * (permissions, lang elements, config values) and probing for an optional class on the classpath.
 * <p>
 * The generic field/method/constructor lookup surface that used to live here - including the legacy
 * Spigot obfuscation-era {@code realName}/{@code obfName} overloads - was unused and has been removed.
 * Do not re-add it; use direct calls or {@link java.lang.invoke.MethodHandles} instead.
 */
public class Reflex {

    /**
     * Whether a class is on the classpath, without initializing it.
     * <p>
     * The {@code initialize} flag matters: this is used to probe for optional soft dependencies, and the
     * plain {@code Class.forName} would run the probed class's static initializer as a side effect of
     * asking whether it exists.
     */
    public static boolean classExists(@NonNull String path) {
        try {
            Class.forName(path, false, Reflex.class.getClassLoader());
            return true;
        }
        catch (ClassNotFoundException _) {
            return false;
        }
    }

    @NonNull
    public static <T> List<T> getStaticFields(@NonNull Class<?> source, @NonNull Class<T> type, boolean includeParent) {
        List<T> list = new ArrayList<>();

        for (Field field : getFields(source, includeParent)) {
            if (!Modifier.isStatic(field.getModifiers())) continue;
            if (!type.isAssignableFrom(field.getType())) continue;
            if (!field.trySetAccessible()) continue;

            try {
                list.add(type.cast(field.get(null)));
            }
            catch (IllegalArgumentException | IllegalAccessException exception) {
                throw new IllegalStateException("Could not read static field '" + field.getDeclaringClass().getName() +
                    "#" + field.getName() + "'", exception);
            }
        }

        return list;
    }

    @NonNull
    private static List<Field> getFields(@NonNull Class<?> source, boolean includeParent) {
        List<Field> result = new ArrayList<>();

        Class<?> lookupClass = source;
        while (lookupClass != null && lookupClass != Object.class) {
            if (!result.isEmpty()) {
                result.addAll(0, Arrays.asList(lookupClass.getDeclaredFields()));
            }
            else {
                Collections.addAll(result, lookupClass.getDeclaredFields());
            }
            if (!includeParent) {
                break;
            }
            lookupClass = lookupClass.getSuperclass();
        }

        return result;
    }
}

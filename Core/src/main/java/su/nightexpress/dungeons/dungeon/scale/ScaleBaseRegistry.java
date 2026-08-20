package su.nightexpress.dungeons.dungeon.scale;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.dungeon.scale.impl.AlivePlayerAmountBase;
import su.nightexpress.dungeons.dungeon.scale.impl.DeadPlayerAmountBase;
import su.nightexpress.dungeons.dungeon.scale.impl.PlayerAmountBase;

import java.util.HashMap;
import java.util.Map;

public class ScaleBaseRegistry {

    private static final Map<String, ScaleBase> SCALE_BASES = new HashMap<>();

    public static void load() {
        register(new PlayerAmountBase());
        register(new AlivePlayerAmountBase());
        register(new DeadPlayerAmountBase());
    }

    public static void clear() {
        SCALE_BASES.clear();
    }

    @NonNull
    public static ScaleBase register(@NonNull ScaleBase scaleBase) {
        SCALE_BASES.put(scaleBase.getName().toLowerCase(), scaleBase);

        return scaleBase;
    }

    @Nullable
    public static ScaleBase getByName(@NonNull String name) {
        return SCALE_BASES.get(name.toLowerCase());
    }
}

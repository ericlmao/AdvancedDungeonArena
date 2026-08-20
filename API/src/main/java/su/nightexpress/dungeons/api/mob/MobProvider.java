package su.nightexpress.dungeons.api.mob;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.api.dungeon.Dungeon;
import su.nightexpress.dungeons.api.type.MobFaction;

import java.util.List;
import java.util.function.Consumer;

public interface MobProvider {

    @NonNull String getName();

    @Nullable LivingEntity spawn(@NonNull Dungeon arena, @NonNull String mobId, @NonNull MobFaction faction, @NonNull Location location, int level, @Nullable Consumer<LivingEntity> prespawn);

    @Deprecated
    @NonNull List<String> getMobNames();

    boolean isProducedBy(@NonNull LivingEntity entity);

    @Nullable String getMobId(@NonNull LivingEntity entity);
}

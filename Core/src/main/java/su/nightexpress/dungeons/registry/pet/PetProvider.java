package su.nightexpress.dungeons.registry.pet;

import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.NonNull;

public interface PetProvider {

    @NonNull String getName();

    boolean isPet(@NonNull LivingEntity entity);
}

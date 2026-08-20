package su.nightexpress.dungeons.registry.pet.provider;

import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.registry.pet.PetProvider;
import su.nightexpress.combatpets.api.pet.PetEntityBridge;

public class CombatPetsProvider implements PetProvider {

    @NonNull
    @Override
    public String getName() {
        return "combatpets";
    }

    @Override
    public boolean isPet(@NonNull LivingEntity entity) {
        return PetEntityBridge.isPet(entity);
    }
}

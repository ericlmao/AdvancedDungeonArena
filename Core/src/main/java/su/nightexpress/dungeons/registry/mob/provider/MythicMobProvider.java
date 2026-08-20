package su.nightexpress.dungeons.registry.mob.provider;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.api.mobs.entities.SpawnReason;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.api.dungeon.Dungeon;
import su.nightexpress.dungeons.api.mob.MobProvider;
import su.nightexpress.dungeons.api.type.MobFaction;
import su.nightexpress.dungeons.hook.impl.MythicMobsHook;
import su.nightexpress.dungeons.registry.mob.MobProviderId;

import java.util.List;
import java.util.function.Consumer;

public class MythicMobProvider implements MobProvider {

    @NonNull
    @Override
    public String getName() {
        return MobProviderId.MYTHIC_MOBS;
    }

    @Nullable
    @Override
    public LivingEntity spawn(@NonNull Dungeon arena, @NonNull String mobId, @NonNull MobFaction faction, @NonNull Location location, int level, @Nullable Consumer<LivingEntity> prespawn) {
        MythicMob mythicMob = MythicMobsHook.getMobConfig(mobId);
        if (mythicMob == null) return null;

        ActiveMob mob = mythicMob.spawn(BukkitAdapter.adapt(location), level, SpawnReason.OTHER, entity -> {
            if (prespawn != null && entity instanceof LivingEntity livingEntity) {
                prespawn.accept(livingEntity);
            }
        });

        if (!(mob.getEntity().getBukkitEntity() instanceof LivingEntity entity)) {
            mob.remove();
            return null;
        }

        return entity;
    }

    @NonNull
    @Override
    public List<String> getMobNames() {
        return MythicMobsHook.getMobConfigIds();
    }

    @Override
    public boolean isProducedBy(@NonNull LivingEntity entity) {
        return MythicMobsHook.isMythicMob(entity);
    }

    @Override
    @Nullable
    public String getMobId(@NonNull LivingEntity entity) {
        return MythicMobsHook.getMobInternalName(entity);
    }
}

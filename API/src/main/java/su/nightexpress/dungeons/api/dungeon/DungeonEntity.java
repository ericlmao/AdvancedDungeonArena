package su.nightexpress.dungeons.api.dungeon;

import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.criteria.CriterionMob;
import su.nightexpress.dungeons.api.mob.MobIdentifier;
import su.nightexpress.dungeons.api.mob.MobProvider;
import su.nightexpress.dungeons.api.mob.MobSnapshot;

import java.util.UUID;

public interface DungeonEntity extends CriterionMob {

    @NonNull MobSnapshot getSnapshot();

    @NonNull Dungeon getDungeon();

    @NonNull UUID getUniqueId();

//    boolean isMob(@NonNull MobProvider provider, @NonNull String mobId);
//
//    boolean isMob(@NonNull MobIdentifier identifier);
//
//    boolean isId(@NonNull String mobId);
//
//    boolean isProvider(@NonNull MobProvider provider);
//
//    boolean isFaction(@NonNull MobFaction faction);

    boolean isDead();

    boolean isAlive();

//    @NonNull String getProviderId();

    @NonNull LivingEntity getBukkitEntity();

//    @NonNull MobFaction getFaction();

    @NonNull MobProvider getProvider();

    @NonNull MobIdentifier getIdentifier();

//    @NonNull String getMobId();
}

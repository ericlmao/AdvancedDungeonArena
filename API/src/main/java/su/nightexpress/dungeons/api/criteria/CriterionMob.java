package su.nightexpress.dungeons.api.criteria;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.mob.MobIdentifier;
import su.nightexpress.dungeons.api.mob.MobProvider;
import su.nightexpress.dungeons.api.type.MobFaction;

public interface CriterionMob {

    boolean isMob(@NonNull MobProvider provider, @NonNull String mobId);

    boolean isMob(@NonNull MobIdentifier identifier);

    boolean isId(@NonNull String mobId);

    boolean isProvider(@NonNull MobProvider provider);

    boolean isProvider(@NonNull String providerId);

    boolean isFaction(@NonNull MobFaction faction);

    @NonNull String getProviderId();

    @NonNull String getMobId();

    @NonNull MobFaction getFaction();

    @NonNull String getBornStageId();
}

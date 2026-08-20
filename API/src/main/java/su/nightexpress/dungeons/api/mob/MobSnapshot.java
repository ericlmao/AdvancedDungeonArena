package su.nightexpress.dungeons.api.mob;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.criteria.CriterionMob;
import su.nightexpress.dungeons.api.type.MobFaction;

// Used as a HashMap key (StageStats) - equality was already structural over these four components.
public record MobSnapshot(@NonNull String providerId, @NonNull String mobId, @NonNull MobFaction faction, @NonNull String bornStageId) implements CriterionMob {

    @Override
    public boolean isMob(@NonNull MobProvider provider, @NonNull String mobId) {
        return this.isProvider(provider) && this.isId(mobId);
    }

    @Override
    public boolean isMob(@NonNull MobIdentifier identifier) {
        return this.isProvider(identifier.providerId()) && this.isId(identifier.mobId());
    }

    @Override
    public boolean isId(@NonNull String mobId) {
        return this.mobId.equalsIgnoreCase(mobId);
    }

    @Override
    public boolean isProvider(@NonNull MobProvider provider) {
        return this.isProvider(provider.getName());
    }

    @Override
    public boolean isProvider(@NonNull String providerId) {
        return this.providerId.equalsIgnoreCase(providerId);
    }

    @Override
    public boolean isFaction(@NonNull MobFaction faction) {
        return this.faction == faction;
    }

    // CriterionMob is published API and is also implemented by DungeonMob, so its getters stay as-is
    // and delegate to the record accessors.

    @Override
    public @NonNull String getProviderId() {
        return this.providerId;
    }

    @Override
    public @NonNull String getMobId() {
        return this.mobId;
    }

    @Override
    public @NonNull MobFaction getFaction() {
        return this.faction;
    }

    @Override
    public @NonNull String getBornStageId() {
        return this.bornStageId;
    }
}

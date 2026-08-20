package su.nightexpress.dungeons.api.mob;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.criteria.CriterionMob;
import su.nightexpress.dungeons.api.type.MobFaction;

import java.util.Objects;

public class MobSnapshot implements CriterionMob {

    private final String     providerId;
    private final String     mobId;
    private final MobFaction faction;
    private final String     bornStageId;

    public MobSnapshot(String providerId, String mobId, MobFaction faction, String bornStageId) {
        this.providerId = providerId;
        this.mobId = mobId;
        this.faction = faction;
        this.bornStageId = bornStageId;
    }

    @Override
    public boolean isMob(@NonNull MobProvider provider, @NonNull String mobId) {
        return this.isProvider(provider) && this.isId(mobId);
    }

    @Override
    public boolean isMob(@NonNull MobIdentifier identifier) {
        return this.isProvider(identifier.getProviderId()) && this.isId(identifier.getMobId());
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

    @NonNull
    public String getProviderId() {
        return this.providerId;
    }

    @NonNull
    public String getMobId() {
        return this.mobId;
    }

    @NonNull
    public MobFaction getFaction() {
        return this.faction;
    }

    @NonNull
    public String getBornStageId() {
        return this.bornStageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MobSnapshot that)) return false;
        return Objects.equals(providerId, that.providerId) && Objects.equals(mobId, that.mobId) && faction == that.faction && Objects.equals(bornStageId, that.bornStageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(providerId, mobId, faction, bornStageId);
    }

    @Override
    public String toString() {
        return "MobSnapshot{" +
            "providerId='" + providerId + '\'' +
            ", mobId='" + mobId + '\'' +
            ", faction=" + faction +
            ", stageId='" + bornStageId + '\'' +
            '}';
    }
}

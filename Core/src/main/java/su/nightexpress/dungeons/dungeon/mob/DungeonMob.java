package su.nightexpress.dungeons.dungeon.mob;

import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.dungeon.DungeonEntity;
import su.nightexpress.dungeons.api.mob.MobIdentifier;
import su.nightexpress.dungeons.api.mob.MobSnapshot;
import su.nightexpress.dungeons.api.type.MobFaction;
import su.nightexpress.dungeons.api.mob.MobProvider;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;

import java.util.UUID;

public class DungeonMob implements DungeonEntity {

    private final DungeonInstance dungeon;
    private final LivingEntity    bukkitEntity;
    private final MobFaction      faction;
    private final MobProvider     provider;
    private final String          mobId;
    private final MobIdentifier   identifier;
    private final MobSnapshot snapshot;

    public DungeonMob(@NonNull DungeonInstance dungeon,
                      @NonNull LivingEntity bukkitEntity,
                      @NonNull MobFaction faction,
                      @NonNull MobProvider provider,
                      @NonNull String mobId) {
        this.dungeon = dungeon;
        this.bukkitEntity = bukkitEntity;
        this.faction = faction;
        this.provider = provider;
        this.mobId = mobId;
        this.identifier = MobIdentifier.from(this.provider, this.mobId);
        this.snapshot = new MobSnapshot(this.getProviderId(), this.getMobId(), this.faction, this.dungeon.getStage().getId());
    }

    @NonNull
    @Override
    public DungeonInstance getDungeon() {
        return this.dungeon;
    }

    @Override
    @NonNull
    public UUID getUniqueId() {
        return this.bukkitEntity.getUniqueId();
    }

    @Override
    @NonNull
    public MobSnapshot getSnapshot() {
        return this.snapshot;
    }

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
        return this.provider == provider;
    }

    @Override
    public boolean isProvider(@NonNull String providerId) {
        return this.provider.getName().equalsIgnoreCase(providerId);
    }

    @Override
    public boolean isFaction(@NonNull MobFaction faction) {
        return this.faction == faction;
    }

    @Override
    public boolean isDead() {
        return this.bukkitEntity.isDead() || !this.bukkitEntity.isValid();
    }

    @Override
    public boolean isAlive() {
        return !this.isDead();
    }

    @Override
    @NonNull
    public MobIdentifier getIdentifier() {
        return this.identifier;
    }

    @Override
    @NonNull
    public String getProviderId() {
        return this.provider.getName();
    }

    @Override
    @NonNull
    public LivingEntity getBukkitEntity() {
        return this.bukkitEntity;
    }

    @Override
    @NonNull
    public MobFaction getFaction() {
        return this.faction;
    }

    @Override
    @NonNull
    public MobProvider getProvider() {
        return this.provider;
    }

    @Override
    @NonNull
    public String getMobId() {
        return this.mobId;
    }

    @Override
    @NonNull
    public String getBornStageId() {
        return this.snapshot.getBornStageId();
    }
}

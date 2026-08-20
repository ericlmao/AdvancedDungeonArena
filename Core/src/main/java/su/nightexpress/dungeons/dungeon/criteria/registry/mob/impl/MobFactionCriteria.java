package su.nightexpress.dungeons.dungeon.criteria.registry.mob.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.criteria.CriterionMob;
import su.nightexpress.dungeons.api.type.MobFaction;
import su.nightexpress.dungeons.dungeon.criteria.CriteriaValidators;
import su.nightexpress.dungeons.dungeon.criteria.registry.mob.MobCriteria;

public class MobFactionCriteria extends MobCriteria<MobFaction> {

    public MobFactionCriteria(@NonNull String name) {
        super(CriteriaValidators.forEnum(MobFaction.class), name);
    }

    @Override
    public boolean test(@NonNull CriterionMob mob, @NonNull MobFaction faction) {
        return mob.isFaction(faction);
    }
}

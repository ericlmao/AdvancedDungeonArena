package su.nightexpress.dungeons.dungeon.criteria.registry.mob.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.criteria.CriterionMob;
import su.nightexpress.dungeons.dungeon.criteria.CriteriaValidators;
import su.nightexpress.dungeons.dungeon.criteria.registry.mob.MobCriteria;

public class MobIdCriteria extends MobCriteria<String> {

    public MobIdCriteria(@NonNull String name) {
        super(CriteriaValidators.STRING, name);
    }

    @Override
    public boolean test(@NonNull CriterionMob mob, @NonNull String mobId) {
        return mob.isId(mobId);
    }
}

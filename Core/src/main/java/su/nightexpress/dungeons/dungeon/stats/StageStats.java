package su.nightexpress.dungeons.dungeon.stats;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.criteria.CriterionMob;
import su.nightexpress.dungeons.api.dungeon.DungeonEntity;
import su.nightexpress.dungeons.api.mob.MobSnapshot;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class StageStats {

    private final Map<MobSnapshot, MobStats> mobStats;

    public StageStats() {
        this.mobStats = new ConcurrentHashMap<>();
    }

    public void clear() {
        this.mobStats.clear();
    }

    @NonNull
    public MobStats getMobStats(@NonNull DungeonEntity entity) {
        MobSnapshot snapshot = entity.getSnapshot();
        return this.mobStats.computeIfAbsent(snapshot, k -> new MobStats());
    }

    @NonNull
    public List<MobStats> queryMobStats(@NonNull Predicate<CriterionMob> predicate) {
        return this.mobStats.entrySet().stream().filter(entry -> predicate.test(entry.getKey())).map(Map.Entry::getValue).toList();
    }

    public int countMobKills(@NonNull Predicate<CriterionMob> predicate) {
        return this.countMobs(predicate, MobStats::getKilledAmount);
    }

    public int countMobSpawns(@NonNull Predicate<CriterionMob> predicate) {
        return this.countMobs(predicate, MobStats::getSpawnedAmount);
    }

    public int countMobs(@NonNull Predicate<CriterionMob> predicate, @NonNull Function<MobStats, Integer> function) {
        return this.queryMobStats(predicate).stream().mapToInt(function::apply).sum();
    }

    public void addMobKill(@NonNull DungeonEntity mob) {
        this.getMobStats(mob).addKill();
    }

    public void addMobSpawn(@NonNull DungeonEntity mob) {
        this.getMobStats(mob).addSpawn();
    }
}

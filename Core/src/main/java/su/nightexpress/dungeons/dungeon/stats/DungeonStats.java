package su.nightexpress.dungeons.dungeon.stats;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.criteria.CriterionMob;
import su.nightexpress.dungeons.api.dungeon.DungeonEntity;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.stage.Stage;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class DungeonStats {

    private final DungeonInstance         dungeon;
    private final Map<String, StageStats> stats;

    public DungeonStats(@NonNull DungeonInstance dungeon) {
        this.dungeon = dungeon;
        // Mob spawn and kill bookkeeping arrives from region and entity threads, not just the clock.
        this.stats = new ConcurrentHashMap<>();
    }

    public void clear() {
        this.stats.clear();
    }

    @NonNull
    public StageStats getStageStats(@NonNull Stage stage) {
        return this.getStageStats(stage.getId());
    }

    @NonNull
    public StageStats getStageStats(@NonNull String stageId) {
        return this.stats.computeIfAbsent(stageId.toLowerCase(), k -> new StageStats());
    }


    @NonNull
    public List<StageStats> queryStageStats(@NonNull Predicate<Stage> stageTest) {
        return this.dungeon.getConfig().getStages().stream().filter(stageTest).map(this::getStageStats).toList();
    }

    @NonNull
    public List<MobStats> queryMobStats(@NonNull Predicate<CriterionMob> mobTest) {
        return this.queryMobStats(stage -> true, mobTest);
    }

    @NonNull
    public List<MobStats> queryMobStats(@NonNull Predicate<Stage> stageTest, @NonNull Predicate<CriterionMob> mobTest) {
        return this.queryStageStats(stageTest).stream()
            .flatMap(stageStats -> stageStats.queryMobStats(mobTest).stream())
            .toList();
    }


    public int countMobKills(@NonNull Predicate<Stage> stageTest, @NonNull Predicate<CriterionMob> predicate) {
        return this.countMobs(stageTest, predicate, MobStats::getKilledAmount);
    }

    public int countMobSpawns(@NonNull Predicate<Stage> stageTest, @NonNull Predicate<CriterionMob> predicate) {
        return this.countMobs(stageTest, predicate, MobStats::getSpawnedAmount);
    }

    public int countMobs(@NonNull Predicate<Stage> stageTest, @NonNull Predicate<CriterionMob> predicate, @NonNull Function<MobStats, Integer> function) {
        return this.queryMobStats(stageTest, predicate).stream().mapToInt(function::apply).sum();
    }

    public void addMobKill(@NonNull DungeonEntity mob) {
        this.getStageStats(this.dungeon.getStage()).addMobKill(mob);
    }

    public void addMobSpawn(@NonNull DungeonEntity mob) {
        this.getStageStats(this.dungeon.getStage()).addMobSpawn(mob);
    }
}

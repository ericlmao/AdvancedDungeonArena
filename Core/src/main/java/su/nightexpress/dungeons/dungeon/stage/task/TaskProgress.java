package su.nightexpress.dungeons.dungeon.stage.task;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.api.dungeon.DungeonPlayer;
import su.nightexpress.dungeons.dungeon.script.task.ProgressFormatter;

public interface TaskProgress {

    @NonNull ProgressFormatter getFormatter();

    @NonNull String format(@Nullable Player player);

    void onPlayerJoined(@NonNull DungeonPlayer player);

    void onPlayerLeft(@NonNull DungeonPlayer player);

    void addProgress(int amount);

    void addProgress(@Nullable Player player, int amount);

    void resetProgress();

    void resetProgress(@Nullable Player player);

    int countProgress();

    int countProgress(@Nullable Player player);

    int countLeftover();

    int countLeftover(@Nullable Player player);

    boolean isEmpty();

    boolean isCompleted();

    int getRequiredAmount();

    int getRequiredAmount(@Nullable Player player);

    void setRequiredAmount(int requiredAmount);
}

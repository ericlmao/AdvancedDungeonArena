package su.nightexpress.dungeons.dungeon.stage.task;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.api.dungeon.DungeonPlayer;
import su.nightexpress.dungeons.dungeon.script.task.ProgressFormatter;

public class GlobalProgress extends AbstractProgress {

    private int count;

    public GlobalProgress(@NonNull ProgressFormatter formatter, int requiredAmount) {
        super(formatter, requiredAmount);
    }

    @Override
    public void onPlayerJoined(@NonNull DungeonPlayer player) {

    }

    @Override
    public void onPlayerLeft(@NonNull DungeonPlayer player) {

    }

    @Override
    public int getRequiredAmount(@Nullable Player player) {
        return this.getRequiredAmount();
    }

    @Override
    public void addProgress(@Nullable Player player, int amount) {
        this.addProgress(amount);
    }

    @Override
    public void addProgress(int amount) {
        this.count += Math.max(0, amount);
    }

    @Override
    public void resetProgress(@Nullable Player player) {
        this.resetProgress();
    }

    @Override
    public void resetProgress() {
        this.count = 0;
    }

    @Override
    public int countProgress(@Nullable Player player) {
        return this.countProgress();
    }

    @Override
    public int countProgress() {
        return this.count;
    }

    @Override
    public int countLeftover(@Nullable Player player) {
        return this.countLeftover();
    }
}

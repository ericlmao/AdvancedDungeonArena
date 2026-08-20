package su.nightexpress.dungeons.dungeon.stage.task;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.dungeon.script.task.ProgressFormatter;

public abstract class AbstractProgress implements TaskProgress {

    protected final ProgressFormatter formatter;
    protected int requiredAmount;

    public AbstractProgress(@NonNull ProgressFormatter formatter, int requiredAmount) {
        this.formatter = formatter;
        this.requiredAmount = requiredAmount;
    }

    @NonNull
    public ProgressFormatter getFormatter() {
        return this.formatter;
    }

    @NonNull
    @Override
    public String format(@Nullable Player player) {
        return this.formatter.format(this, player);
    }

    @Override
    public int countLeftover() {
        return Math.max(0, this.getRequiredAmount() - this.countProgress());
    }

    @Override
    public boolean isEmpty() {
        return this.requiredAmount <= 0;
    }

    @Override
    public boolean isCompleted() {
        return this.isEmpty() || this.countProgress() >= this.getRequiredAmount();
    }

    @Override
    public int getRequiredAmount() {
        return this.requiredAmount;
    }

    @Override
    public void setRequiredAmount(int requiredAmount) {
        this.requiredAmount = requiredAmount;
    }
}

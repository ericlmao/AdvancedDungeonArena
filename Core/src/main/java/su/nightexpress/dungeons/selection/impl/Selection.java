package su.nightexpress.dungeons.selection.impl;

import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.selection.SelectionType;
import su.nightexpress.dungeons.nightcore.util.geodata.pos.BlockPos;

public abstract class Selection {

    protected final SelectionType type;

    public Selection(@NonNull SelectionType type) {
        this.type = type;
    }

    @NonNull
    public static Selection create(@NonNull SelectionType type) {
        return switch (type) {
            case CUBOID -> new CuboidSelection();
            case POSITION -> new PositionSelection();
        };
    }

    public abstract void clear();

    public abstract boolean isIncompleted();

    public abstract void onSelect(@NonNull Player player, @NonNull BlockPos pos, @NonNull Action action);

    @NonNull
    public SelectionType getType() {
        return this.type;
    }
}

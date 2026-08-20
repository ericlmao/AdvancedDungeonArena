package su.nightexpress.dungeons.selection.impl;

import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.Placeholders;
import su.nightexpress.dungeons.config.Lang;
import su.nightexpress.dungeons.selection.SelectionType;
import su.nightexpress.dungeons.nightcore.util.geodata.Cuboid;
import su.nightexpress.dungeons.nightcore.util.geodata.pos.BlockPos;

public class CuboidSelection extends Selection {

    private BlockPos first;
    private BlockPos second;

    public CuboidSelection() {
        super(SelectionType.CUBOID);
    }

    @Override
    public void clear() {
        this.setFirst(null);
        this.setSecond(null);
    }

    @Override
    public boolean isIncompleted() {
        return this.first == null || this.second == null;
    }

    @Override
    public void onSelect(@NonNull Player player, @NonNull BlockPos pos, @NonNull Action action) {
        int value;
        if (action == Action.LEFT_CLICK_BLOCK) {
            this.setFirst(pos);
            value = 1;
        }
        else {
            this.setSecond(pos);
            value = 2;
        }

        Lang.SELECTION_INFO_CUBOID.message().send(player, replacer -> replacer.replace(Placeholders.GENERIC_VALUE, value));
    }

    @Nullable
    public BlockPos getFirst() {
        return this.first;
    }

    public void setFirst(@Nullable BlockPos first) {
        this.first = first;
    }

    @Nullable
    public BlockPos getSecond() {
        return this.second;
    }

    public void setSecond(@Nullable BlockPos second) {
        this.second = second;
    }

    @Nullable
    public Cuboid toCuboid() {
        return this.isIncompleted() ? null : new Cuboid(this.first, this.second);
    }
}

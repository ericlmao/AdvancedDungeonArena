package su.nightexpress.dungeons.selection.visual;

import org.bukkit.block.data.BlockData;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.nightcore.util.geodata.pos.BlockPos;

public class BlockInfo {

    private final BlockPos  blockPos;
    private final BlockData blockData;

    public BlockInfo(@NonNull BlockPos blockPos, @NonNull BlockData blockData) {
        this.blockPos = blockPos;
        this.blockData = blockData;
    }

    @NonNull
    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    @NonNull
    public BlockData getBlockData() {
        return this.blockData;
    }
}

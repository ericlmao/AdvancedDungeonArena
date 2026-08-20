package su.nightexpress.dungeons.api.schema;

import org.bukkit.block.data.BlockData;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.nightcore.util.geodata.pos.BlockPos;

public class SchemaBlock {

    private final BlockPos  blockPos;
    private final BlockData blockData;
    private final Object    nbt;

    public SchemaBlock(@NonNull BlockPos blockPos, @NonNull BlockData blockData, @Nullable Object nbt) {
        this.blockPos = blockPos;
        this.blockData = blockData;
        this.nbt = nbt;
    }

    @NonNull
    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    @NonNull
    public BlockData getBlockData() {
        return this.blockData;
    }

    @Nullable
    public Object getNbt() {
        return this.nbt;
    }
}

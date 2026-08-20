package su.nightexpress.dungeons.api.schema;

import org.bukkit.block.data.BlockData;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.nightcore.util.geodata.pos.BlockPos;

public record SchemaBlock(@NonNull BlockPos blockPos, @NonNull BlockData blockData, @Nullable Object nbt) {

}

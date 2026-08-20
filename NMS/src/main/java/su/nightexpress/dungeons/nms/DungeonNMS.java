package su.nightexpress.dungeons.nms;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.dungeons.api.schema.SchemaBlock;

import java.io.File;
import java.util.List;

public interface DungeonNMS {

    void setSchemaBlock(@NotNull World world, @NotNull SchemaBlock schemaBlock);

    @NotNull List<SchemaBlock> loadSchema(@NotNull File file, boolean compressed);

    void saveSchema(@NotNull World world, @NotNull List<Block> blocks, @NotNull File file);
}

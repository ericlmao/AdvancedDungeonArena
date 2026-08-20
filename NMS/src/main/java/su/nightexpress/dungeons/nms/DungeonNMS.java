package su.nightexpress.dungeons.nms;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.schema.SchemaBlock;

import java.io.File;
import java.util.List;

public interface DungeonNMS {

    void setSchemaBlock(@NonNull World world, @NonNull SchemaBlock schemaBlock);

    @NonNull List<SchemaBlock> loadSchema(@NonNull File file, boolean compressed);

    void saveSchema(@NonNull World world, @NonNull List<Block> blocks, @NonNull File file);
}

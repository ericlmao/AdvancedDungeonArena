package su.nightexpress.dungeons.dungeon.spot;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.Placeholders;
import su.nightexpress.dungeons.api.schema.SchemaBlock;
import su.nightexpress.dungeons.nightcore.config.ConfigValue;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.config.Writeable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class SpotState implements Writeable {

    private final String id;

    private List<SchemaBlock> schema;

    public SpotState(@NonNull String id) {
        this.id = id.toLowerCase();
        this.schema = new ArrayList<>();
    }

    @NonNull
    public static SpotState read(@NonNull FileConfig config, @NonNull String path, @NonNull String id) {
        ConfigValue.create(path + ".Enabled", true).read(config);

        return new SpotState(id);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".Enabled", true);
    }

    public void loadSchema(@NonNull DungeonPlugin plugin, @NonNull File file, boolean compressed) {
        this.setSchema(plugin.getInternals().loadSchema(file, compressed));
    }

    @NonNull
    public UnaryOperator<String> replacePlaceholders() {
        return Placeholders.SPOT_STATE.replacer(this);
    }

    @NonNull
    public String getId() {
        return this.id;
    }

    @NonNull
    public List<SchemaBlock> getSchema() {
        return this.schema;
    }

    public void setSchema(@NonNull List<SchemaBlock> schema) {
        this.schema = schema;
    }
}

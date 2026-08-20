package su.nightexpress.dungeons.dungeon.feature.board;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.config.Writeable;

import java.util.List;

public record BoardLayout(/*@NonNull String id, */@NonNull String title, @NonNull List<String> lines) implements Writeable {

    public BoardLayout {
        // The accessor hands the list straight out, where the old getter returned a copy. Copy on the way
        // in instead, so a layout is genuinely immutable rather than defensive at every read.
        lines = List.copyOf(lines);
    }

    @NonNull
    public static BoardLayout read(@NonNull FileConfig config, @NonNull String path/*, @NonNull String id*/) {
        String title = config.getString(path +  ".Title", "");
        List<String> lines = config.getStringList(path + ".List");
        return new BoardLayout(/*id, */title, lines);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".Title", this.title);
        config.set(path + ".List", this.lines);
    }
}

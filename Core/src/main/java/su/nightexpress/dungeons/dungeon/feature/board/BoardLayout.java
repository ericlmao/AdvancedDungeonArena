package su.nightexpress.dungeons.dungeon.feature.board;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.config.Writeable;

import java.util.ArrayList;
import java.util.List;

public class BoardLayout implements Writeable {

    //private final String       id;
    private final String       title;
    private final List<String> lines;

    public BoardLayout(/*@NonNull String id, */@NonNull String title, @NonNull List<String> lines) {
        //this.id = id.toLowerCase();
        this.title = title;
        this.lines = lines;
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

//    @NonNull
//    public String getId() {
//        return id;
//    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public List<String> getLines() {
        return new ArrayList<>(lines);
    }
}

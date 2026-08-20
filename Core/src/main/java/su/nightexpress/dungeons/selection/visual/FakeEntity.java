package su.nightexpress.dungeons.selection.visual;

import org.jspecify.annotations.NonNull;

import java.util.UUID;

public class FakeEntity {

    private final int  id;
    private final UUID uuid;

    public FakeEntity(int id, @NonNull UUID uuid) {
        this.id = id;
        this.uuid = uuid;
    }

    public int getId() {
        return id;
    }

    @NonNull
    public UUID getUUID() {
        return uuid;
    }
}

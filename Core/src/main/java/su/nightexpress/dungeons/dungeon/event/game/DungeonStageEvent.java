package su.nightexpress.dungeons.dungeon.event.game;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.DungeonEventType;
import su.nightexpress.dungeons.dungeon.stage.Stage;

public abstract class DungeonStageEvent extends DungeonGameEvent implements StageEvent {

    private final Stage stage;

    public DungeonStageEvent(@NonNull DungeonEventType type, @NonNull DungeonInstance dungeon, @NonNull Stage stage) {
        super(type, dungeon);
        this.stage = stage;
    }

    @Override
    @NonNull
    public Stage getStage() {
        return this.stage;
    }
}

package su.nightexpress.dungeons.dungeon.event.game;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.stage.Stage;

public interface StageEvent {

    @NonNull Stage getStage();
}

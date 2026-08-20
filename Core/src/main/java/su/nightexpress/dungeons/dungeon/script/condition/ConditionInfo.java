package su.nightexpress.dungeons.dungeon.script.condition;

import org.jspecify.annotations.NonNull;

public record ConditionInfo(boolean cached, @NonNull Condition condition) {

}

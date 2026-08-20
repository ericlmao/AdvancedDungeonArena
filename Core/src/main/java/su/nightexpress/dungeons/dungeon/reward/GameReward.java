package su.nightexpress.dungeons.dungeon.reward;

import org.jspecify.annotations.NonNull;

public record GameReward(@NonNull Reward reward, boolean keepOnDeath, boolean keepOnDefeat) {

}

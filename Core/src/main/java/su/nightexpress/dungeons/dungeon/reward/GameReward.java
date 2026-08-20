package su.nightexpress.dungeons.dungeon.reward;

import org.jspecify.annotations.NonNull;

public class GameReward {

    private final Reward  reward;
    private final boolean keepOnDeath;
    private final boolean keepOnDefeat;

    public GameReward(@NonNull Reward reward, boolean keepOnDeath, boolean keepOnDefeat) {
        this.reward = reward;
        this.keepOnDeath = keepOnDeath;
        this.keepOnDefeat = keepOnDefeat;
    }

    @NonNull
    public Reward getReward() {
        return this.reward;
    }

    public boolean isKeepOnDeath() {
        return this.keepOnDeath;
    }

    public boolean isKeepOnDefeat() {
        return this.keepOnDefeat;
    }
}

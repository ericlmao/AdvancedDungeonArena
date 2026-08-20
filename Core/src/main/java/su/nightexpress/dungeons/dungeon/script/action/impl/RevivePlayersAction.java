package su.nightexpress.dungeons.dungeon.script.action.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.dungeon.script.action.Action;
import su.nightexpress.dungeons.dungeon.script.action.ActionId;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.util.wrapper.UniInt;

import java.util.concurrent.TimeUnit;

public class RevivePlayersAction implements Action {

    private final boolean checkDeathTime;
    private final UniInt  secondsSinceDeath;

    public RevivePlayersAction(boolean checkDeathTime, @NonNull UniInt secondsSinceDeath) {
        this.checkDeathTime = checkDeathTime;
        this.secondsSinceDeath = secondsSinceDeath;
    }

    @NonNull
    public static RevivePlayersAction load(@NonNull FileConfig config, @NonNull String path) {
        boolean checkDeathTime = config.getBoolean(path + ".Check_Death_Time", false);
        UniInt secondsSinceDeath = UniInt.read(config, path + ".Seconds_Since_Death");

        return new RevivePlayersAction(checkDeathTime, secondsSinceDeath);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".Check_Death_Time", this.checkDeathTime);
        this.secondsSinceDeath.write(config, path + ".Seconds_Since_Death");
    }

    @NonNull
    @Override
    public String getName() {
        return ActionId.REVIVE_PLAYERS;
    }

    @Override
    public void perform(@NonNull DungeonInstance dungeon, @NonNull DungeonGameEvent event) {
        if (dungeon.isAboutToEnd()) return;

        dungeon.getDeadPlayers().forEach(gamer -> {
            if (!gamer.hasExtraLives()) return;

            if (this.checkDeathTime) {
                long deathTime = gamer.getDeathTime();
                long difference = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - deathTime);

                int min = this.secondsSinceDeath.getMinValue();
                int max = this.secondsSinceDeath.getMaxValue();

                if (min > 0 && difference < min) return;
                if (max > 0 && difference > max) return;

//                if (!TimeUtil.isPassed(deathTime + TimeUnit.SECONDS.toMillis(this.secondsSinceDeath))) {
//                    Bukkit.broadcastMessage("Its too early to revive " + gamer.getPlayer().getName());
//                    return;
//                }
            }

            gamer.revive();
        });
    }
}

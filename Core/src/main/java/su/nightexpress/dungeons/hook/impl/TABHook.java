package su.nightexpress.dungeons.hook.impl;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.scoreboard.ScoreboardManager;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.compat.BoardPlugin;

import java.util.function.BiFunction;

public class TABHook implements BoardPlugin {

    @Override
    public boolean isBoardEnabled(@NonNull Player player) {
        return this.manageScoreboard(player, (tabPlayer, manager) -> manager.hasCustomScoreboard(tabPlayer) || manager.getActiveScoreboard(tabPlayer) != null);
    }

    @Override
    public void disableBoard(@NonNull Player player) {
        this.manageScoreboard(player, (tabPlayer, manager) -> {
            manager.setScoreboardVisible(tabPlayer, false, false);
            return true;
        });
    }

    @Override
    public void enableBoard(@NonNull Player player) {
        this.manageScoreboard(player, (tabPlayer, manager) -> {
            manager.setScoreboardVisible(tabPlayer, true, false);
            return true;
        });
    }

    private boolean manageScoreboard(@NonNull Player player, @NonNull BiFunction<TabPlayer, ScoreboardManager, Boolean> consumer) {
        ScoreboardManager manager = TabAPI.getInstance().getScoreboardManager();
        if (manager == null) return false;

        TabPlayer tabPlayer = TabAPI.getInstance().getPlayer(player.getUniqueId());
        if (tabPlayer == null) return false;

        return consumer.apply(tabPlayer, manager);
    }
}

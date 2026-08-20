package su.nightexpress.dungeons.registry.level.provider;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.hook.HookId;
import su.nightexpress.dungeons.registry.level.LevelProvider;

public class AuroraLevelsProvider implements LevelProvider {

    @NonNull
    @Override
    public String getName() {
        return HookId.AURORA_LEVELS;
    }

    @Override
    public int getLevel(@NonNull Player player) {
        return gg.auroramc.levels.api.AuroraLevelsProvider.getLeveler().getUserData(player).getLevel();
    }
}

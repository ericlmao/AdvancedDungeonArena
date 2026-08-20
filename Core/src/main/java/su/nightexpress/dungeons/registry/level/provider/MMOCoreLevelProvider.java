package su.nightexpress.dungeons.registry.level.provider;

import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.hook.HookId;
import su.nightexpress.dungeons.registry.level.LevelProvider;

public class MMOCoreLevelProvider implements LevelProvider {

    @NonNull
    @Override
    public String getName() {
        return HookId.MMOCORE;
    }

    @Override
    public int getLevel(@NonNull Player player) {
        return PlayerData.get(player).getLevel();
    }
}

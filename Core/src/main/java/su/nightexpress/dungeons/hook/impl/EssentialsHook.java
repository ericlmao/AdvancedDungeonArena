package su.nightexpress.dungeons.hook.impl;

import com.earth2me.essentials.Essentials;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.compat.GodPlugin;
import su.nightexpress.dungeons.hook.HookId;

public class EssentialsHook implements GodPlugin {

    private final Essentials essentials;

    public EssentialsHook() {
        this.essentials = (Essentials) Bukkit.getPluginManager().getPlugin(HookId.ESSENTIALS);
    }

    @Override
    public boolean isGodEnabled(@NonNull Player player) {
        return this.essentials.getUser(player).isGodModeEnabled();
    }

    public void disableGod(@NonNull Player player) {
        this.essentials.getUser(player).setGodModeEnabled(false);
    }

    @Override
    public void enableGod(@NonNull Player player) {
        this.essentials.getUser(player).setGodModeEnabled(true);
    }
}

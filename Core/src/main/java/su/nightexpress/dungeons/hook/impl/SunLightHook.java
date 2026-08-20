package su.nightexpress.dungeons.hook.impl;

import java.lang.reflect.Method;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nightexpress.dungeons.api.compat.BoardPlugin;
import su.nightexpress.dungeons.api.compat.GodPlugin;
import su.nightexpress.dungeons.hook.HookId;

/**
 * SunLight integration.
 * <p>
 * The god half is plain Bukkit. The scoreboard half is reflective on purpose: SunLight's
 * {@code SunLightPlugin}, {@code SunUser} and {@code ScoreboardModule} all extend
 * {@code su.nightexpress.nightcore.*} types, so calling them directly would drag the nightcore artifact
 * back onto this module's compile classpath - the exact dependency this plugin no longer has. SunLight
 * still ships nightcore itself, so every lookup below resolves at runtime on a server that has both.
 */
public class SunLightHook implements GodPlugin, BoardPlugin {

    private static final String CLASS_SCOREBOARD_MODULE = "su.nightexpress.sunlight.module.scoreboard.ScoreboardModule";
    private static final String CLASS_SCOREBOARD_PROPS  = "su.nightexpress.sunlight.module.scoreboard.ScoreboardProperties";

    @Override
    public boolean isGodEnabled(@NotNull Player player) {
        return player.isInvulnerable();
    }

    @Override
    public void disableGod(@NotNull Player player) {
        player.setInvulnerable(false);
    }

    @Override
    public void enableGod(@NotNull Player player) {
        player.setInvulnerable(true);
    }

    @Override
    public boolean isBoardEnabled(@NotNull Player player) {
        Object user = this.getUser(player);
        if (user == null) return false;

        try {
            Class<?> properties = Class.forName(CLASS_SCOREBOARD_PROPS);
            Object property = properties.getField("SCOREBOARD").get(null);

            Method getProperty = findMethod(user.getClass(), "getPropertyOrDefault", 1);
            if (getProperty == null) return false;

            return Boolean.TRUE.equals(getProperty.invoke(user, property));
        }
        catch (ReflectiveOperationException | RuntimeException exception) {
            return false;
        }
    }

    @Override
    public void disableBoard(@NotNull Player player) {
        this.callBoardModule("removeBoard", player);
    }

    @Override
    public void enableBoard(@NotNull Player player) {
        this.callBoardModule("addBoard", player);
    }

    private void callBoardModule(@NotNull String methodName, @NotNull Player player) {
        Object module = this.getScoreboardModule();
        if (module == null) return;

        try {
            Method method = findMethod(module.getClass(), methodName, 1);
            if (method != null) method.invoke(module, player);
        }
        catch (ReflectiveOperationException | RuntimeException ignored) {
            // SunLight changed its module API; treat the integration as unavailable.
        }
    }

    @Nullable
    private Object getScoreboardModule() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(HookId.SUNLIGHT);
        if (plugin == null) return null;

        try {
            Method getRegistry = findMethod(plugin.getClass(), "getModuleRegistry", 0);
            if (getRegistry == null) return null;

            Object registry = getRegistry.invoke(plugin);
            Method byType = findMethod(registry.getClass(), "byType", 1);
            if (byType == null) return null;

            Object result = byType.invoke(registry, Class.forName(CLASS_SCOREBOARD_MODULE));
            return result instanceof Optional<?> optional ? optional.orElse(null) : result;
        }
        catch (ReflectiveOperationException | RuntimeException exception) {
            return null;
        }
    }

    @Nullable
    private Object getUser(@NotNull Player player) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(HookId.SUNLIGHT);
        if (plugin == null) return null;

        try {
            Method getUserManager = findMethod(plugin.getClass(), "getUserManager", 0);
            if (getUserManager == null) return null;

            Object userManager = getUserManager.invoke(plugin);
            Method getOrFetch = findMethod(userManager.getClass(), "getOrFetch", 1);
            if (getOrFetch == null) return null;

            return getOrFetch.invoke(userManager, player);
        }
        catch (ReflectiveOperationException | RuntimeException exception) {
            return null;
        }
    }

    @Nullable
    private static Method findMethod(@NotNull Class<?> owner, @NotNull String name, int parameterCount) {
        for (Class<?> clazz = owner; clazz != null; clazz = clazz.getSuperclass()) {
            for (Method method : clazz.getMethods()) {
                if (method.getName().equals(name) && method.getParameterCount() == parameterCount) {
                    method.setAccessible(true);
                    return method;
                }
            }
        }
        return null;
    }
}

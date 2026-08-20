package su.nightexpress.dungeons.nightcore;

import org.jspecify.annotations.NonNull;

import su.nightexpress.dungeons.nightcore.core.tag.TagBootstrap;
import su.nightexpress.dungeons.nightcore.integration.currency.CurrencyManager;
import su.nightexpress.dungeons.nightcore.ui.UIListener;
import su.nightexpress.dungeons.nightcore.ui.UIUtils;
import su.nightexpress.dungeons.nightcore.util.Plugins;
import su.nightexpress.dungeons.nightcore.util.profile.PlayerProfiles;

/**
 * Re-hosts the global services that the standalone NightCore plugin used to initialise.
 * <p>
 * Without these, the vendored code fails in ways that look unrelated to their cause: tags render as literal
 * text, {@code EconomyBridge} returns no currencies, menus stop responding to clicks, and
 * {@code UIUtils.openConfirmation} silently does nothing.
 */
public class CoreBootstrap {

    private static UIListener uiListener;

    private CoreBootstrap() {
    }

    /**
     * Called once, before any config is read.
     */
    public static void init(@NonNull NightPlugin plugin) {
        Plugins.detectPlugins();
        TagBootstrap.load();
    }

    /**
     * Called on every enable/reload cycle, after config + locale are loaded and before the plugin's own
     * {@code enable()} body runs.
     */
    public static void enable(@NonNull NightPlugin plugin) {
        CurrencyManager.load(plugin);

        UIUtils.load(plugin);

        uiListener = new UIListener(plugin);
        uiListener.registerListeners();
    }

    /**
     * Called on every disable/reload cycle.
     */
    public static void disable() {
        if (uiListener != null) {
            uiListener.unregisterListeners();
            uiListener = null;
        }

        UIUtils.clear();
        CurrencyManager.unload();
        PlayerProfiles.clear();
    }

    /**
     * Called once, on the final plugin disable.
     */
    public static void shutdown() {
        TagBootstrap.unload();
    }
}

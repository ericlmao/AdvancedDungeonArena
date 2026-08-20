package su.nightexpress.dungeons.nightcore.manager;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.nightcore.NightPlugin;

public abstract class AbstractListener<P extends NightPlugin> implements SimpleListener {

    @NonNull
    public final P plugin;

    public AbstractListener(@NonNull P plugin) {
        this.plugin = plugin;
    }

    @Override
    public void registerListeners() {
        this.plugin.getPluginManager().registerEvents(this, this.plugin);
    }
}

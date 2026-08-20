package su.nightexpress.dungeons.nightcore.manager;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.nightcore.NightPlugin;

public abstract class SimpleManager<P extends NightPlugin> {

    protected final P plugin;

    public SimpleManager(@NonNull P plugin) {
        this.plugin = plugin;
    }

    @NonNull
    public P plugin() {
        return this.plugin;
    }

    public void setup() {
        this.onLoad();
    }

    public void shutdown() {
        this.onShutdown();
    }

    public void reload() {
        this.shutdown();
        this.setup();
    }

    protected abstract void onLoad();

    protected abstract void onShutdown();
}

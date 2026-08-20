package su.nightexpress.dungeons.nightcore.ui.menu.data;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.ui.menu.Menu;

@Deprecated
public interface ConfigBased extends Menu {

    default void load(@NonNull FileConfig config) {
        MenuLoader loader = new MenuLoader(this, config);
        loader.loadSettings();
        this.loadConfiguration(config, loader);
        loader.loadItems();
        loader.loadComments();
        config.saveChanges();
    }

    void loadConfiguration(@NonNull FileConfig config, @NonNull MenuLoader loader);
}

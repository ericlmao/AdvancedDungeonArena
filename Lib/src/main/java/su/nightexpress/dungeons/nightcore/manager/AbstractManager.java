package su.nightexpress.dungeons.nightcore.manager;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.nightcore.NightPlugin;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.ui.menu.Menu;
import su.nightexpress.dungeons.nightcore.ui.menu.data.ConfigBased;
import su.nightexpress.dungeons.nightcore.util.bukkit.NightTask;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractManager<P extends NightPlugin> extends SimpleManager<P> {

    protected final Set<SimpeListener> listeners;
    @Deprecated protected final Set<Menu> menus;
    protected final List<NightTask> taskList;

    public AbstractManager(@NonNull P plugin) {
        super(plugin);
        this.listeners = new HashSet<>();
        this.menus = new HashSet<>();
        this.taskList = new ArrayList<>();
    }

    @Override
    public void shutdown() {
        this.taskList.forEach(NightTask::stop);
        this.taskList.clear();
        this.menus.forEach(Menu::clear);
        this.menus.clear();
        this.listeners.forEach(SimpeListener::unregisterListeners);
        this.listeners.clear();
        super.shutdown();
    }

    protected void addListener(@NonNull SimpeListener listener) {
        if (this.listeners.add(listener)) {
            listener.registerListeners();
        }
    }

    @NonNull
    @Deprecated
    protected <T extends Menu> T addMenu(@NonNull T menu) {
        this.menus.add(menu);
        return menu;
    }

    protected void addTask(@NonNull Runnable runnable, int interval) {
        this.addTask(NightTask.create(plugin, runnable, interval));
    }

    protected void addTask(@NonNull Runnable runnable, long interval) {
        this.addTask(NightTask.create(plugin, runnable, interval));
    }

    protected void addAsyncTask(@NonNull Runnable runnable, int interval) {
        this.addTask(NightTask.createAsync(plugin, runnable, interval));
    }

    protected void addAsyncTask(@NonNull Runnable runnable, long interval) {
        this.addTask(NightTask.createAsync(plugin, runnable, interval));
    }

    protected void addTask(@NonNull NightTask task) {
        if (task.isValid()) {
            this.taskList.add(task);
        }
    }
}

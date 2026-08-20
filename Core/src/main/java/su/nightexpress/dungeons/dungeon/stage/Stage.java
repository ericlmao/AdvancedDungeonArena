package su.nightexpress.dungeons.dungeon.stage;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.Placeholders;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.dungeon.event.DungeonEventHandler;
import su.nightexpress.dungeons.dungeon.event.DungeonEventReceiver;
import su.nightexpress.dungeons.dungeon.event.DungeonEventType;
import su.nightexpress.dungeons.util.ErrorHandler;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.manager.AbstractFileData;
import su.nightexpress.dungeons.nightcore.util.StringUtil;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

public class Stage extends AbstractFileData<DungeonPlugin> implements DungeonEventReceiver {

    private final Map<String, StageTask>           taskMap;
    private final Map<String, DungeonEventHandler> handlerMap;

    private String displayName;
    private String description;

    public Stage(@NonNull DungeonPlugin plugin, @NonNull File file) {
        super(plugin, file);

        this.taskMap = new LinkedHashMap<>();
        this.handlerMap = new LinkedHashMap<>();
    }

    @Override
    protected boolean onLoad(@NonNull FileConfig config) {
        this.setDisplayName(config.getString("Name", StringUtil.capitalizeUnderscored(this.getId())));
        this.setDescription(config.getString("Description", ""));

        config.getSection("Tasks").forEach(sId -> {
            StageTask stageTask = StageTask.read(config, "Tasks." + sId, sId);
            if (stageTask == null) {
                ErrorHandler.error("Stage task '" + sId + "' not loaded due to errors.", config, "Tasks." + sId);
                return;
            }

            this.taskMap.put(stageTask.getId(), stageTask);
        });

        config.getSection("EventHandlers").forEach(sId -> {
            DungeonEventHandler handler = DungeonEventHandler.read(config, "EventHandlers." + sId, sId);
            this.addHandler(handler);
        });

        return true;
    }

    @Override
    protected void onSave(@NonNull FileConfig config) {
        config.set("Name", this.displayName);
        config.set("Description", this.description);

        config.remove("Tasks");
        this.taskMap.forEach((id, stageTask) -> config.set("Tasks." + id, stageTask));

        config.remove("EventHandlers");
        this.handlerMap.forEach((id, handler) -> config.set("EventHandlers." + id, handler));
    }

    @Override
    public boolean onDungeonEventBroadcastReceive(@NonNull DungeonGameEvent event, @NonNull DungeonEventType eventType, @NonNull DungeonInstance dungeon) {
        if (!dungeon.isStage(this)) return false;

        this.getEventHandlers().forEach(listener -> listener.handleEvent(event, eventType, dungeon));
        return true;
    }

    @NonNull
    public UnaryOperator<String> replacePlaceholders() {
        return Placeholders.STAGE.replacer(this);
    }

    @Override
    public void addHandler(@NonNull DungeonEventHandler handler) {
        this.handlerMap.put(handler.getId(), handler);
    }

    @NonNull
    public Set<DungeonEventHandler> getEventHandlers() {
        return new HashSet<>(this.handlerMap.values());
    }

    @NonNull
    public Set<StageTask> getTasks() {
        return new HashSet<>(this.taskMap.values());
    }

    @Nullable
    public StageTask getTaskById(@NonNull String id) {
        return this.taskMap.get(id.toLowerCase());
    }

    @NonNull
    public Map<String, DungeonEventHandler> getHandlerMap() {
        return this.handlerMap;
    }

    @NonNull
    public Map<String, StageTask> getTaskMap() {
        return this.taskMap;
    }

    @NonNull
    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(@NonNull String displayName) {
        this.displayName = displayName;
    }

    @NonNull
    public String getDescription() {
        return this.description;
    }

    public void setDescription(@NonNull String description) {
        this.description = description;
    }
}

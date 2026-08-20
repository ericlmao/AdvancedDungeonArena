package su.nightexpress.dungeons.dungeon.level;

import org.bukkit.Location;
import org.bukkit.World;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.Placeholders;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.dungeon.event.DungeonEventHandler;
import su.nightexpress.dungeons.dungeon.event.DungeonEventReceiver;
import su.nightexpress.dungeons.dungeon.event.DungeonEventType;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.manager.AbstractFileData;
import su.nightexpress.dungeons.nightcore.util.geodata.pos.ExactPos;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

public class Level extends AbstractFileData<DungeonPlugin> implements DungeonEventReceiver {

    private final Map<String, DungeonEventHandler> eventHandlers;

    private String displayName;
    private String   description;
    private ExactPos spawnPos;

    public Level(@NonNull DungeonPlugin plugin, @NonNull File file) {
        super(plugin, file);
        this.eventHandlers = new LinkedHashMap<>();
    }

    @Override
    protected boolean onLoad(@NonNull FileConfig config) {
        this.setDisplayName(config.getString("Name", "null"));
        this.setDescription(config.getString("Description", ""));

        this.setSpawnPos(ExactPos.read(config, "SpawnPos"));

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
        this.spawnPos.write(config, "SpawnPos");

        config.remove("EventHandlers");
        this.eventHandlers.forEach((id, handler) -> config.set("EventHandlers." + id, handler));
    }

    @Override
    public void addHandler(@NonNull DungeonEventHandler handler) {
        this.eventHandlers.put(handler.getId(), handler);
    }

    @Override
    public boolean onDungeonEventBroadcastReceive(@NonNull DungeonGameEvent event, @NonNull DungeonEventType eventType, @NonNull DungeonInstance dungeon) {
        if (!dungeon.isLevel(this)) return false;

        //System.out.println("Level event received: " + this.getDisplayName() +" / " + eventType.name());
        this.getEventHandlers().forEach(listener -> listener.handleEvent(event, eventType, dungeon));
        return true;
    }

    @NonNull
    public UnaryOperator<String> replacePlaceholders() {
        return Placeholders.LEVEL.replacer(this);
    }

    @NonNull
    public Location getSpawnLocation(@NonNull World world) {
        return this.spawnPos.toLocation(world);
    }

    @NonNull
    public Set<DungeonEventHandler> getEventHandlers() {
        return new HashSet<>(this.eventHandlers.values());
    }

    @NonNull
    public Map<String, DungeonEventHandler> getEventHandlerMap() {
        return this.eventHandlers;
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

    @NonNull
    public ExactPos getSpawnPos() {
        return this.spawnPos;
    }

    public void setSpawnPos(@NonNull ExactPos spawnPos) {
        this.spawnPos = spawnPos;
    }
}

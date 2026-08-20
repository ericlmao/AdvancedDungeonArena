package su.nightexpress.dungeons.user;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.data.DataHandler;
import su.nightexpress.dungeons.nightcore.userdata.AbstractUserManager;

import java.util.UUID;

public class UserManager extends AbstractUserManager<DungeonPlugin, DungeonUser> {

    public UserManager(@NonNull DungeonPlugin plugin, @NonNull DataHandler dataHandler) {
        super(plugin, dataHandler);
    }

    @Override
    @NonNull
    public DungeonUser create(@NonNull UUID uuid, @NonNull String name) {
        return DungeonUser.create(uuid, name);
    }
}

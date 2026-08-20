package su.nightexpress.dungeons.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.Nullable;

import su.nightexpress.dungeons.user.DungeonUser;

public class DataQueries {

    private DataQueries() {
    }

    @Nullable
    public static DungeonUser readUser(ResultSet resultSet) {
        try {
            UUID uuid = UUID.fromString(resultSet.getString(DataHandler.COLUMN_USER_ID));
            String name = resultSet.getString(DataHandler.COLUMN_USER_NAME);
            long dateCreated = resultSet.getLong(DataHandler.COLUMN_USER_DATE_CREATED);
            long lastOnline = resultSet.getLong(DataHandler.COLUMN_USER_LAST_ONLINE);

            Set<String> kits = DataHandler.GSON.fromJson(resultSet.getString(DataHandler.COLUMN_KITS),
                new TypeToken<Set<String>>() {}.getType());
            Map<String, Long> cooldownMap = DataHandler.GSON.fromJson(resultSet.getString(DataHandler.COLUMN_COOLDOWN),
                new TypeToken<Map<String, Long>>() {}.getType());

            // Upstream only null-guarded the cooldown map; a null kit set NPE'd in the DungeonUser
            // constructor for rows written before the column existed.
            if (kits == null) kits = new HashSet<>();
            if (cooldownMap == null) cooldownMap = new HashMap<>();

            return new DungeonUser(uuid, name, dateCreated, lastOnline, kits, cooldownMap);
        }
        catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    }
}

package su.nightexpress.dungeons.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.data.DatabaseSettings.DatabaseType;
import su.nightexpress.dungeons.nightcore.manager.SimpleManager;
import su.nightexpress.dungeons.nightcore.userdata.UserDataStore;
import su.nightexpress.dungeons.user.DungeonUser;

/**
 * User persistence, on plain JDBC.
 * <p>
 * Replaces {@code nightcore.db.AbstractUserDataManager} and the ~6,000 lines of query-builder,
 * connection-pool and data-layer machinery underneath it. The schema is byte-compatible with the
 * nightcore version - same {@code <prefix>_users} table, same column names (including upstream's
 * inconsistent {@code dateCreated} vs {@code last_online}), same JSON payloads - so an existing
 * {@code data.db} loads unchanged.
 * <p>
 * Connection handling: SQLite keeps one connection open for the plugin's lifetime (upstream pinned the
 * Hikari pool to a single connection anyway); MySQL opens one per operation. All calls come from the
 * async tasks in {@code AbstractUserManager} or from {@code DungeonPlugin}'s enable/disable.
 */
public class DataHandler extends SimpleManager<DungeonPlugin> implements UserDataStore<DungeonUser> {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final String COLUMN_USER_ID           = "uuid";
    public static final String COLUMN_USER_NAME         = "name";
    public static final String COLUMN_USER_DATE_CREATED = "dateCreated";
    public static final String COLUMN_USER_LAST_ONLINE  = "last_online";
    public static final String COLUMN_KITS              = "kits";
    public static final String COLUMN_COOLDOWN          = "cooldown";

    private DatabaseSettings settings;
    private String           tableUsers;
    private Connection       sqliteConnection;

    public DataHandler(@NotNull DungeonPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void onLoad() {
        this.settings = DatabaseSettings.read(this.plugin);
        this.tableUsers = this.settings.getTablePrefix() + "_users";

        this.createUserTable();

        if (this.settings.isPurgeEnabled() && this.settings.getPurgePeriod() > 0) {
            this.purge();
        }
    }

    @Override
    protected void onShutdown() {
        if (this.sqliteConnection != null) {
            try {
                this.sqliteConnection.close();
            }
            catch (SQLException exception) {
                this.plugin.error("Could not close the database connection.", exception);
            }
            this.sqliteConnection = null;
        }
    }

    // ------------------------------------------------------------------ connection

    private boolean isSQLite() {
        return this.settings.getType() == DatabaseType.SQLITE;
    }

    @NotNull
    private Connection openConnection() throws SQLException {
        String url = this.settings.getJdbcUrl(this.plugin);

        if (this.isSQLite()) {
            if (this.sqliteConnection == null || this.sqliteConnection.isClosed()) {
                this.sqliteConnection = DriverManager.getConnection(url);
            }
            return this.sqliteConnection;
        }

        return DriverManager.getConnection(url, this.settings.getUsername(), this.settings.getPassword());
    }

    /**
     * SQLite reuses a single long-lived connection, so it must not be closed after each statement.
     */
    private void closeIfPooled(@Nullable Connection connection) {
        if (connection == null || this.isSQLite()) return;

        try {
            connection.close();
        }
        catch (SQLException ignored) {
            // Nothing useful to do with a failed close.
        }
    }

    private void execute(@NotNull String sql) {
        Connection connection = null;
        try {
            connection = this.openConnection();
            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
            }
        }
        catch (SQLException exception) {
            this.plugin.error("Database query failed: " + sql, exception);
        }
        finally {
            this.closeIfPooled(connection);
        }
    }

    // ------------------------------------------------------------------ schema

    private void createUserTable() {
        String idType = this.isSQLite() ? "INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT"
            : "int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT";
        String text = this.isSQLite() ? "TEXT NOT NULL" : "MEDIUMTEXT NOT NULL";
        String bigint = "BIGINT NOT NULL";

        this.execute("CREATE TABLE IF NOT EXISTS " + this.tableUsers + "("
            + "`id` " + idType + ", "
            + "`" + COLUMN_USER_ID + "` " + text + ", "
            + "`" + COLUMN_USER_NAME + "` " + text + ", "
            + "`" + COLUMN_USER_DATE_CREATED + "` " + bigint + ", "
            + "`" + COLUMN_USER_LAST_ONLINE + "` " + bigint + ", "
            + "`" + COLUMN_KITS + "` " + text + ", "
            + "`" + COLUMN_COOLDOWN + "` " + text + ");");
    }

    private void purge() {
        long deadline = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(this.settings.getPurgePeriod());

        this.execute("DELETE FROM " + this.tableUsers + " WHERE `" + COLUMN_USER_LAST_ONLINE + "` < " + deadline + ";");
    }

    // ------------------------------------------------------------------ UserDataStore

    @Override
    public int getSyncInterval() {
        return this.isSQLite() ? -1 : this.settings.getSyncInterval();
    }

    @Override
    public void onSynchronize() {
        this.plugin.getUserManager().getLoaded().forEach(user -> {
            if (!user.isAutoSyncReady() || user.isAutoSavePlanned()) return;

            DungeonUser fetch = this.getUser(user.getId());
            if (fetch == null) return;

            fetch.getCooldownMap().clear();
            fetch.getCooldownMap().putAll(user.getCooldownMap());
        });
    }

    @Override
    public boolean isUserExists(@NotNull UUID uuid) {
        return this.exists("`" + COLUMN_USER_ID + "` = ?", uuid.toString());
    }

    @Override
    public boolean isUserExists(@NotNull String name) {
        return this.exists("LOWER(`" + COLUMN_USER_NAME + "`) = ?", name.toLowerCase());
    }

    private boolean exists(@NotNull String where, @NotNull String value) {
        Connection connection = null;
        try {
            connection = this.openConnection();
            try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM " + this.tableUsers + " WHERE " + where + " LIMIT 1;")) {
                statement.setString(1, value);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            }
        }
        catch (SQLException exception) {
            this.plugin.error("Could not check user existence.", exception);
            return false;
        }
        finally {
            this.closeIfPooled(connection);
        }
    }

    @Override
    @Nullable
    public DungeonUser getUser(@NotNull UUID uuid) {
        return this.selectFirst("`" + COLUMN_USER_ID + "` = ?", uuid.toString());
    }

    @Override
    @Nullable
    public DungeonUser getUser(@NotNull String name) {
        return this.selectFirst("LOWER(`" + COLUMN_USER_NAME + "`) = ?", name.toLowerCase());
    }

    @Nullable
    private DungeonUser selectFirst(@NotNull String where, @NotNull String value) {
        Connection connection = null;
        try {
            connection = this.openConnection();
            try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM " + this.tableUsers + " WHERE " + where + " LIMIT 1;")) {
                statement.setString(1, value);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? DataQueries.readUser(resultSet) : null;
                }
            }
        }
        catch (SQLException exception) {
            this.plugin.error("Could not load user data.", exception);
            return null;
        }
        finally {
            this.closeIfPooled(connection);
        }
    }

    @Override
    @NotNull
    public List<DungeonUser> getUsers() {
        List<DungeonUser> users = new ArrayList<>();

        Connection connection = null;
        try {
            connection = this.openConnection();
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT * FROM " + this.tableUsers + ";")) {
                while (resultSet.next()) {
                    DungeonUser user = DataQueries.readUser(resultSet);
                    if (user != null) users.add(user);
                }
            }
        }
        catch (SQLException exception) {
            this.plugin.error("Could not load users.", exception);
        }
        finally {
            this.closeIfPooled(connection);
        }

        return users;
    }

    @Override
    public void insertUser(@NotNull DungeonUser user) {
        Connection connection = null;
        try {
            connection = this.openConnection();
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO " + this.tableUsers
                + " (`" + COLUMN_USER_ID + "`, `" + COLUMN_USER_NAME + "`, `" + COLUMN_USER_DATE_CREATED
                + "`, `" + COLUMN_USER_LAST_ONLINE + "`, `" + COLUMN_KITS + "`, `" + COLUMN_COOLDOWN
                + "`) VALUES (?, ?, ?, ?, ?, ?);")) {

                statement.setString(1, user.getId().toString());
                statement.setString(2, user.getName());
                statement.setLong(3, user.getDateCreated());
                statement.setLong(4, user.getLastOnline());
                statement.setString(5, GSON.toJson(user.getPurchasedKits()));
                statement.setString(6, GSON.toJson(user.getCooldownMap()));
                statement.executeUpdate();
            }
        }
        catch (SQLException exception) {
            this.plugin.error("Could not insert user data.", exception);
        }
        finally {
            this.closeIfPooled(connection);
        }
    }

    @Override
    public void saveUsersCommons(@NotNull Collection<DungeonUser> users) {
        this.update(users, false);
    }

    @Override
    public void saveUsersFully(@NotNull Collection<DungeonUser> users) {
        this.update(users, true);
    }

    private void update(@NotNull Collection<DungeonUser> users, boolean full) {
        if (users.isEmpty()) return;

        String sql = "UPDATE " + this.tableUsers + " SET `" + COLUMN_USER_NAME + "` = ?, `"
            + COLUMN_USER_LAST_ONLINE + "` = ?"
            + (full ? ", `" + COLUMN_KITS + "` = ?, `" + COLUMN_COOLDOWN + "` = ?" : "")
            + " WHERE `" + COLUMN_USER_ID + "` = ?;";

        Connection connection = null;
        try {
            connection = this.openConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (DungeonUser user : users) {
                    int index = 1;
                    statement.setString(index++, user.getName());
                    statement.setLong(index++, user.getLastOnline());
                    if (full) {
                        statement.setString(index++, GSON.toJson(user.getPurchasedKits()));
                        statement.setString(index++, GSON.toJson(user.getCooldownMap()));
                    }
                    statement.setString(index, user.getId().toString());
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        }
        catch (SQLException exception) {
            this.plugin.error("Could not save user data.", exception);
        }
        finally {
            this.closeIfPooled(connection);
        }
    }

    /**
     * Exposed for tooling/debug; the plugin itself never mutates arbitrary rows.
     */
    @NotNull
    public Map<String, String> describeSchema() {
        return Map.of("table", this.tableUsers, "type", this.settings.getType().name());
    }
}

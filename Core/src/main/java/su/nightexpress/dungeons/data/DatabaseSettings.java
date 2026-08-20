package su.nightexpress.dungeons.data;

import java.util.UUID;

import org.jspecify.annotations.NonNull;

import su.nightexpress.dungeons.nightcore.NightPlugin;
import su.nightexpress.dungeons.nightcore.config.ConfigValue;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.config.Writeable;
import su.nightexpress.dungeons.nightcore.util.StringUtil;

/**
 * Database settings, read from {@code engine.yml}.
 * <p>
 * Replaces nightcore's {@code db.config.DatabaseConfig}. Every config path, default and comment is
 * preserved so existing {@code engine.yml} files keep working untouched, including the
 * {@code config.yml -> engine.yml} migration nightcore used to perform.
 */
public class DatabaseSettings implements Writeable {

    public static final String PATH             = "Database";
    public static final String DEFAULT_FILENAME = "data.db";

    private final DatabaseType type;
    private final String       tablePrefix;
    private final int          syncInterval;
    private final boolean      purgeEnabled;
    private final int          purgePeriod;

    private final String username;
    private final String password;
    private final String host;
    private final String database;
    private final String urlOptions;

    private final String filename;
    private final String serverId;

    public enum DatabaseType {
        SQLITE, MYSQL
    }

    public DatabaseSettings(@NonNull DatabaseType type,
                            @NonNull String tablePrefix,
                            int syncInterval,
                            boolean purgeEnabled,
                            int purgePeriod,
                            @NonNull String username,
                            @NonNull String password,
                            @NonNull String host,
                            @NonNull String database,
                            @NonNull String urlOptions,
                            @NonNull String filename,
                            @NonNull String serverId) {
        this.type = type;
        this.tablePrefix = tablePrefix;
        this.syncInterval = syncInterval;
        this.purgeEnabled = purgeEnabled;
        this.purgePeriod = purgePeriod;
        this.username = username;
        this.password = password;
        this.host = host;
        this.database = database;
        this.urlOptions = urlOptions;
        this.filename = filename;
        this.serverId = serverId;
    }

    @NonNull
    public static DatabaseSettings read(@NonNull NightPlugin plugin) {
        String defaultPrefix = StringUtil.lowerCaseUnderscore(plugin.getName());

        FileConfig config = plugin.getConfig();
        FileConfig engineConf = plugin.getEngineConfig();

        // ---------- MIGRATION - START ----------
        if (config.contains(PATH)) {
            DatabaseSettings old = read(config, defaultPrefix);
            old.write(engineConf, "");
            if (!config.contains("Database.UserData")) {
                config.remove(PATH);
            }
        }
        // ---------- MIGRATION - END ----------

        return read(engineConf, defaultPrefix);
    }

    @NonNull
    public static DatabaseSettings read(@NonNull FileConfig config, @NonNull String defaultPrefix) {
        DatabaseType type = ConfigValue.create(PATH + ".Type", DatabaseType.class, DatabaseType.SQLITE,
            "Sets database type.",
            "Available values: SQLITE, MYSQL")
            .read(config);

        int syncInterval = ConfigValue.create(PATH + ".Sync_Interval", -1,
            "Sets how often (in seconds) plugin data will be fetched and loaded from the remote database.",
            "Useless for SQLITE.",
            "Set to '-1' to disable.")
            .read(config);

        // Kept for config compatibility: the vendored data store does not pool connections.
        ConfigValue.create(PATH + ".Max_Lifetime", 1800000L,
            "[ UNUSED since the HikariCP-based connection pool was removed. ]")
            .read(config);

        String tablePrefix = ConfigValue.create(PATH + ".Table_Prefix", defaultPrefix,
            "Custom prefix for plugin tables in database.")
            .read(config);

        String serverId = ConfigValue.create(PATH + ".MySQL.ServerId", UUID.randomUUID().toString(),
            "Custom identifier of this server instance used in data syncing.")
            .read(config);

        String username = ConfigValue.create(PATH + ".MySQL.Username", "root",
            "Database user name.")
            .read(config);

        String password = ConfigValue.create(PATH + ".MySQL.Password", "",
            "Database password.")
            .read(config);

        String host = ConfigValue.create(PATH + ".MySQL.Host", "localhost:3306",
            "Database host. Example: localhost:3306, 127.0.0.1:3306")
            .read(config);

        String database = ConfigValue.create(PATH + ".MySQL.Database", "minecraft",
            "Name of the MySQL database where plugin will create tables.")
            .read(config);

        String urlOptions = ConfigValue.create(PATH + ".MySQL.Options",
            "?allowPublicKeyRetrieval=true&useSSL=false",
            "Connection options. Do not touch unless you know what you're doing.")
            .read(config);

        String filename = ConfigValue.create(PATH + ".SQLite.FileName", DEFAULT_FILENAME,
            "File name for the SQLite database file.",
            "Actually it's a path to the file, so you can use directories here.")
            .read(config);

        boolean purgeEnabled = ConfigValue.create(PATH + ".Purge.Enabled", false,
            "Enables the purge feature.",
            "Purge will remove all records from the plugin tables that are 'old' enough.")
            .read(config);

        int purgePeriod = ConfigValue.create(PATH + ".Purge.For_Period", 60,
            "Sets maximal 'age' (in days of inactivity) for users before they will be purged.")
            .read(config);

        return new DatabaseSettings(type, tablePrefix, syncInterval, purgeEnabled, purgePeriod,
            username, password, host, database, urlOptions, filename, serverId);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        String root = path.isEmpty() ? PATH : path + "." + PATH;

        config.set(root + ".Type", this.type.name());
        config.set(root + ".Sync_Interval", this.syncInterval);
        config.set(root + ".Table_Prefix", this.tablePrefix);
        config.set(root + ".MySQL.ServerId", this.serverId);
        config.set(root + ".MySQL.Username", this.username);
        config.set(root + ".MySQL.Password", this.password);
        config.set(root + ".MySQL.Host", this.host);
        config.set(root + ".MySQL.Database", this.database);
        config.set(root + ".MySQL.Options", this.urlOptions);
        config.set(root + ".SQLite.FileName", this.filename);
        config.set(root + ".Purge.Enabled", this.purgeEnabled);
        config.set(root + ".Purge.For_Period", this.purgePeriod);
    }

    @NonNull
    public DatabaseType getType() {
        return this.type;
    }

    @NonNull
    public String getTablePrefix() {
        return this.tablePrefix;
    }

    public int getSyncInterval() {
        return this.syncInterval;
    }

    public boolean isPurgeEnabled() {
        return this.purgeEnabled;
    }

    public int getPurgePeriod() {
        return this.purgePeriod;
    }

    @NonNull
    public String getUsername() {
        return this.username;
    }

    @NonNull
    public String getPassword() {
        return this.password;
    }

    @NonNull
    public String getFilename() {
        return this.filename;
    }

    @NonNull
    public String getJdbcUrl(@NonNull NightPlugin plugin) {
        if (this.type == DatabaseType.SQLITE) {
            return "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/" + this.filename;
        }
        return "jdbc:mysql://" + this.host + "/" + this.database + this.urlOptions;
    }
}

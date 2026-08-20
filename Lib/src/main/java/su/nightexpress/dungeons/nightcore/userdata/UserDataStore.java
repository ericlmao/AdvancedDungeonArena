package su.nightexpress.dungeons.nightcore.userdata;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Persistence backend for {@link AbstractUserManager}.
 * <p>
 * Upstream this role was played by {@code db.AbstractUserDataManager}, sitting on top of ~6,000 lines of
 * query-builder machinery. The manager only ever needs these eight operations, so the whole layer is
 * reduced to an interface that the plugin implements with plain JDBC.
 */
public interface UserDataStore<U extends AbstractUser> {

    boolean isUserExists(@NonNull UUID uuid);

    boolean isUserExists(@NonNull String name);

    @Nullable
    U getUser(@NonNull UUID uuid);

    @Nullable
    U getUser(@NonNull String name);

    @NonNull
    List<U> getUsers();

    void insertUser(@NonNull U user);

    /** Persists the name/last-online columns only. */
    void saveUsersCommons(@NonNull Collection<U> users);

    /** Persists every column. */
    void saveUsersFully(@NonNull Collection<U> users);

    default void saveUserCommons(@NonNull U user) {
        this.saveUsersCommons(List.of(user));
    }

    default void saveUserFully(@NonNull U user) {
        this.saveUsersFully(List.of(user));
    }

    /** Called from {@code AbstractUserManager}'s scheduled sync task. */
    void onSynchronize();

    /** Sync interval in seconds, or a non-positive value to disable. */
    int getSyncInterval();
}

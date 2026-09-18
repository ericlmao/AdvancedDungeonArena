package su.nightexpress.dungeons.dungeon.game;

import org.bukkit.Bukkit;
import org.bukkit.entity.Projectile;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.config.Keys;
import su.nightexpress.dungeons.nightcore.util.PDCUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Projectiles owned by one dungeon run, regardless of where they travel or whether their shooter dies. */
public class DungeonProjectiles {

    private final DungeonPlugin plugin;
    private final String dungeonId;
    private final Map<UUID, Projectile> projectiles = new HashMap<>();
    private UUID roundId = UUID.randomUUID();
    private boolean active;

    public DungeonProjectiles(@NonNull DungeonPlugin plugin, @NonNull String dungeonId) {
        this.plugin = plugin;
        this.dungeonId = dungeonId;
    }

    public void startRound() {
        this.clear();
        synchronized (this) {
            this.active = true;
        }
    }

    @NonNull
    public synchronized UUID getRoundId() {
        return this.roundId;
    }

    // Called on the projectile's owning region. The mob's captured run prevents a late launch from
    // being adopted by the next run while the old mob is waiting for its scheduled removal.
    public synchronized boolean track(@NonNull Projectile projectile, @NonNull UUID mobRoundId) {
        projectile.setPersistent(false);
        if (!this.active || !this.roundId.equals(mobRoundId)) {
            projectile.remove();
            return false;
        }
        PDCUtil.set(projectile, Keys.projectileDungeonId, this.dungeonId);
        PDCUtil.set(projectile, Keys.projectileRoundId, this.roundId.toString());
        this.projectiles.put(projectile.getUniqueId(), projectile);
        return true;
    }

    public synchronized boolean isCurrent(@NonNull Projectile projectile) {
        return this.active && PDCUtil.getString(projectile, Keys.projectileRoundId)
            .filter(this.roundId.toString()::equals).isPresent();
    }

    public synchronized void forget(@NonNull Projectile projectile) {
        this.projectiles.remove(projectile.getUniqueId(), projectile);
    }

    public void clear() {
        List<Projectile> expired;
        synchronized (this) {
            this.active = false;
            this.roundId = UUID.randomUUID();
            expired = new ArrayList<>(this.projectiles.values());
            this.projectiles.clear();
        }
        // Never hold the registry lock while scheduling work or firing entity removal callbacks.
        expired.forEach(projectile -> {
            if (Bukkit.isOwnedByCurrentRegion(projectile)) {
                // Also works during Paper plugin disable, when new scheduled tasks cannot run.
                projectile.remove();
            }
            else {
                this.plugin.runTask(projectile, projectile::remove);
            }
        });
    }
}

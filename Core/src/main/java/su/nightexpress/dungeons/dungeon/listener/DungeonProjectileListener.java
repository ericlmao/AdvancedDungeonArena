package su.nightexpress.dungeons.dungeon.listener;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.config.Keys;
import su.nightexpress.dungeons.dungeon.DungeonManager;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.mob.DungeonMob;
import su.nightexpress.dungeons.nightcore.manager.AbstractListener;
import su.nightexpress.dungeons.nightcore.util.PDCUtil;

import java.util.Optional;

public class DungeonProjectileListener extends AbstractListener<DungeonPlugin> {

    private final DungeonManager manager;

    public DungeonProjectileListener(@NonNull DungeonPlugin plugin, @NonNull DungeonManager manager) {
        super(plugin);
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLaunch(@NonNull ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        if (!(projectile.getShooter() instanceof LivingEntity shooter) || shooter instanceof Player) return;

        DungeonMob mob = this.manager.getDungeonMob(shooter);
        if (mob == null) {
            // Reset removes the mob registry entry before its entity-scheduled removal executes.
            // Its exact ownership tags still identify shots fired during that window.
            if (PDCUtil.getString(shooter, Keys.mobDungeonId).isPresent()
                && PDCUtil.getString(shooter, Keys.mobRoundId).isPresent()) {
                event.setCancelled(true);
                projectile.setPersistent(false);
                projectile.remove();
            }
            return;
        }

        if (!mob.getDungeon().getProjectiles().track(projectile, mob.getRoundId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHit(@NonNull ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (!this.isExpired(projectile)) return;

        // A different region may still be waiting to execute the reset's removal task.
        event.setCancelled(true);
        projectile.remove();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRemove(@NonNull EntityRemoveFromWorldEvent event) {
        if (!(event.getEntity() instanceof Projectile projectile)) return;
        this.getOwner(projectile).ifPresent(dungeon -> dungeon.getProjectiles().forget(projectile));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkUnload(@NonNull ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity instanceof Projectile projectile && this.isOwned(projectile)) projectile.remove();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntitiesLoad(@NonNull EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities()) {
            if (entity instanceof Projectile projectile && this.isExpired(projectile)) projectile.remove();
        }
    }

    private boolean isOwned(@NonNull Projectile projectile) {
        return PDCUtil.getString(projectile, Keys.projectileDungeonId).isPresent();
    }

    private boolean isExpired(@NonNull Projectile projectile) {
        if (!this.isOwned(projectile)) return false;
        return this.getOwner(projectile)
            .map(dungeon -> !dungeon.getProjectiles().isCurrent(projectile)).orElse(true);
    }

    @NonNull
    private Optional<DungeonInstance> getOwner(@NonNull Projectile projectile) {
        return PDCUtil.getString(projectile, Keys.projectileDungeonId)
            .map(this.manager::getInstanceById);
    }
}

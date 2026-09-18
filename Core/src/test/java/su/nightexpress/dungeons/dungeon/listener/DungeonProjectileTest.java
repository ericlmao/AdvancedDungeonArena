package su.nightexpress.dungeons.dungeon.listener;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.config.Keys;
import su.nightexpress.dungeons.dungeon.DungeonManager;
import su.nightexpress.dungeons.dungeon.config.DungeonConfig;
import su.nightexpress.dungeons.dungeon.module.GameSettings;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.game.DungeonProjectiles;
import su.nightexpress.dungeons.dungeon.mob.DungeonMob;
import su.nightexpress.dungeons.util.DungeonUtils;
import su.nightexpress.dungeons.nightcore.util.bukkit.NightItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DungeonProjectileTest {

    private DungeonPlugin plugin;
    private DungeonManager manager;
    private DungeonProjectileListener listener;
    private DungeonProjectiles projectiles;
    private DungeonInstance dungeon;
    private Shulker shooter;
    private Map<NamespacedKey, Object> shooterData;

    @BeforeEach
    void setUp() {
        Keys.mobDungeonId = new NamespacedKey("dungeonarena", "mob_dungeon_id");
        Keys.mobRoundId = new NamespacedKey("dungeonarena", "mob_round_id");
        Keys.projectileDungeonId = new NamespacedKey("dungeonarena", "projectile_dungeon_id");
        Keys.projectileRoundId = new NamespacedKey("dungeonarena", "projectile_round_id");
        plugin = mock(DungeonPlugin.class);
        manager = mock(DungeonManager.class);
        listener = new DungeonProjectileListener(plugin, manager);
        projectiles = new DungeonProjectiles(plugin, "nightmare");
        projectiles.startRound();
        dungeon = mock(DungeonInstance.class);
        when(dungeon.getProjectiles()).thenReturn(projectiles);
        when(manager.getInstanceById("nightmare")).thenReturn(dungeon);
        shooter = mock(Shulker.class);
        shooterData = attachData(shooter);
        DungeonMob mob = mock(DungeonMob.class);
        when(mob.getDungeon()).thenReturn(dungeon);
        when(mob.getRoundId()).thenReturn(projectiles.getRoundId());
        when(manager.getDungeonMob(shooter)).thenReturn(mob);
    }

    @Test
    void resetRemovesBulletEvenAfterShooterDiesAndChangesRound() {
        ShulkerBullet bullet = projectile(ShulkerBullet.class);
        launch(bullet);
        UUID previous = projectiles.getRoundId();
        when(manager.getDungeonMob(shooter)).thenReturn(null);
        clearOnOwningThread(projectiles);
        verify(bullet).remove();
        verify(bullet).setPersistent(false);
        assertNotEquals(previous, projectiles.getRoundId());
        assertFalse(projectiles.isCurrent(bullet));
    }

    @Test
    void lateShotFromPreviousRoundIsRemovedInsteadOfAdopted() {
        clearOnOwningThread(projectiles);
        ShulkerBullet bullet = projectile(ShulkerBullet.class);
        assertTrue(launch(bullet).isCancelled());
        verify(bullet).remove();
        assertFalse(projectiles.isCurrent(bullet));
    }

    @Test
    void newRoundProjectilesSurviveRepeatedOldCleanupAndOtherInstanceReset() {
        ShulkerBullet old = projectile(ShulkerBullet.class);
        launch(old);
        clearOnOwningThread(projectiles);
        projectiles.startRound();
        Arrow next = projectile(Arrow.class);
        projectiles.track(next, projectiles.getRoundId());
        DungeonProjectiles other = new DungeonProjectiles(plugin, "other");
        other.startRound();
        Arrow unrelated = projectile(Arrow.class);
        other.track(unrelated, other.getRoundId());
        clearOnOwningThread(other);
        verify(next, never()).remove();
        assertTrue(projectiles.isCurrent(next));
        verify(unrelated).remove();
        verify(old).remove();
    }

    @Test
    void playerAndUnregisteredMobShotsRemainUntouched() {
        Arrow playerArrow = projectile(Arrow.class);
        when(playerArrow.getShooter()).thenReturn(mock(Player.class));
        listener.onLaunch(new ProjectileLaunchEvent(playerArrow));
        ShulkerBullet wildBullet = projectile(ShulkerBullet.class);
        when(manager.getDungeonMob(shooter)).thenReturn(null);
        launch(wildBullet);
        clearOnOwningThread(projectiles);
        verify(playerArrow, never()).setPersistent(false);
        verify(playerArrow, never()).remove();
        verify(wildBullet, never()).setPersistent(false);
        verify(wildBullet, never()).remove();
    }

    @Test
    void removedProjectileIsForgottenWithoutForgettingOtherShots() {
        ShulkerBullet bullet = projectile(ShulkerBullet.class);
        Arrow arrow = projectile(Arrow.class);
        launch(bullet);
        launch(arrow);
        EntityRemoveFromWorldEvent event = mock(EntityRemoveFromWorldEvent.class);
        when(event.getEntity()).thenReturn(bullet);
        listener.onRemove(event);
        clearOnOwningThread(projectiles);
        verify(bullet, never()).remove();
        verify(arrow).remove();
    }

    @Test
    void chunkUnloadRemovesOnlyTaggedProjectiles() {
        ShulkerBullet bullet = projectile(ShulkerBullet.class);
        Arrow outside = projectile(Arrow.class);
        Player player = mock(Player.class);
        launch(bullet);
        Chunk chunk = mock(Chunk.class);
        when(chunk.getEntities()).thenReturn(new Entity[]{bullet, outside, player});
        ChunkUnloadEvent event = mock(ChunkUnloadEvent.class);
        when(event.getChunk()).thenReturn(chunk);
        listener.onChunkUnload(event);
        verify(bullet).remove();
        verify(outside, never()).remove();
        verify(player, never()).remove();
    }

    @Test
    void staleHitIsCancelledBeforeScheduledRemovalRuns() {
        ShulkerBullet bullet = projectile(ShulkerBullet.class);
        launch(bullet);
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            projectiles.clear(); // Not the owning region: removal is queued.
        }
        verify(plugin).runTask(any(Entity.class), any(Runnable.class));
        ProjectileHitEvent hit = new ProjectileHitEvent(bullet, shooter);
        listener.onHit(hit);
        assertTrue(hit.isCancelled());
        verify(bullet).remove();
    }

    @Test
    void activeAndUnownedHitsAreUnaffected() {
        Arrow arrow = projectile(Arrow.class);
        launch(arrow);
        ProjectileHitEvent hit = new ProjectileHitEvent(arrow, shooter);
        listener.onHit(hit);
        assertFalse(hit.isCancelled());
        ProjectileHitEvent outside = new ProjectileHitEvent(projectile(Arrow.class), shooter);
        listener.onHit(outside);
        assertFalse(outside.isCancelled());
    }

    @Test
    void delayedRemovalCannotRemoveNewRoundsShot() {
        ShulkerBullet old = projectile(ShulkerBullet.class);
        launch(old);
        AtomicReference<Runnable> removal = new AtomicReference<>();
        doAnswer(call -> { removal.set(call.getArgument(1)); return null; })
            .when(plugin).runTask(any(Entity.class), any(Runnable.class));
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            projectiles.clear();
        }
        projectiles.startRound();
        Arrow next = projectile(Arrow.class);
        projectiles.track(next, projectiles.getRoundId());
        removal.get().run();
        verify(old).remove();
        verify(next, never()).remove();
    }

    @Test
    void loadedExpiredOrRemovedArenaShotsAreRemoved() {
        ShulkerBullet expired = projectile(ShulkerBullet.class);
        launch(expired);
        clearOnOwningThread(projectiles);
        projectiles.startRound();
        Arrow active = projectile(Arrow.class);
        projectiles.track(active, projectiles.getRoundId());
        Arrow outside = projectile(Arrow.class);
        EntitiesLoadEvent event = mock(EntitiesLoadEvent.class);
        when(event.getEntities()).thenReturn(List.of(expired, active, outside));
        listener.onEntitiesLoad(event);
        verify(expired, times(2)).remove();
        verify(active, never()).remove();
        verify(outside, never()).remove();
        when(manager.getInstanceById("nightmare")).thenReturn(null);
        listener.onEntitiesLoad(event);
        verify(active).remove();
        verify(outside, never()).remove();
    }

    @Test
    void cancelledLaunchAndUnloadEventsAreIgnoredByRegistration() throws Exception {
        assertTrue(DungeonProjectileListener.class.getMethod("onLaunch", ProjectileLaunchEvent.class)
            .getAnnotation(EventHandler.class).ignoreCancelled());
        assertTrue(DungeonProjectileListener.class.getMethod("onChunkUnload", ChunkUnloadEvent.class)
            .getAnnotation(EventHandler.class).ignoreCancelled());
    }

    @Test
    void stopAndDeactivateUseRealInstanceCleanup() {
        DungeonConfig config = mock(DungeonConfig.class);
        when(config.getId()).thenReturn("nightmare");
        when(config.getPrefix()).thenReturn("");
        when(config.gameSettings()).thenReturn(mock(GameSettings.class));
        DungeonInstance instance = new DungeonInstance(plugin, config);
        instance.getProjectiles().startRound();
        Arrow arrow = projectile(Arrow.class);
        instance.getProjectiles().track(arrow, instance.getProjectiles().getRoundId());
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.isOwnedByCurrentRegion(any(Entity.class))).thenReturn(true);
            instance.stop();
        }
        verify(arrow).remove();
        instance.getProjectiles().startRound();
        ShulkerBullet bullet = projectile(ShulkerBullet.class);
        instance.getProjectiles().track(bullet, instance.getProjectiles().getRoundId());
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.isOwnedByCurrentRegion(any(Entity.class))).thenReturn(true);
            instance.deactivate();
        }
        verify(bullet).remove();
        assertFalse(instance.isActive());
    }

    @Test
    void failedStartupClearsShotsAndClosesRound() {
        DungeonConfig config = mock(DungeonConfig.class);
        when(config.getId()).thenReturn("nightmare");
        when(config.getPrefix()).thenReturn("");
        when(config.gameSettings()).thenReturn(mock(GameSettings.class));
        DungeonInstance instance = new DungeonInstance(plugin, config);
        ShulkerBullet bullet = projectile(ShulkerBullet.class);
        when(config.getStartLevel()).thenAnswer(call -> {
            instance.getProjectiles().track(bullet, instance.getProjectiles().getRoundId());
            throw new IllegalStateException("startup failed");
        });
        instance.tickLobby(); // Waiting -> ready.
        try (MockedStatic<DungeonUtils> utils = mockStatic(DungeonUtils.class, CALLS_REAL_METHODS);
             MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            // Config defaults otherwise construct a real item through Paper's server-only registry.
            utils.when(DungeonUtils::getDefaultSelectionItem).thenReturn(mock(NightItem.class));
            bukkit.when(() -> Bukkit.isOwnedByCurrentRegion(any(Entity.class))).thenReturn(true);
            assertThrows(IllegalStateException.class, instance::tickLobby);
        }
        verify(bullet).remove();
        assertFalse(instance.getProjectiles().isCurrent(bullet));
        Arrow late = projectile(Arrow.class);
        instance.getProjectiles().track(late, instance.getProjectiles().getRoundId());
        verify(late).remove();
    }

    @Test
    void shotFromDeregisteredMobAwaitingRemovalIsRemoved() {
        when(manager.getDungeonMob(shooter)).thenReturn(null);
        shooterData.put(Keys.mobDungeonId, "nightmare");
        shooterData.put(Keys.mobRoundId, projectiles.getRoundId().toString());
        ShulkerBullet bullet = projectile(ShulkerBullet.class);
        launch(bullet);
        verify(bullet).remove();
        verify(bullet).setPersistent(false);
    }

    private ProjectileLaunchEvent launch(Projectile projectile) {
        when(projectile.getShooter()).thenReturn(shooter);
        ProjectileLaunchEvent event = new ProjectileLaunchEvent(projectile);
        listener.onLaunch(event);
        return event;
    }

    private void clearOnOwningThread(DungeonProjectiles tracker) {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.isOwnedByCurrentRegion(any(Entity.class))).thenReturn(true);
            tracker.clear();
        }
    }

    private <T extends Projectile> T projectile(Class<T> type) {
        T projectile = mock(type);
        when(projectile.getUniqueId()).thenReturn(UUID.randomUUID());
        attachData(projectile);
        return projectile;
    }

    private Map<NamespacedKey, Object> attachData(Entity entity) {
        PersistentDataContainer data = mock(PersistentDataContainer.class);
        Map<NamespacedKey, Object> values = new HashMap<>();
        when(entity.getPersistentDataContainer()).thenReturn(data);
        doAnswer(call -> { values.put(call.getArgument(0), call.getArgument(2)); return null; })
            .when(data).set(any(), any(), any());
        when(data.has(any(), any())).thenAnswer(call -> values.containsKey(call.getArgument(0)));
        when(data.get(any(), any())).thenAnswer(call -> values.get(call.getArgument(0)));
        return values;
    }
}

package su.nightexpress.dungeons.dungeon.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import su.nightexpress.dungeons.api.type.GameState;
import su.nightexpress.dungeons.config.Perms;
import su.nightexpress.dungeons.dungeon.DungeonManager;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.player.DungeonGamer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DungeonTeleportTest {

    private DungeonManager manager;
    private DungeonGameListener listener;
    private Player player;
    private DungeonInstance dungeon;
    private Location from;
    private Location to;

    @BeforeAll
    static void initializePermissions() {
        Server server = mock(Server.class);
        when(server.getPluginManager()).thenReturn(mock(PluginManager.class));
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getServer).thenReturn(server);
            // The real permission tree needs a server while its static fields are initialized.
            assertEquals("dungeonarena.creator", Perms.CREATOR.getName());
        }
    }

    @BeforeEach
    void setUp() {
        manager = mock(DungeonManager.class);
        listener = new DungeonGameListener(null, manager);
        player = mock(Player.class);
        dungeon = mock(DungeonInstance.class);
        World world = mock(World.class);
        from = new Location(world, 0, 64, 0);
        to = new Location(world, 100, 64, 100);
        when(manager.getInstanceByLocation(to)).thenReturn(dungeon);
    }

    @ParameterizedTest
    @EnumSource(GameState.class)
    void pendingExternalTeleportCannotEnterAnyState(GameState state) {
        when(dungeon.getState()).thenReturn(state);
        for (PlayerTeleportEvent.TeleportCause cause : PlayerTeleportEvent.TeleportCause.values()) {
            PlayerTeleportEvent event = new PlayerTeleportEvent(player, from, to, cause);
            listener.onDungeonPlayerTeleport(event);
            assertTrue(event.isCancelled(), state + ": " + cause);
        }
        // Rejection only cancels the event, without reading or changing the outsider's inventory.
        verify(player, never()).getInventory();
    }

    @Test
    void outsiderCannotTeleportWithinSameArena() {
        when(manager.getInstanceByLocation(from)).thenReturn(dungeon);
        assertTrue(teleport().isCancelled());
    }

    @ParameterizedTest
    @EnumSource(GameState.class)
    void registeredJoinAndInternalTeleportRemainAllowed(GameState state) {
        when(dungeon.getState()).thenReturn(state);
        DungeonGamer gamer = register(dungeon);
        when(gamer.isTeleporting()).thenReturn(true);
        assertFalse(teleport().isCancelled());
        when(manager.getInstanceByLocation(from)).thenReturn(dungeon);
        when(gamer.isTeleporting()).thenReturn(false);
        assertFalse(teleport().isCancelled());
    }

    @Test
    void membershipInOtherArenaDoesNotAuthorizeEntryEvenDuringExit() {
        DungeonInstance other = mock(DungeonInstance.class);
        DungeonGamer gamer = register(other);
        when(other.isAboutToEnd()).thenReturn(true);
        when(gamer.isTeleporting()).thenReturn(true);
        assertTrue(teleport().isCancelled());
    }

    @Test
    void creatorCanEnterWithoutRegistration() {
        when(player.hasPermission(Perms.CREATOR)).thenReturn(true);
        assertFalse(teleport().isCancelled());
    }

    @Test
    void controlledLeaveAndEndOfGameExitRemainAllowed() {
        DungeonGamer gamer = register(dungeon);
        when(manager.getInstanceByLocation(from)).thenReturn(dungeon);
        when(manager.getInstanceByLocation(to)).thenReturn(null);
        assertTrue(teleport().isCancelled());
        when(gamer.isTeleporting()).thenReturn(true);
        assertFalse(teleport().isCancelled());
        when(gamer.isTeleporting()).thenReturn(false);
        when(dungeon.isAboutToEnd()).thenReturn(true);
        assertFalse(teleport().isCancelled());
    }

    @Test
    void outsiderCanLeaveArenaAndUseNormalWorldTeleports() {
        when(manager.getInstanceByLocation(to)).thenReturn(null);
        when(manager.getInstanceByLocation(from)).thenReturn(dungeon);
        assertFalse(teleport().isCancelled());
        when(manager.getInstanceByLocation(from)).thenReturn(null);
        assertFalse(teleport().isCancelled());
    }

    @Test
    void redirectedDestinationIsCheckedAtHighestPriority() throws NoSuchMethodException {
        Location outside = new Location(to.getWorld(), 200, 64, 200);
        PlayerTeleportEvent event = new PlayerTeleportEvent(player, from, outside);
        event.setTo(to); // A lower-priority handler redirected the pending teleport into the lobby.
        listener.onDungeonPlayerTeleport(event);
        assertTrue(event.isCancelled());
        EventHandler handler = DungeonGameListener.class
            .getMethod("onDungeonPlayerTeleport", PlayerTeleportEvent.class).getAnnotation(EventHandler.class);
        assertEquals(EventPriority.HIGHEST, handler.priority());
        assertTrue(handler.ignoreCancelled());
    }

    private DungeonGamer register(DungeonInstance instance) {
        DungeonGamer gamer = mock(DungeonGamer.class);
        when(manager.getDungeonPlayer(player)).thenReturn(gamer);
        when(gamer.getDungeon()).thenReturn(instance);
        return gamer;
    }

    private PlayerTeleportEvent teleport() {
        PlayerTeleportEvent event = new PlayerTeleportEvent(player, from, to);
        listener.onDungeonPlayerTeleport(event);
        return event;
    }
}

package server.monitor;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.monitor.model.Item;
import server.monitor.model.PlayerManager;
import server.monitor.model.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PlayerManagerTest {

    private final UUID id = UUID.randomUUID();
    private Player player;
    private final PlayerManager playerManager = new PlayerManager();

    @BeforeEach
    void setUp() {
        playerManager.clear();
        player = new Player(id,"Riven",
                new Player.Position(0, 0, 0),
                List.of(new Item(10, "SWORD", 1)));
    }

    @Test
    void addPlayerStoresPlayer() {
        playerManager.addPlayer(id, player);

        assertSame(player, playerManager.getPlayer(id));
    }
    @Test
    void addPlayerBuildsPlayerFromParameters() {
        PlayerInventory inv = mock(PlayerInventory.class);
        when(inv.getContents()).thenReturn(new ItemStack[5]);

        playerManager.addPlayer(id, "Riven", 1, 2, 3, inv);

        Player stored = playerManager.getPlayer(id);
        assertNotNull(stored);

        assertAll(
                () -> assertEquals("Riven", stored.getNickname()),
                () -> assertEquals(new Player.Position(1, 2, 3), stored.getPosition()),
                () -> assertTrue(stored.getInventory().isEmpty())
        );
    }
    @Test
    void getPlayers() {
        playerManager.addPlayer(id, player);

        assertEquals(Map.of(id,player), playerManager.getPlayers());
    }
    @Test
    void removePlayerDeletesPlayer() {
        playerManager.addPlayer(id, player);
        playerManager.removePlayer(id);

        assertNull(playerManager.getPlayer(id));
    }
    @Test
    void playerDeath() {
        playerManager.addPlayer(id, player);
        playerManager.playerDeath(id);
        assertSame(1, playerManager.getPlayer(id).getDeaths());
    }
    @Test
    void updatePlayerPosition() {
        playerManager.addPlayer(id, player);
        playerManager.updatePosition(id,3,3,3);
        assertEquals(new Player.Position(3,3,3), playerManager.getPlayer(id).getPosition());
    }
    @Test
    void unknownPlayerDeath() {
        playerManager.addPlayer(id, player);
        playerManager.playerDeath(UUID.randomUUID());
        assertSame(0, playerManager.getPlayer(id).getDeaths());
    }
    @Test
    void emptyInventoryGivesEmptyList() {
        PlayerInventory inv = mock(PlayerInventory.class);
        when(inv.getContents()).thenReturn(new ItemStack[36]);

        assertTrue(playerManager.inventory(inv).isEmpty());
    }
    @Test
    void nullSlotsAreSkipped() {
        PlayerInventory inv = mock(PlayerInventory.class);
        ItemStack[] items = new ItemStack[5];
        when(inv.getContents()).thenReturn(items);

        assertEquals(0, playerManager.inventory(inv).size());
    }
    @Test
    void updatePlayerInventory() {
        player.setInventory(List.of(new Item(1, "Sword", 1)));
        playerManager.addPlayer(id, player);
        PlayerInventory inv = mock(PlayerInventory.class);
        ItemStack[] items = new ItemStack[5];
        when(inv.getContents()).thenReturn(items);
        playerManager.updateInventory(id,inv);
        assertEquals(0,playerManager.getPlayer(id).getInventory().size());
    }
    @Test
    void updateUnknownPlayerInventory() {
        player.setInventory(List.of(new Item(1, "Sword", 1)));
        PlayerInventory inv = mock(PlayerInventory.class);
        ItemStack[] items = new ItemStack[5];
        when(inv.getContents()).thenReturn(items);
        playerManager.updateInventory(id,inv);
        playerManager.addPlayer(id, player);
        assertEquals(1,playerManager.getPlayer(id).getInventory().size());
    }
    @Test
    void updateUnknownPlayerPosition() {
        playerManager.updatePosition(id,3d,3d,3d);
        playerManager.addPlayer(id, player);
        assertEquals(new Player.Position(0,0,0),playerManager.getPlayer(id).getPosition());
    }
    @Test
    void killerGetsKill() {
        playerManager.addPlayer(id, player);

        org.bukkit.entity.Player killer = mock(org.bukkit.entity.Player.class);
        org.bukkit.entity.Player victim = mock(org.bukkit.entity.Player.class);
        when(killer.getUniqueId()).thenReturn(id);
        when(victim.getKiller()).thenReturn(killer);

        playerManager.playerKill(victim);

        assertEquals(1, playerManager.getPlayer(id).getKills());
    }
    @Test
    void unknownKillerGetsKill() {

        org.bukkit.entity.Player killer = mock(org.bukkit.entity.Player.class);
        org.bukkit.entity.Player victim = mock(org.bukkit.entity.Player.class);
        when(killer.getUniqueId()).thenReturn(id);
        when(victim.getKiller()).thenReturn(killer);

        playerManager.playerKill(victim);
        playerManager.addPlayer(id, player);
        assertEquals(0, playerManager.getPlayer(id).getKills());
    }
    @Test
    void unknownPlayerKillerGetsKill() {
        playerManager.addPlayer(id, player);
        org.bukkit.entity.Player killer = mock(org.bukkit.entity.Player.class);
        org.bukkit.entity.Player victim = mock(org.bukkit.entity.Player.class);
        when(victim.getKiller()).thenReturn(null);

        playerManager.playerKill(victim);
        assertEquals(0, playerManager.getPlayer(id).getKills());
    }
}
package server.monitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.monitor.model.PlayerManager;
import server.monitor.model.Player;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerManagerTest {

    private final UUID id = UUID.randomUUID();
    private Player player;

    @BeforeEach
    void setUp() {
        PlayerManager.clear();
        player = new Player(id,"Riven",
                new Player.Position(0, 0, 0),
                List.of(new Player.PlayerItem(10, "SWORD", 1)));
    }

    @Test
    void addPlayerStoresPlayer() {
        PlayerManager.addPlayer(id, player);

        assertSame(player, PlayerManager.getPlayer(id));
    }

    @Test
    void removePlayerDeletesPlayer() {
        PlayerManager.addPlayer(id, player);
        PlayerManager.removePlayer(id);

        assertNull(PlayerManager.getPlayer(id));
    }
}
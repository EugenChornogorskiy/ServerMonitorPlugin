package server.monitor.model;


import java.time.LocalDateTime;
import java.util.List;

public class PlayerStats {
    private String nickname;
    private LocalDateTime joinTime;
    private PlayerManager.Position position;
    private List<PlayerManager.PlayerItem> inventory;
    private int kills = 0;
    private int death = 0;
    public PlayerStats(String nickname, PlayerManager.Position position, List<PlayerManager.PlayerItem> inventory) {
        this.nickname = nickname;
        this.joinTime = LocalDateTime.now();
        this.position = position;
        this.inventory = inventory;
    }

    public void setInventory(List<PlayerManager.PlayerItem> inventory) {
        this.inventory = inventory;
    }

    public void setPosition(PlayerManager.Position position) {
        this.position = position;
    }
    public void kill() {
        kills++;
    }
    public void death() {
        death++;
    }
}

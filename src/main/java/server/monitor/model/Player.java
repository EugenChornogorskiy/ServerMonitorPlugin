package server.monitor.model;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Player {
    private UUID id;
    private String nickname;
    private LocalDateTime joinTime;
    private Position position;
    private List<PlayerItem> inventory;
    private int kills = 0;
    private int deaths = 0;
    public Player(UUID id,String nickname, Position position, List<PlayerItem> inventory) {
        this.id = id;
        this.nickname = nickname;
        this.joinTime = LocalDateTime.now();
        this.position = position;
        this.inventory = inventory;
    }

    public void setInventory(List<PlayerItem> inventory) {
        this.inventory = inventory;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
    public void kill() {
        this.kills++;
    }
    public void death() {
        this.deaths++;
    }
    public record Position(double x, double y, double z) {}
    public record PlayerItem(int position, String name, int count) {}
}

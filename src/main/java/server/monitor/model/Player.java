package server.monitor.model;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Player {
    private UUID id;
    private String nickname;
    private LocalDateTime joinTime;
    private Position position;
    private List<Item> inventory;
    private int kills = 0;
    private int deaths = 0;
    public Player(UUID id,String nickname, Position position, List<Item> inventory) {
        this.id = id;
        this.nickname = nickname;
        this.joinTime = LocalDateTime.now();
        this.position = position;
        this.inventory = inventory;
    }

    public void setInventory(List<Item> inventory) {
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

    public int getDeaths() {
        return deaths;
    }
    public int getKills() {
        return kills;
    }

    public String getNickname() {
        return nickname;
    }

    public List<Item> getInventory() {
        return inventory;
    }

    public Position getPosition() {
        return position;
    }

    public record Position(double x, double y, double z) {}
}

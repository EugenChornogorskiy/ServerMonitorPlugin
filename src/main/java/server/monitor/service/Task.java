package server.monitor.service;

import java.util.Map;
import java.util.UUID;

public record Task(Type type, UUID id, UUID player_id, Map<String,String> arguments) {
    public enum Type{
        KICK,
        BAN,
        GIVE
    }
}

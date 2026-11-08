package server.monitor.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import server.monitor.model.Player;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Sender {
    private final TaskManager taskManager;
    private final HttpClient client;
    private final String url;
    private final String key;
    private final JavaPlugin plugin;
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (src, type, ctx) -> new JsonPrimitive(src.toString()))
            .create();
    public Sender (String url, String key, TaskManager taskManager, JavaPlugin plugin) {
        this.client =  HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        this.taskManager = taskManager;
        this.url = url;
        this.key = key;
        this.plugin = plugin;
    }
    public void send(String path, Map<UUID, Player> data) {
        String json = GSON.toJson(data);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url + path))
                .timeout(Duration.ofSeconds(8))
                .header("Content-Type", "application/json")
                .header("X-API-Key", key)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        .thenAccept(response -> {
            String body = response.body();
            if (response.statusCode() == 200) {
                Map<UUID,Task> tasks = GSON.fromJson(body, Tasks.class).tasks();
                Bukkit.getScheduler().runTask(plugin, () -> taskManager.handleCommand(this, tasks));
            } else {
                System.out.println("Error " + response.statusCode() + ": " + body);
            }
        });
    }
    public void sendCommandStatus(UUID id, boolean status) {
        String json = GSON.toJson(new TaskStatus(id, status));
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url + "/api/command/status"))
                .timeout(Duration.ofSeconds(8))
                .header("Content-Type", "application/json")
                .header("X-API-Key", key)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        client.sendAsync(request, HttpResponse.BodyHandlers.discarding());
    }
    public record Tasks (Map<UUID,Task> tasks) {

    }
    public record TaskStatus(UUID id, boolean success) {}

}

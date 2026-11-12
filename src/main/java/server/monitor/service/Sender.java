package server.monitor.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import server.monitor.logging.Logger;
import server.monitor.model.Player;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class Sender {
    private final TaskManager taskManager;
    private final HttpClient client;
    private final String url;
    private final String key;
    private final JavaPlugin plugin;
    private final Logger logger;

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (src, type, ctx) -> new JsonPrimitive(src.toString()))
            .create();

    public Sender (String url, String key, TaskManager taskManager, JavaPlugin plugin, Logger logger) {
        this.client =  HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        this.taskManager = taskManager;
        this.url = url;
        this.key = key;
        this.plugin = plugin;
        this.logger = logger;
    }

    public void send(String path, Map<UUID, Player> data) {
        handleError(path, data, (response) ->  {
            Map<UUID,Task> tasks = GSON.fromJson(response.body(), Tasks.class).tasks();
            Bukkit.getScheduler().runTask(plugin, () -> taskManager.handleCommand(this, tasks));
            return null;
        }, null);
    }

    public void sendCommandStatus(UUID id, boolean status) {
         handleError("/api/command/status", new TaskStatus(id,status), response -> null, null);
    }

    public CompletableFuture<Boolean> authorizeAccount(UUID id, String code) {
        return handleError("/api/authorize/player", new AuthorizationMessage(id,code), response -> true, false);
    }

    public CompletableFuture<Boolean> getPurchase(UUID id, String code) {
        return handleError("/api/get/purchase", new GetPurchaseMessage(id,code), response -> true, false);
    }

    private <T> CompletableFuture<T> handleError(String path, Object data, Function<HttpResponse<String>, T> onSuccess, T fallback) {
        String json = GSON.toJson(data);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url + path))
                .timeout(Duration.ofSeconds(8))
                .header("Content-Type", "application/json")
                .header("X-API-Key", key)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .handle((response, error) -> {
                    if (error != null) {
                        logger.logSenderSend("FAILED", error.getMessage());
                        return fallback;
                    }
                    String body = response.body();
                    logger.logSenderSend(String.valueOf(response.statusCode()), response.body());
                    if (response.statusCode() != 200) {
                        logger.logSenderSend("FAILED","HTTP: " + String.valueOf(response.statusCode()));
                        return fallback;
                    } else {
                        return onSuccess.apply(response);
                    }
                });
    }
    public record Tasks (Map<UUID,Task> tasks) { }
    public record TaskStatus(UUID id, boolean success) {}
    public record AuthorizationMessage(UUID id, String code) {}
    public record GetPurchaseMessage(UUID id, String code) {}
}

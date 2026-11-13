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
import java.util.List;
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
        handleError(Method.POST,path, data, (response) ->  {
            Map<UUID,Task> tasks = GSON.fromJson(response.body(), Tasks.class).tasks();
            Bukkit.getScheduler().runTask(plugin, () -> taskManager.handleCommand(this, tasks));
            return null;
        }, null);
    }

    public void sendCommandStatus(UUID id, boolean status) {
         handleError(Method.POST,"/api/task/status", new TaskStatus(id,status), response -> null, null);
    }

    public CompletableFuture<Response<Boolean>> authorizeAccount(UUID id, String code) {
        return handleError(Method.POST,"/api/player/authorize", new AuthorizationMessage(id,code), response -> true, false);
    }

    public CompletableFuture<Response<Purchase>> getPurchase(UUID id, String code) {
        return handleError(Method.GET,"/api/purchase/get?id=" + id + "&code=" + code, null, response -> GSON.fromJson(response.body(), Purchase.class), null);
    }

    public void closePurchase(Long id, ClosePurchase.Status status) {
        handleError(Method.POST,"/api/purchase/close", new ClosePurchase(id,status), response -> null, null);
    }

    private <T> CompletableFuture<Response<T>> handleError(Method method,String path, Object data, Function<HttpResponse<String>, T> onSuccess, T fallback) {
        HttpRequest.BodyPublisher bodyToSend = (data == null)
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(GSON.toJson(data));
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url + path))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .header("X-API-Key", key)
                .method(method.name(), bodyToSend)
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .handle((response, error) -> {
                    if (error != null) {
                        logger.log("ERROR",path, error.getMessage());
                        return new Response<>(Response.Status.UNAVAILABLE,null);
                    }
                    String body = response.body();
                    if (body == null) {
                        logger.log("ERROR",path, "body is null");
                        return new Response<>(Response.Status.SERVER_ERROR,null);
                    }
                    return switch (response.statusCode()){
                        case 200 -> {
                            logger.log("INFO",path,"HTTP: " + response.statusCode());
                            try{
                                yield new Response<>(Response.Status.OK, onSuccess.apply(response));
                            }
                            catch (Exception e) {
                                logger.log("ERROR",path,"Bad response body: " + e);
                                yield new Response<>(Response.Status.SERVER_ERROR, null);
                            }
                        }
                        case 404 -> {
                            logger.log("ERROR",path,"HTTP: " + response.statusCode());
                            yield new Response<>(Response.Status.NOT_FOUND,null);
                        }
                        case 409 ->  {
                            logger.log("ERROR",path,"HTTP: " + response.statusCode());
                            yield new Response<>(Response.Status.CONFLICT,null);
                        }
                        default ->  {
                            logger.log("ERROR",path,"HTTP: " +response.statusCode());
                            yield new Response<>(Response.Status.SERVER_ERROR,null);
                        }
                    };
                });
    }
    public record Tasks (Map<UUID,Task> tasks) { }
    public record TaskStatus(UUID id, boolean success) {}
    public record AuthorizationMessage(UUID id, String code) {}
    public record GetPurchaseMessage(UUID id, String code) {}
    public record PurchaseItem (String material, int amount) { }
    public record Purchase (long id,List<PurchaseItem> items) { }
    public record Response<T> (Status status, T data) {
        public enum Status {
            OK,
            NOT_FOUND,
            CONFLICT,
            UNAVAILABLE,
            SERVER_ERROR
        }
    }
    public record ClosePurchase(Long id,Status status) {
        public enum Status {
            SUCCESS,
            FAILURE
        }
    }
    public enum Method { GET, POST, PUT, DELETE }
}

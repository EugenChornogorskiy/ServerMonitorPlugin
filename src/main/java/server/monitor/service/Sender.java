package server.monitor.service;

import com.google.gson.Gson;
import server.monitor.model.PlayerStats;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

public class Sender {
    private HttpClient client;
    private final String url;
    private final String key;
    private static final Gson GSON = new Gson();
    public Sender (String url, String key) {
        this.client =  HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        this.url = url;
        this.key = key;
    }
    public void send(String path, Map<UUID, PlayerStats> data) {
        String json = GSON.toJson(data);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url + path))
                .timeout(Duration.ofSeconds(8))
                .header("Content-Type", "application/json")
                .header("X-API-Key", key)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        client.sendAsync(request, HttpResponse.BodyHandlers.discarding());
    }

}

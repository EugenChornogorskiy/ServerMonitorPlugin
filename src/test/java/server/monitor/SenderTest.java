package server.monitor;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.monitor.service.Sender;
import server.monitor.service.TaskManager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SenderTest {
    private MockWebServer server;
    private Sender sender;
    private TaskManager taskManager;
    private JavaPlugin plugin;
    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        taskManager = mock(TaskManager.class);
        plugin = mock(JavaPlugin.class);
        String baseUrl = server.url("/").toString().replaceAll("/$", "");
        sender = new Sender(baseUrl, "mykey", taskManager,plugin);
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void sendPostsJsonWithApiKey() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"tasks\":[]}"));

        sender.send("/api/set/players", Map.of());

        RecordedRequest req = server.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(req);
        assertAll(
                () -> assertEquals("POST", req.getMethod()),
                () -> assertEquals("/api/set/players", req.getPath()),
                () -> assertEquals("mykey", req.getHeader("X-API-Key")),
                () -> assertEquals("{}", req.getBody().readUtf8())
        );
    }


    @Test
    void sendDoesNotHandleTasksOnError() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(500).setBody("fail"));

        sender.send("/api/set/players", Map.of());

        server.takeRequest(5, TimeUnit.SECONDS);
        Thread.sleep(300);   // дати колбеку відпрацювати
        verifyNoInteractions(taskManager);
    }

    @Test
    void sendCommandStatusPostsIdAndSuccess() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200));
        UUID id = UUID.randomUUID();

        sender.sendCommandStatus(id, true);

        RecordedRequest req = server.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(req);
        assertEquals("/api/command/status", req.getPath());
        String body = req.getBody().readUtf8();
        assertTrue(body.contains(id.toString()));
    }
}

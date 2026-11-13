package server.monitor;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.monitor.logging.Logger;
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
    private Logger logger;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        taskManager = mock(TaskManager.class);
        plugin = mock(JavaPlugin.class);
        logger = mock(Logger.class);
        String baseUrl = server.url("/").toString().replaceAll("/$", "");
        sender = new Sender(baseUrl, "mykey", taskManager,plugin,logger);
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void sendPostsJsonWithApiKey() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"tasks\":[]}"));

        sender.send("/api/player/set", Map.of());

        RecordedRequest req = server.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(req);
        assertAll(
                () -> assertEquals("POST", req.getMethod()),
                () -> assertEquals("/api/player/set", req.getPath()),
                () -> assertEquals("mykey", req.getHeader("X-API-Key")),
                () -> assertEquals("{}", req.getBody().readUtf8())
        );
    }


    @Test
    void sendDoesNotHandleTasksOnError() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(500).setBody("fail"));

        sender.send("/api/player/set", Map.of());

        server.takeRequest(5, TimeUnit.SECONDS);
        Thread.sleep(300);
        verifyNoInteractions(taskManager);
    }

    @Test
    void sendCommandStatusPostsIdAndSuccess() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200));
        UUID id = UUID.randomUUID();

        sender.sendCommandStatus(id, true);

        RecordedRequest req = server.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(req);
        assertEquals("/api/task/status", req.getPath());
        String body = req.getBody().readUtf8();
        assertTrue(body.contains(id.toString()));
    }
    @Test
    void authorizeAccount() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200));
        UUID id = UUID.randomUUID();

        sender.authorizeAccount(id, "1111");

        RecordedRequest req = server.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(req);
        assertEquals("/api/player/authorize", req.getPath());
        String body = req.getBody().readUtf8();
        assertTrue(body.contains(id.toString()));
    }
    @Test
    void authorizeAccountFailureStatus() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(400));
        UUID id = UUID.randomUUID();

        Sender.Response<Boolean> result = sender.authorizeAccount(id, "1111").get(5, TimeUnit.SECONDS);
        assertEquals(Sender.Response.Status.SERVER_ERROR, result.status());

        RecordedRequest req = server.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(req);
        assertEquals("/api/player/authorize", req.getPath());
        verify(logger).log("ERROR","/api/player/authorize", "HTTP: 400");
    }
    @Test
    void authorizeAccountFailure() throws Exception {
        server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));
        UUID id = UUID.randomUUID();

        Sender.Response<Boolean> result = sender.authorizeAccount(id, "1111").get(5, TimeUnit.SECONDS);
        assertEquals(Sender.Response.Status.UNAVAILABLE, result.status());

        RecordedRequest req = server.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(req);
        verify(logger).log(eq("ERROR"), eq("/api/player/authorize"), anyString());
    }

}

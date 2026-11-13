package server.monitor;

import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import server.monitor.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class LoggerTest {
    private final JavaPlugin plugin = mock(JavaPlugin.class);
    @TempDir
    Path tempDir;

    @Test
    void logWriteInFile() throws IOException {
        Path file = tempDir.resolve("log-file.txt");
        Logger logger = new Logger(file.toString(),plugin);
        logger.log("ERROR", "/api","HTTP 400");

        List<String> lines = Files.readAllLines(file);
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).endsWith(" | ERROR | /api | HTTP 400"), lines.get(0));
    }
}

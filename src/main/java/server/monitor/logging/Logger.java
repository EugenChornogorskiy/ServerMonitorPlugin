package server.monitor.logging;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Level;

public class Logger {
    private final File file;
    private final JavaPlugin plugin;
    public Logger(String path, JavaPlugin plugin) {
        this.file = new File(path);
        this.plugin = plugin;
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
    }

    public void logSenderSend(String status,String content) {
        String line = LocalDateTime.now() + " | " + status + " | " + content
                + System.lineSeparator();
        writeLog(line);
    }
    private synchronized void writeLog( String line) {
        try {
            Files.writeString(file.toPath(), line,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Cannot write log file", e);
        }
    }
}

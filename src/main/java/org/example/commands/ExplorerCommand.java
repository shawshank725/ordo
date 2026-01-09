package org.example.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(
        name = "open",
        mixinStandardHelpOptions = true,
        description = "Open a file or folder using the system default application"
)
public class ExplorerCommand implements Callable<Integer> {

    @Parameters(
            index = "0",
            arity = "0..1",
            description = "File or folder path to open (default: current directory)"
    )
    private Path path = Path.of(".");

    @Override
    public Integer call() {
        Path resolved = path.toAbsolutePath().normalize();

        if (!Files.exists(resolved)) {
            System.err.println("Error: Path does not exist: " + resolved);
            return 1;
        }

        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(resolved.toFile());
                    System.out.println("Opened: " + resolved);
                    return 0;
                }
            }

            // Fallback for systems without full Desktop support (e.g., some Linux)
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;

            if (os.contains("win")) {
                pb = new ProcessBuilder("explorer", resolved.toString());
            } else if (os.contains("mac")) {
                pb = new ProcessBuilder("open", resolved.toString());
            } else {
                // Linux: try xdg-open
                pb = new ProcessBuilder("xdg-open", resolved.toString());
            }

            pb.start();
            System.out.println("Opened: " + resolved);
            return 0;

        } catch (IOException e) {
            System.err.println("Failed to open: " + resolved);
            System.err.println("Reason: " + e.getMessage());
            return 1;
        }
    }
}
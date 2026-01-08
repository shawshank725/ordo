package org.example.commands;

import org.example.enumeration.FileTransfer;
import org.example.enumeration.FileType;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;

import static org.example.commands.FileFetcher.*;

@Command(
        name = "transfer",
        mixinStandardHelpOptions = true,
        description = "Batch transfer files with certain conditions"
)
public class TransferCommand implements Callable<Integer> {

    @Parameters(index = "0", arity = "1..*", description = "Files or glob patterns to rename")
    private List<Path> from;

    @Parameters(index = "1", arity = "1", description = "Destination folder")
    private Path destination;

    @Option(names = {"-m", "--move"}, description = "Move files (cut - delete source)")
    private boolean move;

    @Option(names = {"-c", "--copy"}, description = "Copy files (keep source)")
    private boolean copy = true;

    @Option(names = {"-r", "--recursive"}, description = "Recursive means that any folder and subsequent subfolders will be affected.")
    private boolean recursive;

    @Option(names = {"-dc", "--datecreated"}, description = "Adding date to filter the files and folders.")
    private LocalDate dateCreated;

    @Option(names = {"-ext", "--extension"}, description = "Extension to filter out the files.")
    private String extension;

    @Option(names = {"-lsz", "--lessthansize"}, description = "Filter out files and folders having size less than the one provided.")
    private double lessThanSize;

    @Option(names = {"-gsz", "--greaterthansize"}, description = "Filter out files and folders having size greater than the one provided.")
    private double greaterThanSize;

    @Override
    public Integer call() throws Exception {
        transferFiles();
        return 0;
    }

    private void transferFiles() throws IOException {
        // Get matching source files
        List<Path> files = getFiles(
                from, recursive, dateCreated, extension,
                lessThanSize, greaterThanSize, FileType.FILE
        );

        if (files.isEmpty()) {
            System.out.println("No files matched your filters.");
            return;
        }

        // Validate destination
        if (!Files.exists(destination)) {
            System.out.print("Destination folder does not exist. Create it? (y/N): ");
            String response = new java.util.Scanner(System.in).nextLine().trim();
            if (!response.equalsIgnoreCase("y")) {
                System.out.println("Operation aborted.");
                return;
            }
            Files.createDirectories(destination);
        }

        if (!Files.isDirectory(destination)) {
            System.out.println("Error: Destination is not a directory.");
            return;
        }

        // Determine mode
        boolean isMove = move;
        boolean isCopy = copy || !move;  // default to copy if neither specified

        if (move && copy) {
            System.out.println("Error: Cannot use both --move and --copy.");
            return;
        }

        String action = isMove ? "Moved" : "Copied";

        int success = 0;
        int failed = 0;

        for (Path source : files) {
            Path destFile = destination.resolve(source.getFileName());

            // Handle collision
            int counter = 1;
            while (Files.exists(destFile)) {
                String name = source.getFileName().toString();
                String newName = name + " (" + counter + ")";
                destFile = destination.resolve(newName);
                counter++;
            }

            try {
                if (isMove) {
                    Files.move(source, destFile);
                } else {
                    Files.copy(source, destFile);
                }
                System.out.printf("%s: %s → %s%n", action, source.getFileName(), destFile.getFileName());
                success++;
            } catch (IOException e) {
                System.err.printf("Failed: %s → %s (%s)%n", source.getFileName(), destFile.getFileName(), e.getMessage());
                failed++;
            }
        }

        System.out.printf("%n%s: %d succeeded, %d failed.%n", action, success, failed);
    }

}
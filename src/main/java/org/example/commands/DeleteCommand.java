package org.example.commands;

import org.example.enumeration.FileType;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.example.commands.FileFetcher.getFiles;

@Command(
        name = "delete",
        mixinStandardHelpOptions = true,
        description = """
                Delete files matching filters (safe by default, moves to trash if possible)\
                Usage: ordo delete [-hprVy] [--dry-run] [-fno] [-dc=<dateCreated>]
                                   [-ext=<extension>] [-gsz=<greaterThanSize>]
                                   [-lsz=<lessThanSize>] [<folderPath>...]"""
)
public class DeleteCommand implements Callable<Integer> {

    @Parameters(index = "0", arity = "0..*", description = "Folder path(s) or file globs (default: current directory)")
    private List<Path> folderPath = List.of(Path.of("."));

    @Option(names = {"-r", "--recursive"}, description = "Search for files and folders recursively inside directories")
    private boolean recursive;

    @Option(names = {"-dc", "--datecreated"}, description = "Filter by creation date (YYYY-MM-DD)")
    private LocalDate dateCreated;

    @Option(names = {"-ext", "--extension"}, description = "Filter by file extension")
    private String extension;

    @Option(names = {"-lsz", "--lessthansize"}, description = "Filter files smaller than size (in MB)")
    private double lessThanSize;

    @Option(names = {"-gsz", "--greaterthansize"}, description = "Filter files larger than size (in MB)")
    private double greaterThanSize;

    @Option(names = {"-p", "--permanent"}, description = "Permanently delete (bypass trash/recycle bin)")
    private boolean permanent;

    @Option(names = {"-y", "--yes"}, description = "Skip confirmation prompt")
    private boolean yes;

    @Option(names = {"-fno", "--filenameonly"}, description = "Show only file names in output")
    private boolean filenameOnly;

    @Option(names = {"-def", "--deleteemptyfolder"}, description = "Delete blank folders")
    private boolean deleteEmptyFolder;

    @Override
    public Integer call() throws Exception {
        deleteFilesFolder();
        return 0;
    }

    private void deleteFilesFolder() throws IOException {
        // Get matching files (FileType.FILE → only files, not directories)
        List<Path> files = getFiles(
                folderPath, recursive, dateCreated, extension,
                lessThanSize, greaterThanSize, FileType.FILE
        );

        if (files.isEmpty()) {
            System.out.println("No files matched your filters.");
        } else {
            System.out.printf("Found %d file(s) to delete:%n%n", files.size());

            for (Path file : files) {
                long sizeMB = Files.size(file) / (1024 * 1024);
                String sizeStr = sizeMB > 0 ? sizeMB + " MB" : "< 1 MB";
                String display = filenameOnly ? file.getFileName().toString() : file.toString();
                System.out.printf("%s  (%s)%n", display, sizeStr);
            }
            System.out.println();
        }

        // Confirmation prompt (skip if -y or no files)
        if (!yes && !files.isEmpty()) {
            System.out.printf("Delete %d file(s)%s? Continue? (y/N): ",
                    files.size(),
                    permanent ? " permanently (cannot be undone)" : "");
            String response = new Scanner(System.in).nextLine().trim();
            if (!response.equalsIgnoreCase("y")) {
                System.out.println("Operation aborted.");
                return;
            }
        }

        // === 1. Delete the files ===
        int fileDeleted = 0;
        int fileFailed = 0;

        for (Path file : files) {
            try {
                if (permanent) {
                    Files.delete(file);
                } else {
                    // Safe delete: move to ~/.ordo-trash
                    Path trashDir = Path.of(System.getProperty("user.home"), ".ordo-trash");
                    Files.createDirectories(trashDir);

                    Path dest = trashDir.resolve(file.getFileName());
                    int counter = 1;
                    while (Files.exists(dest)) {
                        String name = file.getFileName().toString();
                        dest = trashDir.resolve(name + "." + counter);
                        counter++;
                    }
                    Files.move(file, dest);
                }
                fileDeleted++;
            } catch (IOException e) {
                System.err.printf("Failed to delete file: %s (%s)%n", file.getFileName(), e.getMessage());
                fileFailed++;
            }
        }

        // === 2. Delete empty folders (only if requested and recursive) ===
        AtomicInteger folderDeleted = new AtomicInteger();
        if (deleteEmptyFolder && recursive) {
            // Collect all parent directories from the original targets
            List<Path> dirsToCheck = new ArrayList<>();
            for (Path target : folderPath) {
                Path resolved = target.toAbsolutePath().normalize();
                if (Files.isDirectory(resolved)) {
                    dirsToCheck.add(resolved);
                }
            }

            // Walk each directory tree bottom-up and delete empty dirs
            for (Path root : dirsToCheck) {
                Files.walk(root)
                        .sorted(java.util.Comparator.reverseOrder()) // bottom-up
                        .filter(Files::isDirectory)
                        .forEach(dir -> {
                            try {
                                if (Files.list(dir).findAny().isEmpty()) {
                                    if (permanent) {
                                        Files.delete(dir);
                                    } else {
                                        // Move empty folder to trash too
                                        Path trashDir = Path.of(System.getProperty("user.home"), ".ordo-trash");
                                        Path dest = trashDir.resolve(dir.getFileName());
                                        Files.move(dir, dest);
                                    }
                                    folderDeleted.getAndIncrement();
                                }
                            } catch (IOException ignored) {
                                // Directory not empty or permission issue — skip
                            }
                        });
            }
        }

        // === Final summary ===
        System.out.println();
        if (fileDeleted > 0 || folderDeleted.get() > 0) {
            System.out.printf("Deleted: %d file(s)", fileDeleted);
            if (folderDeleted.get() > 0) {
                System.out.printf(" and %d empty folder(s)", folderDeleted);
            }
            System.out.println();
            if (fileFailed > 0) {
                System.out.printf("%d file(s) failed to delete.%n", fileFailed);
            }
        } else if (!files.isEmpty()) {
            System.out.println("No files were deleted (all operations failed).");
        } else if (deleteEmptyFolder && folderDeleted.get() > 0) {
            System.out.printf("Deleted %d empty folder(s).%n", folderDeleted);
        } else {
            System.out.println("Nothing to delete.");
        }
    }
}
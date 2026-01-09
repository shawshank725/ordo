package org.example.commands;import org.example.enumeration.FileType;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;

@Command(
        name = "delete",
        mixinStandardHelpOptions = true,
        description = "Delete files matching filters (safe by default, moves to trash if possible)"
)
public class DeleteCommand implements Callable<Integer> {@Parameters(index = "0", arity = "0..*", description = "Folder path(s) or file globs (default: current directory)")
private List<Path> targets = List.of(Path.of("."));

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

    @Option(names = {"-fno", "--filenameonly"}, description = "Show only file names in output")
    private boolean filenameOnly;

    @Option(names = {"-df", "--deletefolders"}, description = "Delete folders as well")
    private boolean deleteFolders;

    @Override
    public Integer call() throws Exception {
        deleteFilesAndFolders();
        return 0;
    }

    private void deleteFilesAndFolders() throws IOException {
        boolean hasFilters = dateCreated != null || extension != null
                || lessThanSize > 0 || greaterThanSize > 0;

        System.out.println("Targets: " + targets);
        System.out.println("Has filters: " + hasFilters);
        System.out.println("Recursive: " + recursive);
        System.out.println("Delete folders: " + deleteFolders);
        System.out.println("Permanent: " + permanent);

        // ── 1. Collect items to delete ──────────────────────────────────────
        List<Path> itemsToDelete;
        if (hasFilters) {
            // Filtered search → only files (folders are never filtered/deleted this way)
            itemsToDelete = FileFetcher.getFiles(targets, recursive, dateCreated,
                    extension, lessThanSize, greaterThanSize, FileType.FILE);
            System.out.println("Found " + itemsToDelete.size() + " matching files");
        } else {
            // No filters → delete everything (files + folders if allowed)
            itemsToDelete = FileFetcher.getAllFiles(targets, recursive, FileType.BOTH);
            System.out.println("Found " + itemsToDelete.size() + " total items (files + folders)");
        }

        if (itemsToDelete.isEmpty()) {
            System.out.println("Nothing to delete.");
            return;
        }

        // ── 2. Safety check: folders without -df ────────────────────────────
        boolean hasFolders = itemsToDelete.stream().anyMatch(Files::isDirectory);
        if (hasFolders && !deleteFolders && !hasFilters) {
            System.err.println("Error: Found folders but -df/--deletefolders not used.");
            System.err.println("Use -df if you really want to delete folders.");
            return;
        }

        // ── 3. Confirmation (always ask — very important!) ──────────────────
        long fileCount = itemsToDelete.stream().filter(Files::isRegularFile).count();
        long folderCount = itemsToDelete.size() - fileCount;

        System.out.printf("About to %s %d file(s) and %d folder(s).%n",
                permanent ? "PERMANENTLY delete" : "move to trash", fileCount, folderCount);

        Scanner sc = new Scanner(System.in);
        System.out.print("Continue? (y/N): ");
        if (!sc.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.println("Aborted.");
            return;
        }

        // ── 4. Actual deletion ──────────────────────────────────────────────
        int filesOk = 0, filesFail = 0;
        int foldersOk = 0, foldersFail = 0;

        for (Path path : itemsToDelete) {
            boolean isDir = Files.isDirectory(path);

            if (isDir && !deleteFolders) continue;  // skip folders unless explicitly allowed

            try {
                if (permanent) {
                    Files.delete(path);  // throws if directory not empty → use deleteRecursively if needed
                } else {
                    moveToTrash(path);
                }

                if (isDir) {
                    foldersOk++;
                    System.out.println((permanent ? "Deleted" : "Trashed") + " folder: " + path);
                } else {
                    filesOk++;
                    System.out.println((permanent ? "Deleted" : "Trashed") + " file:   " + path);
                }

            } catch (Exception e) {
                if (isDir) foldersFail++;
                else filesFail++;
                System.err.println("Failed to " + (permanent ? "delete" : "trash") + ": " + path);
                System.err.println("  → " + e.getMessage());
            }
        }

        // ── 5. Final report ─────────────────────────────────────────────────
        System.out.println("\n───── DELETE SUMMARY ─────");
        System.out.printf("Files   successful: %d   failed: %d%n", filesOk, filesFail);
        System.out.printf("Folders successful: %d   failed: %d%n", foldersOk, foldersFail);
        System.out.println("──────────────────────────");

    }

    private void moveToTrash(Path path) throws IOException {
        Path trashDir = Path.of(System.getProperty("user.home"), ".ordo-trash");
        Files.createDirectories(trashDir);

        Path dest = trashDir.resolve(path.getFileName());
        int counter = 1;
        while (Files.exists(dest)) {
            String name = path.getFileName().toString();
            String base = name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name;
            String ext  = name.contains(".") ? name.substring(name.lastIndexOf('.')) : "";
            dest = trashDir.resolve(base + "." + counter + ext);
            counter++;
        }

        Files.move(path, dest);
    }

}
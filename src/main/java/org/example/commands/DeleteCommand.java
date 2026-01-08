package org.example.commands;

import org.example.enumeration.FileType;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.example.commands.FileFetcher.getAllFiles;
import static org.example.commands.FileFetcher.getFiles;

@Command(
        name = "delete",
        mixinStandardHelpOptions = true,
        description = "Delete files matching filters (safe by default, moves to trash if possible)"
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

    @Option(names = {"-def", "--deletefolders"}, description = "Delete folders as well")
    private boolean deleteFolders;

    @Override
    public Integer call() throws Exception {
        deleteFilesFolder();
        return 0;
    }

    private void deleteFilesFolder() throws IOException {
        boolean hasFilter = dateCreated != null || extension != null || lessThanSize > 0 || greaterThanSize > 0;

        // based on filter get list.
        List<Path> allFiles;
        if (hasFilter) {
            // get the files based on filters applied using the method: getFiles
            allFiles = getFiles(folderPath, recursive, dateCreated, extension,
                    lessThanSize, greaterThanSize, FileType.BOTH);
        } else {
            // no filter means that entire folder is to be deleted. along with any other files provided.
            allFiles = getAllFiles(folderPath, recursive, FileType.BOTH);
        }

        if (allFiles.isEmpty()) {
            System.out.println("No files or folders matched.");
            return;
        }

        // Show what will be deleted
        System.out.printf("Found %d item(s):%n%n", allFiles.size());
        for (Path item : allFiles) {
            String display = filenameOnly ? item.getFileName().toString() : item.toString();
            if (Files.isDirectory(item)) {
                System.out.printf("%s  <DIR>%n", display);
            } else {
                long sizeMB = Files.size(item) / (1024 * 1024);
                String sizeStr = sizeMB > 0 ? sizeMB + " MB" : "< 1 MB";
                System.out.printf("%s  (%s)%n", display, sizeStr);
            }
        }
        System.out.println();

        // Confirmation prompt if not -y
        if (!yes) {
            System.out.printf("Delete %d item(s)%s? Continue? (y/N): ",
                    allFiles.size(),
                    permanent ? " permanently (cannot be undone)" : "");
            String response = new Scanner(System.in).nextLine().trim();
            if (!response.equalsIgnoreCase("y")) {
                System.out.println("Operation aborted.");
                return;
            }
        }

        int deleted = 0;
        int failed = 0;

        // Sort bottom-up for safe recursive delete
        allFiles.sort(java.util.Comparator.comparingInt(p -> -p.getNameCount()));

        Path trashDir = Path.of(System.getProperty("user.home"), ".ordo-trash");
        // now we check if permanent delete is added or not.
        // first condition is permanent is true
        if (permanent) {
            for (Path path : allFiles) {
                try {
                    Files.delete(path);
                    deleted++;
                } catch (IOException e) {
                    System.err.printf("Failed to permanently delete: %s (%s)%n",
                            filenameOnly ? path.getFileName() : path, e.getMessage());
                    failed++;
                }
            }
        }
        // the delete is temporary not permanent. so detect the OS and move to trash.
        else {

            Files.createDirectories(trashDir);

            for (Path path : allFiles) {
                try {
                    Path dest = trashDir.resolve(path.getFileName());
                    int counter = 1;
                    while (Files.exists(dest)) {
                        String name = path.getFileName().toString();
                        dest = trashDir.resolve(name + "." + counter);
                        counter++;
                    }
                    Files.move(path, dest);
                    deleted++;
                } catch (IOException e) {
                    System.err.printf("Failed to move to trash: %s (%s)%n",
                            filenameOnly ? path.getFileName() : path, e.getMessage());
                    failed++;
                }
            }
        }

        // === Delete empty folders if requested (only after files are gone) ===
        if (deleteFolders && recursive) {
            List<Path> roots = new ArrayList<>(folderPath);
            for (Path root : roots) {
                Path resolved = root.toAbsolutePath().normalize();
                if (Files.isDirectory(resolved)) {
                    Files.walk(resolved)
                            .sorted(java.util.Comparator.reverseOrder())
                            .filter(Files::isDirectory)
                            .forEach(dir -> {
                                try {
                                    if (Files.list(dir).findAny().isEmpty()) {
                                        if (permanent) {
                                            Files.delete(dir);
                                        } else {
                                            Path trashDest = trashDir.resolve(dir.getFileName());
                                            Files.move(dir, trashDest);
                                        }
                                        System.out.printf("Deleted empty folder: %s%n", dir.getFileName());
                                    }
                                } catch (IOException ignored) {
                                    // Not empty or permission issue — skip
                                }
                            });
                }
            }
        }

        // Final report
        System.out.println();
        if (deleted > 0) {
            System.out.printf("Successfully deleted %d item(s).%n", deleted);
        }
        if (failed > 0) {
            System.out.printf("%d item(s) failed.%n", failed);
        }
        if (deleted == 0 && failed == 0) {
            System.out.println("Nothing was deleted.");
        }
    }
}
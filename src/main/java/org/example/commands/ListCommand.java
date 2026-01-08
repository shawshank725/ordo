package org.example.commands;

import org.example.enumeration.FileType;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.example.commands.FileFetcher.getAllFiles;
import static org.example.commands.FileFetcher.getFiles;

@Command(
        name = "list",
        mixinStandardHelpOptions = true,
        description = "List files meeting certain conditions"
)
public class ListCommand implements Callable<Integer> {

    @Parameters(index = "0", arity = "0..*", description = "Folder path")
    private List<Path> folderPath = List.of(Path.of(".")); ;

    @Option(names = {"-r", "--recursive"}, description = "Recursive means that any folder and subsequent subfolders will be affected.")
    private boolean recursive;

    @Option(names = {"-fno", "--filenameonly"}, description = "Adding this prints only the file names (skips the folder name).")
    private boolean filenameOnly;

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
        List<Path> allFiles = new ArrayList<>();

        // Determine if any filter is active (excluding recursive and filenameOnly)
        boolean hasFilter = (dateCreated != null) ||
                (extension != null && !extension.isEmpty()) ||
                (lessThanSize > 0) ||
                (greaterThanSize > 0);

        if (hasFilter) {
            // Any real filter → use filtered getFiles()
            allFiles = getFiles(
                    folderPath, recursive, dateCreated, extension,
                    lessThanSize, greaterThanSize, FileType.FILE
            );
        } else if (recursive) {
            // Only --recursive (no other filters) → get everything recursively
            allFiles = getAllFiles(folderPath, true, FileType.FILE);
        } else {
            // No filters and no recursive → list only direct children (files + folders)
            for (Path target : folderPath) {
                Path resolved = target.toAbsolutePath().normalize();

                if (!Files.exists(resolved)) {
                    System.err.println("Warning: Path does not exist: " + resolved);
                    continue;
                }

                if (Files.isRegularFile(resolved)) {
                    allFiles.add(resolved);
                } else if (Files.isDirectory(resolved)) {
                    try (var stream = Files.newDirectoryStream(resolved)) {
                        for (Path entry : stream) {
                            if (Files.isRegularFile(entry) || Files.isDirectory(entry)) {
                                allFiles.add(entry);
                            }
                        }
                    }
                }
            }
        }

        if (allFiles.isEmpty()) {
            System.out.println("No items found.");
            return 0;
        }

        System.out.println("Found " + allFiles.size() + " item(s):\n");

        for (Path item : allFiles) {
            String display = filenameOnly ? item.getFileName().toString() : item.toString();

            if (Files.isRegularFile(item)) {
                long sizeMB = Files.size(item) / (1024 * 1024);
                String sizeStr = sizeMB > 0 ? sizeMB + " MB" : "< 1 MB";
                System.out.printf("%s  (%s)%n", display, sizeStr);
            } else {
                System.out.printf("%s  <DIR>%n", display);
            }
        }

        return 0;
    }
}

package org.example.commands;import org.example.enumeration.FileType;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;import static org.example.commands.FileFetcher.getAllFiles;
import static org.example.commands.FileFetcher.getFiles;@Command(
        name = "delete",
        mixinStandardHelpOptions = true,
        description = "Delete files matching filters (safe by default, moves to trash if possible)"
)
public class DeleteCommand implements Callable<Integer> {@Parameters(index = "0", arity = "0..*", description = "Folder path(s) or file globs (default: current directory)")
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

        int filesDeleted, foldersDeleted, filesFailedToDelete, foldersFailedToDelete = 0;

        // based on filter get list.
        List<Path> allFiles;
        if (hasFilter){
            // get the files based on filters applied using the method: getFiles
            allFiles = getFiles(folderPath ,recursive,dateCreated,extension,lessThanSize,greaterThanSize,FileType.BOTH);
        }
        else {
            // no filter means that entire folder is to be deleted. along with any other files provided.
            allFiles = getAllFiles(folderPath,recursive, FileType.BOTH);
        }

        // since the target (folder path) may contain standalone files, they also need to be added.
        allFiles = allFiles.stream()
                .filter(Files::isRegularFile)
                .toList();

        // now we check if permanent delete is added or not.
        // first condition is permanent is true
        if (permanent){
            // ask user if they want to do this.
            System.out.printf("Total files found: %d%n", allFiles.size());

            Scanner sc = new Scanner(System.in);
            System.out.println("Do you want to delete this? this action cannot be reversed! (y/N)");
            String userAnswer = sc.nextLine();
            if (userAnswer.equalsIgnoreCase("y")) {
                // files can be deleted permanently but the user may want to keep just the folders.
                // by default the code keeps folders so delete as such. no extra code.
                // first if condition is that folders are to be deleted
                if (deleteFolders){
                    // deleting a folder means deleting its
                    // files as well. so just get the containing
                    // folder of the file and delete it.
                    // use the folderPath list (not the all files list)
                    for (Path path: folderPath){
                        Files.delete(path);
                    }
                }
                // second else is that folders shouldn't be deleted. default code. nothing fancy here.
                else {
                    for (Path path: allFiles){
                        Files.delete(path.toAbsolutePath());
                        System.out.println("file deleted");
                    }
                }
            }
            // user selects no.
            else {
                System.out.println("Operation aborted.");
            }
        }

        // the delete is temporary not permanent. so detect the OS and move to trash.
        else {

        }

    }}


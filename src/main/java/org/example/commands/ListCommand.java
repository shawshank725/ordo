package org.example.commands;

import org.example.enumeration.FileType;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;

import static org.example.commands.FileFetcher.getFiles;

@Command(
        name = "list",
        mixinStandardHelpOptions = true,
        description = """
                List files meeting certain conditions
                Usage: ordo list [-hrV] [-dc=<dateCreated>] [-ext=<extension>]
                [-gsz=<greaterThanSize>] [-lsz=<lessThanSize>] [<folderPath>]"""
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
        List<Path> allFiles = getFiles(
                folderPath,
                recursive,
                dateCreated,
                extension,
                lessThanSize,
                greaterThanSize,
                FileType.FILE
        );

        if (allFiles.isEmpty()) {
            System.out.println("No files matched your filters.");
            return 0;
        }

        System.out.println("Found " + allFiles.size() + " file(s):\n");
        for (Path file : allFiles) {
            long sizeMB = Files.size(file) / (1024 * 1024);
            String sizeStr = sizeMB > 0 ? sizeMB + " MB" : "< 1 MB";
            System.out.printf("%s  (%s)%n",
                    filenameOnly ? file.getFileName() : file,
                    sizeStr);
        }

        return 0;
    }


}

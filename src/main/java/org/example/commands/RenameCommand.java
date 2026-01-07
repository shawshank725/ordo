package org.example.commands;

import org.example.enumeration.FileType;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;

import static org.example.commands.FileFetcher.*;

@Command(
        name = "rename",
        mixinStandardHelpOptions = true,
        description = """
                Batch rename files with patterns
                Usage: ordo rename [-s] [-nn=<newNamePattern>] [-pfx=<prefix>] [-sfx=<suffix>]
                [-t=<type>] <targets>..."""
)
public class RenameCommand implements Callable<Integer> {

    @Parameters(index = "0", arity = "1..*", description = "Files or glob patterns to rename")
    private List<Path> targets;

    @Option(names = {"-nn", "--newname"}, description = "New name (simple rename) or pattern (e.g., photo-{seq})")
    private String newNamePattern;

    @Option(names = {"-pfx", "--prefix"}, description = "Add prefix to original names")
    private String prefix;

    @Option(names = {"-sfx", "--suffix"}, description = "Add suffix to original names")
    private String suffix;

    @Option(names = {"-s", "--seq"}, description = "Add sequential number (use {seq} in pattern)")
    private boolean sequence;

    @Option(names = {"-t", "--type"}, defaultValue = "FILE", description = "By default, files will be affected. If ALL is provided, both folders and files will be affected.")
    private FileType type;

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
        renameFiles(targets, newNamePattern, prefix, suffix,type, sequence);
        return 0;
    }

    public void renameFiles(List<Path> targets, String newNamePattern,
                            String prefix, String suffix,
                            FileType type, boolean sequence) throws IOException {

        // get list of files based on specified parameters
        List<Path> files = getFiles(
                targets, recursive, dateCreated, extension,
                lessThanSize, greaterThanSize, type
        );

        // check if empty or not. if yes then print else traverse
        if (files.isEmpty()){
            System.out.println("The files list is empty.");
            return;
        }

        // sequence number is 1.
        int sequenceNumber = 1;

        // traverse the list to rename the files
        for (Path path: files){
            Path parent = path.getParent();
            String oldFileName = path.getFileName().toString();
            String ext = getExtension(oldFileName);

            // providing a base name, don't want this to break on files with no extensions like dockerfile
            String baseName = oldFileName;
            if (!ext.isEmpty()) {
                baseName = oldFileName.substring(0, oldFileName.length() - ext.length() - 1);
            }

            StringBuilder newName = new StringBuilder();

            // Handle case 1: Pattern provided, but no {seq} in it, and -s flag is given → error
            if (newNamePattern != null && !newNamePattern.isEmpty() && !newNamePattern.contains("{seq}") && sequence) {
                System.err.println("Error: -s (--seq) flag provided, but {seq} not found in the pattern.");
                return;  // Stop the entire operation or continue without seq? (I chose stop for safety)
            }

            // Handle case 2: Pattern provided with {seq}, and -s flag is given → replace {seq} with number
            if (newNamePattern != null && !newNamePattern.isEmpty() && newNamePattern.contains("{seq}") && sequence) {
                String[] patternBroken = breakNewNamePattern(newNamePattern);
                StringBuilder tempNewName = new StringBuilder();
                for (String substring : patternBroken) {
                    tempNewName.append(substring).append(sequenceNumber);
                }
                newName.append(tempNewName);
            }

            // Handle case 3: No pattern, but -s flag is given → rename to number.ext
            else if (newNamePattern == null && sequence) {
                newName.append(sequenceNumber);
            }

            // Handle other cases: Pattern without {seq} and no -s → use pattern as literal
            // Or no pattern and no -s → use original name (or error if you want no-op)
            else if (newNamePattern != null && !newNamePattern.isEmpty()) {
                newName.append(newNamePattern);  // Literal pattern
            } else {
                newName.append(baseName);  // Original name — add error if you don't want no-op?
            }

            // add the prefix and suffix if any
            if (prefix != null){
                newName.insert(0, prefix);
            }
            if (suffix != null){
                newName.append(suffix);
            }
            // add extension
            if (!ext.isEmpty()){
                newName.append(".").append(ext);
            }
            // new name building is complete at this point.

            // checking if name exists or not.
            Path finalNewPath = parent.resolve(newName.toString());
            int counter = 1;

            // Build the base name without extension for collision
            String baseForCollision = newName.toString();
            if (!ext.isEmpty()) {
                baseForCollision = baseForCollision.substring(0, baseForCollision.length() - ext.length() - 1);
            }

            while (Files.exists(finalNewPath)) {
                String counterStr = " (" + counter + ")";
                String collisionName = baseForCollision + counterStr + (ext.isEmpty() ? "" : "." + ext);
                finalNewPath = parent.resolve(collisionName);
                counter++;
            }

            try {
                Files.move(path, finalNewPath);
                String displayedNewName = finalNewPath.getFileName().toString();
                System.out.printf("Renamed: %s → %s%n", oldFileName, displayedNewName);
            } catch (IOException e) {
                String displayedNewName = finalNewPath.getFileName().toString();
                System.err.printf("Failed to rename: %s → %s (%s)%n", oldFileName, displayedNewName, e.getMessage());
            }

            sequenceNumber++;
        }
    }
}
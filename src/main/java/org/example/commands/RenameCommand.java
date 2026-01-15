package org.example.commands;

import org.example.enumeration.FileType;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;

import static org.example.commands.FileFetcher.*;

@Command(
        name = "rename",
        mixinStandardHelpOptions = true,
        description = "Batch rename files with patterns"
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
        renameFiles(targets, newNamePattern, prefix, suffix,FileType.FILE, sequence);
        return 0;
    }

    public void renameFiles(List<Path> targets, String newNamePattern,
                            String prefix, String suffix,
                            FileType type, boolean sequence) throws IOException {
        if (targets.isEmpty()) {
            System.out.println("The target list is empty. Operation cannot be performed.");
            return;
        }
        List<Path> files = getFiles(
                targets, recursive, dateCreated, extension,
                lessThanSize, greaterThanSize, type
        );

        if (files.isEmpty()){
            System.out.println("The files list is empty. Operation cannot be performed.");
            return;
        }

        if (hasRenameInstruction()) {
            int sequenceNumber = 1;
            for (Path path : files ) {
                if (Files.isRegularFile(path)){
                    Path parent = path.getParent();
                    if (parent == null){
                        parent = Path.of(".");
                    }
                    String newName = giveNewName(parent, path, sequenceNumber);
                    Path newFilePath = parent.resolve(newName);
                    if (path.equals(newFilePath)) {
                        System.out.println("Skipping (same name): " + path);
                        continue;
                    }
                    try {
                        Files.move(path, newFilePath);
                        System.out.println("Renamed file: " + path.getFileName() + " to " + newFilePath.getFileName());
                        sequenceNumber++;
                    } catch (Exception e){
                        Files.move(path, newFilePath, StandardCopyOption.REPLACE_EXISTING);
                        //System.out.println("Failed to rename the file: " + path + ". Error: " + e.getMessage());
                    }
                }
            }
        }
        else {
            System.out.println("Naming pattern is not provided.");
        }
    }

    private boolean hasRenameInstruction() {
        return
                (newNamePattern != null && !newNamePattern.trim().isEmpty()) ||
                        (prefix       != null && !prefix.trim().isEmpty())         ||
                        (suffix       != null && !suffix.trim().isEmpty());
    }

    // this function checks the existence of a file and returns a new name
    private String giveNewName(Path parent, Path path, int sequenceNumber){
        StringBuilder newName = new StringBuilder();
        String extensionFromOldName = getExtension(path.toString());
        int counter = 1;

        if (newNamePattern != null) {
            if (sequence && newNamePattern.contains("{seq}")) {
                newName.append(newNamePattern.replace("{seq}", Integer.toString(sequenceNumber)));
            }
            else if (!sequence || !newNamePattern.contains("{seq}")){
                newName.append(newNamePattern);
            }
        }
        else {
            newName.append(getFileNameWithoutExtension(path,extensionFromOldName));
        }
        if (prefix != null)
            newName.insert(0, prefix);
        if (suffix != null)
            newName.append(suffix);

        String tempNewName = extensionFromOldName.isEmpty() ? newName.toString() :newName.toString() + "." + extensionFromOldName;
        Path finalFilePath = parent.resolve(tempNewName);
        while (Files.exists(finalFilePath)){
            System.out.println("OI" + path);
            String counterString = " ("+ counter + ")";
            newName.append(counterString);
            tempNewName = extensionFromOldName.isEmpty() ? newName.toString() :newName.toString() + "." + extensionFromOldName;
            finalFilePath = parent.resolve(tempNewName);
            counter++;
        }
        if (!extensionFromOldName.isEmpty()) {
            newName.append(".").append(extensionFromOldName);
        }
        return newName.toString();
    }
}
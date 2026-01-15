package org.example.commands;

import org.example.enumeration.FileTransfer;
import org.example.enumeration.FileType;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FileFetcher {

    // this method is to get a list of all file paths provided by the user.
    // the single files are added as is. but the folders are checked (if true, then recursively)
    public static List<Path> getFiles(
            List<Path> targets,
            boolean recursive,
            LocalDate dateCreated,
            String extension,
            double lessThanSizeMB,    // in MB
            double greaterThanSizeMB, // in MB
            FileType type             // FILE, DIRECTORY, or ALL
    ) throws IOException {
        List<Path> allFiles = new ArrayList<>();
        long lessThanBytes = lessThanSizeMB > 0 ? (long) (lessThanSizeMB * 1024 * 1024) : Long.MAX_VALUE;
        long greaterThanBytes = greaterThanSizeMB > 0 ? (long) (greaterThanSizeMB * 1024 * 1024) : 0;

        // Normalize extension (e.g., "jpg" or ".jpg" → "jpg")
        String targetExt = extension != null ? extension.toLowerCase().replaceFirst("^\\.*", "") : null;

        for (Path target : targets) {
            Path resolved = target.toAbsolutePath().normalize();

            if (!Files.exists(resolved)) {
                System.err.println("Warning: Path does not exist: " + resolved);
                continue;
            }

            List<Path> candidates = new ArrayList<>();

            if (Files.isRegularFile(resolved)) {
                candidates.add(resolved);
            } else if (Files.isDirectory(resolved)) {
                if (recursive) {
                    Files.walk(resolved)
                            .filter(p -> type == FileType.BOTH ||
                                    (type == FileType.FILE && Files.isRegularFile(p)) ||
                                    (type == FileType.DIRECTORY && Files.isDirectory(p)))
                            .forEach(candidates::add);
                } else {
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(resolved)) {
                        for (Path entry : stream) {
                            if (type == FileType.BOTH ||
                                    (type == FileType.FILE && Files.isRegularFile(entry)) ||
                                    (type == FileType.DIRECTORY && Files.isDirectory(entry))) {
                                candidates.add(entry);
                            }
                        }
                    }
                }
            }

            // Now apply all filters
            for (Path file : candidates) {
                boolean matches = true;

                // Extension filter
                if (targetExt != null) {
                    String fileName = file.getFileName().toString().toLowerCase();
                    matches = fileName.endsWith("." + targetExt) || fileName.equals(targetExt);
                }

                // Size filters
                if (matches) {
                    try {
                        long size = Files.size(file);
                        if (size >= lessThanBytes) matches = false;     // too big
                        if (size < greaterThanBytes) matches = false;   // too small
                    } catch (IOException e) {
                        System.err.println("Warning: Could not read size of " + file);
                        matches = false;
                    }
                }

                // Date created filter (approximate — uses last modified if creation not available)
                if (matches && dateCreated != null) {
                    try {
                        var creationTime = Files.readAttributes(file, BasicFileAttributes.class).creationTime();
                        var fileTime = creationTime != null
                                ? creationTime.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                                : Files.getLastModifiedTime(file).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

                        matches = fileTime.equals(dateCreated);
                    } catch (IOException e) {
                        System.err.println("Warning: Could not read date of " + file);
                        matches = false;
                    }
                }

                if (matches) {
                    allFiles.add(file);
                }
            }
        }

        return allFiles;
    }

    public static List<Path> getAllFiles(
            List<Path> targets, boolean recursive, FileType type
    ) throws IOException {
        List<Path> allFiles = new ArrayList<>();

        for (Path target : targets) {
            Path resolved = target.toAbsolutePath().normalize();

            if (!Files.exists(resolved)) {
                System.err.println("Warning: Path does not exist: " + resolved);
                continue;
            }

            List<Path> candidates = new ArrayList<>();

            if (Files.isRegularFile(resolved)) {
                candidates.add(resolved);
            } else if (Files.isDirectory(resolved)) {
                if (recursive) {
                    Files.walk(resolved)
                            .filter(p -> type == FileType.BOTH ||
                                    (type == FileType.FILE && Files.isRegularFile(p)) ||
                                    (type == FileType.DIRECTORY && Files.isDirectory(p)))
                            .forEach(candidates::add);
                } else {
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(resolved)) {
                        for (Path entry : stream) {
                            if (type == FileType.BOTH ||
                                    (type == FileType.FILE && Files.isRegularFile(entry)) ||
                                    (type == FileType.DIRECTORY && Files.isDirectory(entry))) {
                                candidates.add(entry);
                            }
                        }
                    }
                }
            }
            allFiles.addAll(candidates);
        }

        return allFiles;
    }

    public static String getExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot <= 0 || lastDot == fileName.length() - 1) {
            return "";  // no extension or hidden file like .gitignore
        }
        return fileName.substring(lastDot + 1);
    }

    // break new name pattern from rename command.
    // follow this pattern: "sample {seq} {seq} file {seq}"
    public static String[] breakNewNamePattern(String pattern){
        return pattern.split("\\{seq}");
    }


    public static String getFileNameWithoutExtension(Path path, String extension){
        String fileName = String.valueOf(path.getFileName());
        fileName = fileName.replace("." + extension, "");
        return fileName;
    }
}

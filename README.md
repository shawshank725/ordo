# Ordo

Ordo is a cross-platform command-line tool for advanced file and directory management. Built in Java with Picocli, it offers safe and powerful batch operations including listing, renaming, transferring (move/copy), and deletion with comprehensive filtering and safety mechanisms.

Designed for developers, power users, and anyone managing large collections of files (photos, documents, project assets, downloads), Ordo emphasizes usability, predictability, and protection against accidental data loss.

Note: This tool is in development and is being checked for any unexpected behaviour or issues. If you find any issue, open one in this repository.

## Features

- Smart file listing with filters (extension, size, creation date, recursive)
- Powerful batch renaming with patterns, sequential numbering, prefix/suffix, and automatic collision handling
- Safe file transfer (move/copy) with filtering and destination validation
- Configurable deletion: safe (moves to trash) by default, optional permanent delete
- Dry-run support and confirmation prompts for destructive operations
- Clean, colored, and structured output
- Single executable JAR (future native binaries via GraalVM planned)

## Build and Run
1. Clone the repository:
```bash
git clone https://github.com/shawshank725/ordo.git
cd ordo
```
2. Build project using Maven:
```bash
mvn clean package
```
3. This will produce an executable JAR file in the target directory.
4. If using Linux Distro, try using alias ordo="path-to-ordo-jar-file-in-your-system".
5. Type in terminal:
```bash
ordo --help
```



<h1 align='center'>Ordo</h1>
<div align="center">
 
  <img src="https://img.shields.io/badge/Java-17+-blue?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 17+"/> <img src="https://img.shields.io/badge/Picocli-CLI-orange?style=for-the-badge&logo=picocli&logoColor=white" alt="Picocli"/> <img src="https://img.shields.io/badge/GraalVM-Native%20Image-green?style=for-the-badge&logo=graalvm&logoColor=white" alt="GraalVM"/> <img src="https://img.shields.io/badge/GitHub%20Pages-Deployed-brightgreen?style=for-the-badge&logo=github&logoColor=white" alt="GitHub Pages"/>

  [![Download Latest Release](https://img.shields.io/badge/Download%20Latest-brightgreen?style=for-the-badge&logo=github&logoColor=white)](https://github.com/shawshank725/ordo/releases/latest)
</div>

Ordo is a cross-platform command-line tool for advanced file and directory management. Built in Java with Picocli, it offers safe and powerful batch operations including listing, renaming, transferring (move/copy), and deletion with comprehensive filtering and safety mechanisms.

Designed for developers, power users, and anyone managing large collections of files (photos, documents, project assets, downloads), Ordo emphasizes usability, predictability, and protection against accidental data loss.

## Features

- Smart file listing with filters (extension, size, creation date, recursive)
- Powerful batch renaming with patterns, sequential numbering, prefix/suffix, and automatic collision handling
- Safe file transfer (move/copy) with filtering and destination validation
- Configurable deletion: safe (moves to trash) by default, optional permanent delete
- Clean, colored, and structured output
- Single executable JAR and Native binaries via GraalVM can be built
- Option to open any existing folder

## Project Scope & Future Plans

**Current scope**  
Ordo is focused on **basic but powerful file operations** in the terminal:  
- List files/folders with filters  
- Batch rename (patterns, prefix/suffix, sequencing)  
- Delete (with filters, trash or permanent, recursive)  
- Transfer/move/copy files  
- Open files/folders with system default apps  

If you have ideas, suggestions, or want something specific — open an issue or drop a comment.  

## Build and Run

### Building the JAR file
1. Clone the repository:
```bash
git clone https://github.com/shawshank725/ordo.git
cd ordo
```
2. Build the project using Maven:`mvn clean package`.
3. This will generate an executable JAR file inside the `target/` directory.
4. (Optional – Linux/macOS) Create an alias for easier usage:`alias ordo="java -jar /path/to/ordo.jar"`.
5. Verify the installation:`ordo --help`.
---

### Installing GraalVM
GraalVM is used to build a standalone native executable for this CLI tool.
1. Go to the official GraalVM website: https://www.graalvm.org/downloads/
2. Select your operating system and download the appropriate distribution.  
   On Linux, this will typically be a `.tar.gz` archive.
3. Extract the downloaded archive:`tar -xvf graalvm-*.tar.gz`
4. Set up environment variables. Open your shell configuration file: `nano ~/.bashrc`
5. Add the following lines at the end (update the path accordingly):
```bash
export JAVA_HOME=/path/to/graalvm
export PATH="$JAVA_HOME/bin:$PATH"
```
6. Reload the configuration: `source ~/.bashrc`
7. Verify the installation:`java --version`. The output should match
```text
Java(TM) SE Runtime Environment Oracle GraalVM
```
8. Verify that `native-image` is installed:`native-image --version`.
---

### Building a Native Executable (Standalone Binary)
1. Build the project and generate the native image:
```bash
mvn clean package
mvn -Pnative package
```
> Note: The first native build may take several minutes.
2. Once completed, the executable will be available in the `target/` directory. Open terminal in the directory `target`.
3. For Linux, set proper permissions using the command: `chmod +x ordo`.
4. Then use the executable: `ordo`.

## Known Issues & Workarounds

### Native executable (`./ordo`) shows incomplete `--help` output for subcommands

**Symptom**  
`./ordo rename --help` shows only `-h/-V` options, missing your custom flags (like `-r`, `-dc`, etc.).  
The JAR version (`java -jar ordo.jar rename --help`) works fine.

**Cause**  
GraalVM native-image removes reflection info unless explicitly registered. Picocli's option discovery for subcommands relies on it.

**Fix** (already included in the project)  
The `src/main/resources/META-INF/native-image/reflect-config.json` file registers all subcommand classes + fields.  
If you fork/build from source and still see this:
- Make sure the file exists and has `"allDeclaredFields": true` for each subcommand
- Clean & rebuild: `mvn clean package -Pnative`

This is a common GraalVM + Picocli gotcha — reported in several issues (e.g., picocli#1916, #2357).  
The config file is the standard workaround.

### Other native build tips
- First native build is slow (3–10 min) — normal.
- Need `build-essential` + `zlib1g-dev` on Linux.
- If "no constructor" error → add subcommand classes to `reflect-config.json` (already done here).

## Documentation
### Rename command
- Used to rename files.
- Main command: `ordo rename`. To get help, use `ordo rename --help`.
- The options used are:
```bash
Usage: ordo rename [-hrsV] [-dc=<dateCreated>] [-ext=<extension>]
                   [-gsz=<greaterThanSize>] [-lsz=<lessThanSize>]
                   [-nn=<newNamePattern>] [-pfx=<prefix>] [-sfx=<suffix>]
                   <targets>...
Batch rename files with patterns
      <targets>...   Files or glob patterns to rename
      -dc, --datecreated=<dateCreated>
                     Adding date to filter the files and folders.
      -ext, --extension=<extension>
                     Extension to filter out the files.
      -gsz, --greaterthansize=<greaterThanSize>
                     Filter out files and folders having size greater than the
                       one provided.
  -h, --help         Show this help message and exit.
      -lsz, --lessthansize=<lessThanSize>
                     Filter out files and folders having size less than the one
                       provided.
      -nn, --newname=<newNamePattern>
                     New name (simple rename) or pattern (e.g., photo-{seq})
      -pfx, --prefix=<prefix>
                     Add prefix to original names
  -r, --recursive    Recursive means that any folder and subsequent subfolders
                       will be affected.
  -s, --seq          Add sequential number (use {seq} in pattern)
      -sfx, --suffix=<suffix>
                     Add suffix to original names
  -V, --version      Print version information and exit.
```
- For example, to rename all files in a folder (including subfolders) and a separate file, a new name pattern can be provided, with prefix and suffix as well. `ordo rename bro/ file.txt --newname="file {seq}" -s -r`

### Delete Command
- Used to delete multiple files based on a filter.
- Main command is `ordo delete`. To get help, write: `ordo delete --help`.
- The options used are:
```bash
Usage: ordo delete [-hprV] [-df] [-fno] [-dc=<dateCreated>] [-ext=<extension>]
                   [-gsz=<greaterThanSize>] [-lsz=<lessThanSize>] [<targets>...]
Delete files matching filters (safe by default, moves to trash if possible)
      [<targets>...]         Folder path(s) or file globs (default: current
                               directory)
      -dc, --datecreated=<dateCreated>
                             Filter by creation date (YYYY-MM-DD)
      -df, --deletefolders   Delete folders as well
      -ext, --extension=<extension>
                             Filter by file extension
      -fno, --filenameonly   Show only file names in output
      -gsz, --greaterthansize=<greaterThanSize>
                             Filter files larger than size (in MB)
  -h, --help                 Show this help message and exit.
      -lsz, --lessthansize=<lessThanSize>
                             Filter files smaller than size (in MB)
  -p, --permanent            Permanently delete (bypass trash/recycle bin)
  -r, --recursive            Search for files and folders recursively inside
                               directories
  -V, --version              Print version information and exit.
```
- The filters can be applied the same way we do in rename command.

### List Command
- Used to list files in a folder. Works the same way Linux's `ls` does.
- Main command is `ordo list`. By default it lists files in the current directory. Options can be provided like extensions, size, etc.
- Usage:
```bash
Usage: ordo list [-hrV] [-fno] [-dc=<dateCreated>] [-ext=<extension>]
                 [-gsz=<greaterThanSize>] [-lsz=<lessThanSize>]
                 [<folderPath>...]
List files meeting certain conditions
      [<folderPath>...]      Folder path
      -dc, --datecreated=<dateCreated>
                             Adding date to filter the files and folders.
      -ext, --extension=<extension>
                             Extension to filter out the files.
      -fno, --filenameonly   Adding this prints only the file names (skips the
                               folder name).
      -gsz, --greaterthansize=<greaterThanSize>
                             Filter out files and folders having size greater
                               than the one provided.
  -h, --help                 Show this help message and exit.
      -lsz, --lessthansize=<lessThanSize>
                             Filter out files and folders having size less than
                               the one provided.
  -r, --recursive            Recursive means that any folder and subsequent
                               subfolders will be affected.
  -V, --version              Print version information and exit.
```

### Open command
- Used to open up some folder in the system's default file manager.
- If folder is not found, appropriate error is given. Providing no arguments will open the current working directory.
- Usage:
```bash
Usage: ordo open [-hV] [<path>]
Open a file or folder using the system default application
      [<path>]    File or folder path to open (default: current directory)
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
```

### Transfer Command
- Used to transfer files to a target folder.
- It can either move (cut) or copy files entirely.
- Usage:
```bash
Usage: ordo transfer [-chmrV] [-d=<destination>] [-dc=<dateCreated>]
                     [-ext=<extension>] [-gsz=<greaterThanSize>]
                     [-lsz=<lessThanSize>] <from>...
Batch transfer files with certain conditions
      <from>...     Files or glob patterns to rename
  -c, --copy        Copy files (keep source)
  -d, --destination=<destination>
                    Destination folder
      -dc, --datecreated=<dateCreated>
                    Adding date to filter the files and folders.
      -ext, --extension=<extension>
                    Extension to filter out the files.
      -gsz, --greaterthansize=<greaterThanSize>
                    Filter out files and folders having size greater than the
                      one provided.
  -h, --help        Show this help message and exit.
      -lsz, --lessthansize=<lessThanSize>
                    Filter out files and folders having size less than the one
                      provided.
  -m, --move        Move files (cut - delete source)
  -r, --recursive   Recursive means that any folder and subsequent subfolders
                      will be affected.
  -V, --version     Print version information and exit.
```

Feel free to open an issue if something breaks — happy to help!













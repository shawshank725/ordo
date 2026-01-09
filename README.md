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

Feel free to open an issue if something breaks — happy to help!


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

2. Build the project using Maven:
```bash
mvn clean package
```

3. This will generate an executable JAR file inside the `target/` directory.

4. (Optional – Linux/macOS) Create an alias for easier usage:
```bash
alias ordo="java -jar /path/to/ordo.jar"
```

5. Verify the installation:
```bash
ordo --help
```

---

### Installing GraalVM

GraalVM is used to build a standalone native executable for this CLI tool.

1. Go to the official GraalVM website:  
   https://www.graalvm.org/downloads/

2. Select your operating system and download the appropriate distribution.  
   On Linux, this will typically be a `.tar.gz` archive.

3. Extract the downloaded archive:
```bash
tar -xvf graalvm-*.tar.gz
```

4. Set up environment variables. Open your shell configuration file:
```bash
nano ~/.bashrc
```

5. Add the following lines at the end (update the path accordingly):
```bash
export JAVA_HOME=/path/to/graalvm
export PATH="$JAVA_HOME/bin:$PATH"
```

6. Reload the configuration:
```bash
source ~/.bashrc
```

7. Verify the installation:
```bash
java --version
```

Expected output (example):
```text
Java(TM) SE Runtime Environment Oracle GraalVM
```

8. Verify that `native-image` is installed:
```bash
native-image --version
```

---

### Building a Native Executable (Standalone Binary)

1. Build the project and generate the native image:
```bash
mvn clean package
mvn -Pnative package
```

> Note: The first native build may take several minutes.

2. Once completed, the executable will be available in the `target/` directory.

3. Run the executable (Linux):
```bash
./target/ordo
```

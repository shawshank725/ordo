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

### Building JAR file
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
4. If using Linux Distro, try using alias ordo="java -jar path-to-ordo-jar-file-in-your-system".
5. Type in terminal:
```bash
ordo --help
```
### Installing GraalVM
1. GraalVM is used to create an executable file for this CLI tool. To build it from source, we need GraalVM.
2. Go to GraalVM official website: https://www.graalvm.org/downloads/.
3. Select your OS and download the setup. For linux distros, a .tar file will be downloaded.
4. Open the folder where the GraalVM setup is present and extract it.
5. For linux distros, open the terminal in the same folder, type: nano ~/.bashrc. Scroll to the very bottom and add the lines:
```bash
export JAVA_HOME=/home/shashank/graalvm-jdk-25.0.1+8.1
export PATH="$JAVA_HOME/bin:$PATH"
```
6. Save the file. To verify it, write java --version. The output would be similar to this:
```bash
java 25.0.1 2025-10-21 LTS
Java(TM) SE Runtime Environment Oracle GraalVM 25.0.1+8.1 (build 25.0.1+8-LTS-jvmci-b01)
Java HotSpot(TM) 64-Bit Server VM Oracle GraalVM 25.0.1+8.1 (build 25.0.1+8-LTS-jvmci-b01, mixed mode, sharing)
```
7. We need to make sure native image is also installed. Try the command:
native-image --version
8. The output would be similar to this:
native-image 25.0.1 2025-10-21
GraalVM Runtime Environment Oracle GraalVM 25.0.1+8.1 (build 25.0.1+8-LTS-jvmci-b01)
Substrate VM Oracle GraalVM 25.0.1+8.1 (build 25.0.1+8-LTS, serial gc, compressed references)
9. Once this is all done, move to next section

### Building an executable file (standalone executable binary)
1. First run the  command: mvn clean package and then mvn -Pnative package. The first time running the command will take some time. Once this is done, go to Ordo/target folder to get ordo executable.
2. To run it on Linux distros, open the terminal and write: ./ordo.

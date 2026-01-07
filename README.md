# Ordo

Ordo is a cross-platform command-line tool for advanced file and directory management. Built in Java with Picocli, it offers safe and powerful batch operations including listing, renaming, transferring (move/copy), and deletion with comprehensive filtering and safety mechanisms.

Designed for developers, power users, and anyone managing large collections of files (photos, documents, project assets, downloads), Ordo emphasizes usability, predictability, and protection against accidental data loss.

## Features

- Smart file listing with filters (extension, size, creation date, recursive)
- Powerful batch renaming with patterns, sequential numbering, prefix/suffix, and automatic collision handling
- Safe file transfer (move/copy) with filtering and destination validation
- Configurable deletion: safe (moves to trash) by default, optional permanent delete
- Dry-run support and confirmation prompts for destructive operations
- Clean, colored, and structured output
- Single executable JAR (future native binaries via GraalVM planned)

## Installation

Download the latest JAR from [Releases](https://github.com/[your-username]/ordo/releases):

```bash
# Example (replace with actual version)
curl -L https://github.com/[your-username]/ordo/releases/download/v0.1.0/ordo.jar -o ordo.jar

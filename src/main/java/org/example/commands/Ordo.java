package org.example.commands;

import picocli.CommandLine;

@CommandLine.Command(
        name = "ordo",
        description = "Welcome to Ordo!",
        mixinStandardHelpOptions = true,
        version = "Ordo 0.1.0",
        subcommands = {
                RenameCommand.class,
                ListCommand.class,
                DeleteCommand.class,
                TransferCommand.class,
                ExplorerCommand.class
        }
)
public class Ordo implements Runnable{
    @Override
    public void run() {
        System.out.print("""
                  ___            _       \s
                 / _ \\  _ __  __| |  ___ \s
                | | | || '__|/ _` | / _ \\\s
                | |_| || |  | (_| || (_) |
                 \\___/ |_|   \\__,_| \\___/\s
                """);
        System.out.println("Welcome to Ordo, son.");
    }
}

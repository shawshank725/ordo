package org.example;

import org.example.commands.Ordo;
import org.fusesource.jansi.AnsiConsole;
import picocli.CommandLine;

public class Main {
    public static void main(String[] args) {

        int exitCode = new CommandLine(new Ordo())
                .execute(args);

        System.exit(exitCode);
    }
}
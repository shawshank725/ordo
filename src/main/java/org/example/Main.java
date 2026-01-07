package org.example;

import org.example.commands.Ordo;
import picocli.CommandLine;

public class Main {
    public static void main(String[] args) {

        int exitCode = new CommandLine(new Ordo())
                //.setParameterExceptionHandler(new ShortErrorMessageHandler())
                .execute(args);


    }
}
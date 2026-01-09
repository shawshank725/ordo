package org.example.commands;

import picocli.CommandLine;
import picocli.CommandLine.Mixin;

@CommandLine.Command(
        name = "ordo",
        description = "Welcome to @|fg(green) Ordo!|@",
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
    @Mixin
    private CommandLine.HelpCommand helpCommand;

    @Override
    public void run() {
        System.out.print("""
\u001B[32m
   ██████╗ ██████╗ ██████╗  ██████╗ 
  ██╔═══██╗██╔══██╗██╔══██╗██╔═══██╗
  ██║   ██║██████╔╝██║  ██║██║   ██║
  ██║▄▄ ██║██╔══██╗██║  ██║██║   ██║
  ╚██████╔╝██║  ██║██████╔╝╚██████╔╝
   ╚═════╝ ╚═╝  ╚═╝╚═════╝  ╚═════╝ 
\u001B[0m
""");
        System.out.println("Welcome to Ordo, son.");

    }
}

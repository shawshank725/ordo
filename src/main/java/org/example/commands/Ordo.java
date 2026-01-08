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

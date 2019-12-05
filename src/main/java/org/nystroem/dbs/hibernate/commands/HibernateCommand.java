package org.nystroem.dbs.hibernate.commands;

import org.nystroem.dbs.hibernate.commands.subcommands.HibernateVerticalCommand;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import static picocli.CommandLine.RunLast;

@Command(name = "struct")
public class HibernateCommand implements Runnable {

    public static void main(String[] args) {
        CommandLine cmdLine = new CommandLine(new HibernateCommand());
        cmdLine.addSubcommand("vert", new HibernateVerticalCommand());

        cmdLine.parseWithHandler(new RunLast(), args);
    }

    @Override public void run() {
        System.out.println("[Hibernate] Preparing model generation...");
    }
    
}
package org.nystroem.dbs.hibernate;

import javax.management.InstanceAlreadyExistsException;

import javax.persistence.EntityManagerFactory;

import org.nystroem.dbs.hibernate.commands.HibernateCommand;
import org.nystroem.dbs.hibernate.commands.subcommands.HibernateVerticalCommand;
import org.nystroem.dbs.hibernate.store.Mutator;

import picocli.CommandLine;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        // Argument handling with Picoli
        CommandLine cmdl = new CommandLine(new HibernateCommand());
        cmdl.addSubcommand("vert", new HibernateVerticalCommand());

        cmdl.parseWithHandler(new CommandLine.RunLast(), args);

        try {
            // Initialization
            SharedModules.initialize();
        } catch (InstanceAlreadyExistsException e) {
            e.printStackTrace();
            System.exit(1);
        }

        SharedModules.core().setup().start();

        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("-----------------------------");
                System.out.println("Closing open connections...");
                ((EntityManagerFactory)SharedModules.core().store().handleCachedObject(Mutator.ENTITYMANAGERFACTORY, null)).close();
                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

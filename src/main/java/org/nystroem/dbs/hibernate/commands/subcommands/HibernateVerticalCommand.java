package org.nystroem.dbs.hibernate.commands.subcommands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "vert")
public class HibernateVerticalCommand implements Runnable {

    @Option(names = "-D")
    private boolean isDebugEnbaled;

    @Override public void run() {
        if (isDebugEnbaled)
            System.out.println("[Hibernate] Enabling debugging! Generating hibernate models....");
    }

}
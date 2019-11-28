package org.nystroem.dbs.hibernate;

import javax.management.InstanceAlreadyExistsException;

import org.nystroem.dbs.hibernate.core.ApplicationCore;

public class SharedModules {
    // ApplicationCore
    private static ApplicationCore core = null;

    public static void initialize() throws InstanceAlreadyExistsException {
        if (core == null)
            core = new ApplicationCore();
    }

    public static ApplicationCore core() {
        return core;
    }

}
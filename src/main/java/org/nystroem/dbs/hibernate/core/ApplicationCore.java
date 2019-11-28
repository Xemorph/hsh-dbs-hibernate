/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Benny Nystroem. All rights reserved.
 *  Licensed under the GNU GENERAL PUBLIC LICENSE v3 License. 
 *  See LICENSE in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
package org.nystroem.dbs.hibernate.core;

import java.io.File;
import java.io.InvalidObjectException;
import java.util.Properties;

import javax.management.InstanceAlreadyExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.SwingUtilities;

import org.hibernate.cache.CacheException;
import org.nystroem.dbs.hibernate.frontend.gui.SearchMovieDialog;
import org.nystroem.dbs.hibernate.frontend.gui.SearchMovieDialogCallback;
import org.nystroem.dbs.hibernate.store.Mutator;
import org.nystroem.dbs.hibernate.store.Store;

import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;

public class ApplicationCore {
    /** --------------------------------------------------------------------------------------------------------------------- */
    //  Constants
    /** --------------------------------------------------------------------------------------------------------------------- */
    public static boolean initialized = false;
    public static boolean started = false;
    
    /** --------------------------------------------------------------------------------------------------------------------- */
    //  Private variables
    /** --------------------------------------------------------------------------------------------------------------------- */
    // Current Working Directory -> cwd
    private String cwd = "";
    // Store, cache system
    private Store store = new Store();

    /** --------------------------------------------------------------------------------------------------------------------- */
    //  Constructor - Singleton
    /** --------------------------------------------------------------------------------------------------------------------- */
    public ApplicationCore() throws InstanceAlreadyExistsException {
        if (ApplicationCore.initialized)
            throw new InstanceAlreadyExistsException("`ApplicationCore` can only have one instance!");
        ApplicationCore.initialized = true;
    }

    /** --------------------------------------------------------------------------------------------------------------------- */
    //  Public methods
    /** --------------------------------------------------------------------------------------------------------------------- */
    public final ApplicationCore setup() {
        this.cwd = new File(".checkfis").getAbsolutePath();
        Properties props = new Properties();
        props.put("hibernate.search.default.indexBase", this.cwd.replaceAll("\\.checkfis", ""));
        store().handleCachedObject(Mutator.ENTITYMANAGERFACTORY, Persistence.createEntityManagerFactory("org.nystroem.dbserv", props));
        // Method chaining
        return this;
    }
    public final void start() {
        if (store().handleCachedObject(Mutator.ENTITYMANAGERFACTORY, null) == null || ApplicationCore.started)
            throw new CacheException("Fatal Error -> The `EntityManagerFactory` isn't available!");
        // Create indexes
        EntityManager em = ((EntityManagerFactory)store().handleCachedObject(Mutator.ENTITYMANAGERFACTORY, null)).createEntityManager();
        FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
        try {
            ftem.createIndexer().startAndWait();
        } catch (InterruptedException e) {
            em.close();
            e.printStackTrace();
        }
        // Application hast started
        ApplicationCore.started = true;
        // Starting Frontend [Swing Components]
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    ApplicationCore.run();
                } catch (InvalidObjectException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        });
    }

    /** --------------------------------------------------------------------------------------------------------------------- */
    //  Getters - Public methods
    /** --------------------------------------------------------------------------------------------------------------------- */
    public String cwd() {
        return this.cwd;
    }
    public Store store() {
        return this.store;
    }

    /** --------------------------------------------------------------------------------------------------------------------- */
    //  Runnable method
    /** --------------------------------------------------------------------------------------------------------------------- */
    public static void run() throws InvalidObjectException {
        if (!ApplicationCore.initialized || !ApplicationCore.started)
            throw new InvalidObjectException("`ApplicationCore` hasn't be started or initialized!");
        SearchMovieDialogCallback callback = new SearchMovieDialogCallback();
        SearchMovieDialog sd = new SearchMovieDialog(callback);
        sd.setVisible(true);
    }

}
/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Benny Nystroem. All rights reserved.
 *  Licensed under the GNU GENERAL PUBLIC LICENSE v3 License. 
 *  See LICENSE in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
package org.nystroem.dbs.hibernate.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.management.InstanceAlreadyExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.hibernate.cache.CacheException;
import org.hibernate.cfg.Configuration;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.nystroem.dbs.hibernate.frontend.gui.SearchMovieDialog;
import org.nystroem.dbs.hibernate.frontend.gui.SearchMovieDialogCallback;
import org.nystroem.dbs.hibernate.store.Mutator;
import org.nystroem.dbs.hibernate.store.Store;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class ApplicationCore {
    /**
     * ---------------------------------------------------------------------------------------------------------------------
     */
    // Constants
    /**
     * ---------------------------------------------------------------------------------------------------------------------
     */
    public static boolean initialized = false;
    public static boolean started = false;

    private static Logger LOGGER = new Logger(ApplicationCore.class);

    /**
     * ---------------------------------------------------------------------------------------------------------------------
     */
    // Private variables
    /**
     * ---------------------------------------------------------------------------------------------------------------------
     */
    // Current Working Directory -> cwd
    private String cwd = "";
    // Store, cache system
    private Store store = new Store();

    /**
     * ---------------------------------------------------------------------------------------------------------------------
     */
    // Constructor - Singleton
    /**
     * ---------------------------------------------------------------------------------------------------------------------
     */
    public ApplicationCore() throws InstanceAlreadyExistsException {
        if (ApplicationCore.initialized)
            throw new InstanceAlreadyExistsException("`ApplicationCore` can only have one instance!");
        ApplicationCore.initialized = true;
    }

    /**
     * ---------------------------------------------------------------------------------------------------------------------
     */
    // Public methods
    /**
     * ---------------------------------------------------------------------------------------------------------------------
     */
    public final ApplicationCore setup() {
        // Configuration stuff, e.g. Credentials for database connection
        // Using java.nio.file.* mechanics introduced in Java 7 as a new feature
        Path configPath = Paths.get("config/dbs_hibernate.conf");
        if (!Files.exists(configPath.getParent(), LinkOption.NOFOLLOW_LINKS)) {
            // Create here the new path & file, because if Path doesnt exist then there
            // is no configuration file for our application
            try {
                Files.createDirectory(configPath.getParent());
            } catch (IOException e) {
                LOGGER.error("Couldn't create config folder & file! Error: ", e);
                System.exit(1);
            }
        }
        // Path exists but let us check for the file
        if (!Files.exists(configPath, LinkOption.NOFOLLOW_LINKS)) {
            // File doesn't exist, we need to create it
            try {
                Files.createFile(configPath);

                List<String> ctx = Arrays.asList("configuration {", "    url=\"jdbc:oracle:thin:@localhost:1521:db01\"",
                        "    username=\"\"", "    password=\"\"", "}");
                Files.write(configPath, ctx, StandardCharsets.ISO_8859_1, StandardOpenOption.APPEND);

                LOGGER.info("Configuration file created. Please, fill out the new configuration file!");
                System.exit(0);
            } catch (IOException e) {
                LOGGER.error("Couldn't create config file! Error: ", e);
                System.exit(1);
            }
        }
        // Okay, we're good to go now. Let'us read the configuration
        Config conf = ConfigFactory.parseFile(configPath.toFile());
        String url = conf.getString("configuration.url");
        String username = conf.getString("configuration.username");
        String password = conf.getString("configuration.password");
        if (url.isEmpty() || username.isEmpty() || password.isEmpty()) {
            LOGGER.error("Invalid configuration, please check your configuration!");
            System.exit(1);
        }
        // Let's read `persistence.xml` in META-INF and change the default values
        try {
            Configuration cfg = new Configuration();
            cfg.addInputStream(this.getClass().getClassLoader().getResourceAsStream("META-INF/persistence.xml"));
            // Set Properties
            cfg.setProperty("hibernate.connection.url", url);
            cfg.setProperty("hibernate.connection.username", username);
            cfg.setProperty("hibernate.connection.password", password);
        } catch (Exception e) {
            LOGGER.fatal("Unable to read & change properties for Hibernate!", e);
            System.exit(1);
        }
        
        // Hibernate stuff
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
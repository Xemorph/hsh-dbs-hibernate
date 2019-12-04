package org.nystroem.dbs.hibernate.frontend.logic;

import java.util.List;

public class GenreManager {

    /**
     * Ermittelt eine vollstaendige Liste aller in der Datenbank abgelegten Genres
     * Die Genres werden alphabetisch sortiert zurueckgeliefert.
     * @return Alle Genre-Namen als String-Liste
     * @throws Exception
     */
    public List<String> getGenres() throws Exception {
        return new GenreFactory().getGenres();
    }

}

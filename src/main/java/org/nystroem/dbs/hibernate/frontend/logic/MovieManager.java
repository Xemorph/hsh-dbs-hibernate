package org.nystroem.dbs.hibernate.frontend.logic;

import java.util.List;

import org.nystroem.dbs.hibernate.frontend.logic.dto.MovieDTO;

public class MovieManager {

    /**
     * Ermittelt alle Filme, deren Filmtitel den Suchstring enthaelt.
     * Wenn der String leer ist, sollen alle Filme zurueckgegeben werden.
     * Der Suchstring soll ohne Ruecksicht auf Gross/Kleinschreibung verarbeitet werden.
     * @param search Suchstring. 
     * @return Liste aller passenden Filme als MovieDTO
     * @throws Exception
     */
    public List<MovieDTO> getMovieList(String search) throws Exception {
        // TODO
        return new MovieFactory().getMovies(search);
    }

    /**
     * Speichert die uebergebene Version des Films neu in der Datenbank oder aktualisiert den
     * existierenden Film.
     * Dazu werden die Daten des Films selbst (Titel, Jahr, Typ) beruecksichtigt,
     * aber auch alle Genres, die dem Film zugeordnet sind und die Liste der Charaktere
     * auf den neuen Stand gebracht.
     * @param movie Film-Objekt mit Genres und Charakteren.
     * @throws Exception
     */
    public void insertUpdateMovie(MovieDTO movieDTO) throws Exception {
        
    }

    /**
     * Loescht einen Film aus der Datenbank. Es werden auch alle abhaengigen Objekte geloescht,
     * d.h. alle Charaktere und alle Genre-Zuordnungen.
     * @param movie
     * @throws Exception
     */
    public void deleteMovie(long movieId) throws Exception {
        // TODO Auto-generated method stub
    }

    public MovieDTO getMovie(long movieId) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}

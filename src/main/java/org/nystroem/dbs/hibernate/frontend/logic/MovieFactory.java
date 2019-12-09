package org.nystroem.dbs.hibernate.frontend.logic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.nystroem.dbs.hibernate.SharedModules;
import org.nystroem.dbs.hibernate.core.Logger;
import org.nystroem.dbs.hibernate.entities.Genre;
import org.nystroem.dbs.hibernate.entities.Movie;
import org.nystroem.dbs.hibernate.entities.MovieCharacter;
import org.nystroem.dbs.hibernate.entities.Person;
import org.nystroem.dbs.hibernate.entities.sub.CinemaMovie;
import org.nystroem.dbs.hibernate.entities.sub.Series;
import org.nystroem.dbs.hibernate.frontend.gui.ShowErrorDialog;
import org.nystroem.dbs.hibernate.frontend.logic.dto.CharacterDTO;
import org.nystroem.dbs.hibernate.frontend.logic.dto.MovieDTO;
import org.nystroem.dbs.hibernate.store.Mutator;

public class MovieFactory {

    private static Logger LOGGER = new Logger(MovieFactory.class);

    public List<MovieDTO> getMovies(String search) {
        //Local temporary `javax.persistence.EntityManager`
        EntityManager em = ((EntityManagerFactory)SharedModules.core().store().handleCachedObject(Mutator.ENTITYMANAGERFACTORY, null)).createEntityManager();
        // Result list
        List<MovieDTO> rs = new ArrayList<>();
        //Local temporary `FullTextEntityManager`
        FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
        // Decision -> Search
        // [BEGIN] #Transaction
        //Transaktion um auf DB zu sortieren
        em.getTransaction().begin();
        // SearchSortquery wird erstellt und in Zeile 63 ausgeführt
        // SearchSortquery holt alle Movies aus der DB und sortiert sie nach der MovieId
        QueryBuilder qb = ftem.getSearchFactory()
                .buildQueryBuilder().forEntity(Movie.class).get();
        org.apache.lucene.search.Query query = qb
              .keyword()
              .wildcard()
              .onField(Movie.col_title)
              .matching(search.toLowerCase()+"*")
              .createQuery();
        org.apache.lucene.search.Sort sort = new Sort(SortField.FIELD_DOC, new SortField(Movie.col_movieID, SortField.Type.STRING, false)); // Descending order
        // Wrap Lucene query in a `javax.persistence.Query`
        FullTextQuery persistenceQuery = ftem.createFullTextQuery(query, Movie.class);
        persistenceQuery.setSort(sort);
        // Execute search
        persistenceQuery.getResultStream().forEach(movie -> {
            MovieDTO dto = new MovieDTO();
            dto.setId(((Movie)movie).getMovieID());
            dto.setTitle(((Movie)movie).getTitle());
            dto.setType(((Movie)movie).getType().toString());
            dto.setYear(((Movie)movie).getYear());
            // [Missing] Characters
            // [Missing] Genres
            rs.add(dto);
        });
        // [END]
        em.getTransaction().commit();
        //Close local temporary `javax.persistence.EntityManager`
        em.close();
        // Return `List<MovieDTO> rs` as resultset
        return rs;
    }

    public MovieDTO getMovie(long id) {
        //Local temporary `javax.persistence.EntityManager`
        EntityManager em = ((EntityManagerFactory)SharedModules.core().store().handleCachedObject(Mutator.ENTITYMANAGERFACTORY, null)).createEntityManager();
        // [BEGIN] #Transaction
        em.getTransaction().begin();

        // Keep this section for history
        //Query query = em.createQuery(
        //        "SELECT m FROM " + Movie.table + " m " + 
        //        "WHERE m." + Movie.col_movieID + " = :id").setParameter("id", Long.valueOf(id));
        // SingleResultSet -> Movie | No Initialization -> Direct convert
        //Movie rs = (Movie) query.getSingleResult();

        // Create an empty MovieDTO object
        MovieDTO dto = new MovieDTO();
        // Get `Movie` object by ID
        Movie movie = em.find(Movie.class, Long.valueOf(id));
        if (movie instanceof CinemaMovie)
            if (((CinemaMovie)movie).getTicketsSold() != null)
                dto.setTicketsSold(((CinemaMovie)movie).getTicketsSold());
        if (movie instanceof Series)
            if (((Series)movie).getNumOfEpisodes() != null)
                dto.setNumOfEpisodes(((Series)movie).getNumOfEpisodes());
        // Map Movie to MovieDTO
        dto.setId(movie.getMovieID());
        dto.setTitle(movie.getTitle());
        dto.setType(movie.getType().toString());
        dto.setYear(movie.getYear());
        
        movie.getGenres().stream().map(n -> n.getGenre()).forEach(g -> dto.addGenre(g));
        // Map Set<MovieCharacter> to Set<CharacterDTO>
        movie.getMovieCharacters()
            .stream()
            .forEach(mChar -> {
                CharacterDTO cdto = new CharacterDTO();
                cdto.setCharId(mChar.getMovCharID());
                cdto.setCharacter(mChar.getCharacter());
                cdto.setAlias(mChar.getAlias());
                cdto.setPlayer(mChar.getPerson().getName());
                //Add `cdto` to `dto` MovieDTO object
                dto.addCharacter(cdto);
            });

        // [END]
        em.getTransaction().commit();
        //Close local temporary `javax.persistence.EntityManager`
        em.close();
        // Return `MovieDTO` object
        return dto;
    }

    public void insertUpdateMovie(MovieDTO dto) {
        //Local temporary `javax.persistence.EntityManager`
        EntityManager em = ((EntityManagerFactory)SharedModules.core().store().handleCachedObject(Mutator.ENTITYMANAGERFACTORY, null)).createEntityManager();
        // [BEGIN] #Transaction
        em.getTransaction().begin();

        // Generate temporary `Movie` object from the given `MovieDTO` object
        final Movie dto_snapshot;
        if (dto.getTicketsSold() != null)
            dto_snapshot = new CinemaMovie();
        else if (dto.getNumOfEpisodes() != null)
            dto_snapshot = new Series();
        else
            dto_snapshot = new Movie();
        // Set default variables
        dto_snapshot.setTitle(dto.getTitle());
        dto_snapshot.setType(dto.getType());
        dto_snapshot.setYear(dto.getYear());
        // Set inheritanced variables if only needed
        if (dto_snapshot instanceof CinemaMovie)
            ((CinemaMovie)dto_snapshot).setTicketsSold(dto.getTicketsSold());
        if (dto_snapshot instanceof Series)
            ((Series)dto_snapshot).setNumOfEpisodes(dto.getNumOfEpisodes());
        // Get `Genre` object and add it to the `dto_snapshot` via `Movie.addGenre(Genre genre)`
        dto.getGenres().stream().forEach(n -> new Function<String,Void>() {
            // Internal method, which gets executed like this `n -> apply(n)` and returns `void` type
            @Override public Void apply(String t) {
                TypedQuery<Genre> q = em.createQuery("SELECT g FROM " + Genre.table + " g " + 
                                                        "WHERE upper(g." + Genre.col_genre + ") = :name", Genre.class);
                                  q.setParameter("name", t.toUpperCase());
                dto_snapshot.addGenre(q.getSingleResult());
                // Return `Void`, it says `null` but it is a `void` type
                return null;
            }
        }.apply(n));

        // Check for Merge / persist
        if (dto.getId() != null || dto.getId() > 0) {
            // Result => Merge
            Movie db_snapshot = em.find(Movie.class, Long.valueOf(dto.getId()));
            if (db_snapshot != null)
                dto_snapshot.setMovieID(db_snapshot.getMovieID());
            else {
                // The required Movie couldn't be found, do a rollback and get out of this method!
                // [END]
                em.getTransaction().rollback();
                //Close local temporary `javax.persistence.EntityManager`
                em.close();
                // Return, don't execute anything here anymore!
                return;
            }
            // Merge `db_snapshot` with `dto_snapshot` to get the updated `Movie` object
            Movie movie = (Movie) em.merge(dto_snapshot);
        } else {
            // Result => Persist
            em.persist(dto_snapshot);
        }

        // Persist our Movie entity `mov`
        // em.persist(mov);

        // final Set<MovieCharacter> movChars = new HashSet<>();
        // dto.getCharacters()
        //     .stream()
        //     .forEach(c -> new Function<CharacterDTO,Void>() {
        //             @Override public Void apply(CharacterDTO t) {
        //                 MovieCharacter movChar = new MovieCharacter();
        //                 Person person = new Person();

        //                 try {
        //                     movChar = (MovieCharacter) em.createQuery("SELECT c FROM " + MovieCharacter.table + " c " +
        //                                                                     "WHERE c." + MovieCharacter.col_movCharID + " = :id")
        //                                                     .setParameter("id", t.getCharId())
        //                                                     .getSingleResult();
        //                 } catch (NoResultException exc) {
        //                     movChar.setAlias(t.getAlias());
        //                     movChar.setCharacter(t.getCharacter());
        //                     try {
        //                         person = (Person) em.createQuery("SELECT p FROM " + Person.table + " p " +
        //                                                                     "WHERE upper(p." + Person.col_name + ") = :name")
        //                                                 .setParameter("name", t.getPlayer().toUpperCase())
        //                                                 .getSingleResult();
                                
        //                         movChar.setPerson(person);
        //                     } catch (NoResultException ex) {
        //                         person.setName(t.getPlayer());
        //                         em.persist(person);
        //                         movChar.setPerson(person);
        //                     }
        //                 }
        //                 movChars.add(movChar);
        //                 return null;
        //             }
        //     }.apply(c));

        // // Persist MovieCharacter
        // for (MovieCharacter movChar : movChars) {
        //     movChar.setMovie(mov);
        //     em.persist(movChar);
        // }

        // [END]
        em.getTransaction().commit();
        //Close local temporary `javax.persistence.EntityManager`
        em.close();
    }

    public void deleteMovie(long movieId) {
        // Local temporary `javax.persistence.EntityManager`
        EntityManager em = ((EntityManagerFactory)SharedModules.core().store().handleCachedObject(Mutator.ENTITYMANAGERFACTORY, null)).createEntityManager();
        // [BEGIN] #Transaction
        em.getTransaction().begin();
        // Delete `Movie` by the given ID
        boolean deleted = deleteById(Movie.class, Long.valueOf(movieId), em);
        Exception exc = new Exception("");
        if (deleted)
            new ShowErrorDialog(String.format("Movie {%s} deleted!", String.valueOf(movieId)), exc);
        else 
            new ShowErrorDialog(String.format("Error occured while deleting Movie {%s}! Please, check the console for more information.", String.valueOf(movieId)), exc);
        // [END]
        em.getTransaction().commit();
        // Close local temporary `javax.persistence.EntityManager`
        em.close();
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    private boolean deleteById(Class<?> type, Serializable id, EntityManager em) {
        Object persistentInstance = em.find(type, id);
        if (persistentInstance != null) {
            em.remove(persistentInstance);
            return true;
        }
        return false;
    }
}
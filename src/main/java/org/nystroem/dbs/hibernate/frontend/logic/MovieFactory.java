package org.nystroem.dbs.hibernate.frontend.logic;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Query;

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
        final List<MovieDTO> rs = new CopyOnWriteArrayList<>();
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
        MovieDTO dto = new MovieDTO();
        persistenceQuery.getResultList().stream().forEach(movie -> { 
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
        final CharacterDTO cdto = new CharacterDTO();
        movie.getMovieCharacters()
            .stream()
            .forEach(mChar -> {
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
        Movie mov = new Movie();
        if (dto.getTicketsSold() != null)
            mov = new CinemaMovie();
        if (dto.getNumOfEpisodes() != null)
            mov = new Series();
        mov.setTitle(dto.getTitle());
        mov.setType(dto.getType());
        mov.setYear(dto.getYear());

        if (mov instanceof CinemaMovie) {
            LOGGER.debug("Tickets: " + dto.getTicketsSold());
            ((CinemaMovie)mov).setTicketsSold(dto.getTicketsSold());
        }
        if (mov instanceof Series) {
            LOGGER.debug("Episodes: " + dto.getNumOfEpisodes());
            ((Series)mov).setNumOfEpisodes(dto.getNumOfEpisodes());
        }

        //Local temporary `javax.persistence.EntityManager`
        EntityManager tmpEM = ((EntityManagerFactory)SharedModules.core().store().handleCachedObject(Mutator.ENTITYMANAGERFACTORY, null)).createEntityManager();
        // [BEGIN] #Transaction
        //vergessen rauszunehmen
        tmpEM.getTransaction().begin();
        
        final Set<Genre> genres = new HashSet<>();
        dto.getGenres()
            .stream()
            .forEach(g -> genres.add(
                (Genre)tmpEM.createQuery("SELECT g FROM " + Genre.table + " g " + 
                    "WHERE upper(g." + Genre.col_genre + ") = :name").setParameter("name", g.toUpperCase()).getSingleResult()
            ));

        // Persist our Movie entity `mov`
        tmpEM.persist(mov);

        final Set<MovieCharacter> movChars = new HashSet<>();
        dto.getCharacters()
            .stream()
            .forEach(c -> new Function<CharacterDTO,Void>() {
                    @Override public Void apply(CharacterDTO t) {
                        MovieCharacter movChar = new MovieCharacter();
                        Person person = new Person();

                        try {
                            movChar = (MovieCharacter) tmpEM.createQuery("SELECT c FROM " + MovieCharacter.table + " c " +
                                                                            "WHERE c." + MovieCharacter.col_movCharID + " = :id")
                                                            .setParameter("id", t.getCharId())
                                                            .getSingleResult();
                        } catch (NoResultException exc) {
                            movChar.setAlias(t.getAlias());
                            movChar.setCharacter(t.getCharacter());
                            try {
                                person = (Person) tmpEM.createQuery("SELECT p FROM " + Person.table + " p " +
                                                                            "WHERE upper(p." + Person.col_name + ") = :name")
                                                        .setParameter("name", t.getPlayer().toUpperCase())
                                                        .getSingleResult();
                                
                                movChar.setPerson(person);
                            } catch (NoResultException ex) {
                                person.setName(t.getPlayer());
                                tmpEM.persist(person);
                                movChar.setPerson(person);
                            }
                        }
                        movChars.add(movChar);
                        return null;
                    }
            }.apply(c));

        // Persist MovieCharacter
        for (MovieCharacter movChar : movChars) {
            movChar.setMovie(mov);
            tmpEM.persist(movChar);
        }
        // [END]
        tmpEM.getTransaction().commit();
        //Close local temporary `javax.persistence.EntityManager`
        tmpEM.close();
    }

    public void deleteMovie(long movieId) {
        // Local temporary `javax.persistence.EntityManager`
        EntityManager em = ((EntityManagerFactory)SharedModules.core().store().handleCachedObject(Mutator.ENTITYMANAGERFACTORY, null)).createEntityManager();
        // [BEGIN] #Transaction
        em.getTransaction().begin();
        // Delete `Movie` by the given ID
        boolean deleted = deleteById(Movie.class, Long.valueOf(movieId), em);
        if (deleted)
            new ShowErrorDialog(String.format("Movie {%s} deleted!", String.valueOf(movieId)), null);
        else 
            new ShowErrorDialog(String.format("Error occured while deleting Movie {%s}! Please, check the console for more information.", String.valueOf(movieId)), null);
        // [END]
        em.getTransaction().commit();
        // Close local temporary `javax.persistence.EntityManager`
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
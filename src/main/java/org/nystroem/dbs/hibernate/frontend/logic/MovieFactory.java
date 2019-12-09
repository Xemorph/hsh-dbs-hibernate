package org.nystroem.dbs.hibernate.frontend.logic;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.nystroem.dbs.hibernate.SharedModules;
import org.nystroem.dbs.hibernate.core.Logger;
import org.nystroem.dbs.hibernate.core.Utils;
import org.nystroem.dbs.hibernate.entities.Genre;
import org.nystroem.dbs.hibernate.entities.Movie;
import org.nystroem.dbs.hibernate.entities.MovieCharacter;
import org.nystroem.dbs.hibernate.entities.Person;
import org.nystroem.dbs.hibernate.entities.sub.CinemaMovie;
import org.nystroem.dbs.hibernate.entities.sub.Series;
import org.nystroem.dbs.hibernate.frontend.logic.dto.CharacterDTO;
import org.nystroem.dbs.hibernate.frontend.logic.dto.MovieDTO;
import org.nystroem.dbs.hibernate.store.Mutator;

public class MovieFactory {

    private static Logger LOGGER = new Logger(MovieFactory.class);

    public List<MovieDTO> getMovies(String search) {
        //Local temporary `javax.persistence.EntityManager`
        EntityManager tmpEM = ((EntityManagerFactory)SharedModules.core().store().handleCachedObject(Mutator.ENTITYMANAGERFACTORY, null)).createEntityManager();
        // Result list
        List<MovieDTO> rs = new CopyOnWriteArrayList<>();
        //Local temporary `FullTextEntityManager`
        FullTextEntityManager ftem = Search.getFullTextEntityManager(tmpEM);
        
        // Decision -> Search
        // [BEGIN] #Transaction
        //Transaktion um auf DB zu sortieren
        tmpEM.getTransaction().begin();
        
        //Sortquery wird erstellt und in Zeile 51 ausgeführt
        //Sortquery holt alle Movies aus der DB und sortiert sie nach der MovieId
        QueryBuilder qb = ftem.getSearchFactory()
                .buildQueryBuilder().forEntity(Movie.class).get();
        org.apache.lucene.search.Query query = qb
              .keyword()
              .wildcard()
              .onField(Movie.col_title)
              .matching(search.toLowerCase()+"*")
              .createQuery();
        org.apache.lucene.search.Sort sort = new Sort(SortField.FIELD_DOC, new SortField(Movie.col_movieID, SortField.Type.STRING, false)); // Descending order
        // wrap Lucene query in a javax.persistence.Query
        FullTextQuery persistenceQuery = ftem.createFullTextQuery(query, Movie.class);
        persistenceQuery.setSort(sort);
        // execute search
        List<Movie> movies = persistenceQuery.getResultList();
        for (Movie movie : movies) {
            MovieDTO movieDTOCopy = new MovieDTO();
            movieDTOCopy.setId(movie.getMovieID());
            movieDTOCopy.setTitle(movie.getTitle());
            movieDTOCopy.setType(movie.getType().toString());
            movieDTOCopy.setYear(movie.getYear());
            // [Missing] Characters
            // [Missing] Genres
            rs.add(movieDTOCopy);
        }
        
        // [END]
        tmpEM.getTransaction().commit();
        //Close local temporary `javax.persistence.EntityManager`
        tmpEM.close();
        // Return `List<MovieDTO> rs` as resultset
        return rs;
    }

    public MovieDTO getMovie(long id) {
        //Local temporary `javax.persistence.EntityManager`
        EntityManager tmpEM = ((EntityManagerFactory)SharedModules.core().store().handleCachedObject(Mutator.ENTITYMANAGERFACTORY, null)).createEntityManager();
        // [BEGIN] #Transaction
        //vergessen rauszunehmen
        tmpEM.getTransaction().begin();
        // Create an empty MovieDTO object
        MovieDTO dto = new MovieDTO();

        Query query = tmpEM.createQuery(
                "SELECT m FROM " + Movie.table + " m " + 
                "WHERE m." + Movie.col_movieID + " = :id").setParameter("id", Long.valueOf(id));
        // SingleResultSet -> Movie | No Initialization -> Direct convert
        Movie rs = (Movie) query.getSingleResult();
        if (rs instanceof CinemaMovie)
            if (((CinemaMovie)rs).getTicketsSold() != null)
                dto.setTicketsSold(((CinemaMovie)rs).getTicketsSold());
        if (rs instanceof Series)
            if (((Series)rs).getNumOfEpisodes() != null)
                dto.setNumOfEpisodes(((Series)rs).getNumOfEpisodes());
        // Map Movie to MovieDTO
        dto.setId(rs.getMovieID());
        dto.setTitle(rs.getTitle());
        dto.setType(rs.getType().toString());
        dto.setYear(rs.getYear());
        
        rs.getGenres().stream().map(n -> n.getGenre()).forEach(g -> dto.addGenre(g));
        // Map Set<MovieCharacter> to Set<CharacterDTO>
        for (MovieCharacter mChar : rs.getMovieCharacters()) {
            CharacterDTO cdto = new CharacterDTO();
            cdto.setCharId(mChar.getMovCharID());
            cdto.setCharacter(mChar.getCharacter());
            cdto.setAlias(mChar.getAlias());
            cdto.setPlayer(mChar.getPerson().getName());
            //Add `cdto` to `dto` MovieDTO object
            dto.addCharacter(cdto);
        }

        // [END]
        tmpEM.getTransaction().commit();
        //Close local temporary `javax.persistence.EntityManager`
        tmpEM.close();
        return dto;
    }

    public void insertUpdateMovie(MovieDTO dto) {
        //Local temporary `javax.persistence.EntityManager`
        EntityManager tmpEM = ((EntityManagerFactory)SharedModules.core().store().handleCachedObject(Mutator.ENTITYMANAGERFACTORY, null)).createEntityManager();
        // [BEGIN] #Transaction
        //vergessen rauszunehmen
        tmpEM.getTransaction().begin();
        // `Movie` template - gets filled if Movie exists in database
        Movie movie = new Movie();
        // Fetch `Movie` entity object if it exists
        if (dto.getId() != null)
            movie = tmpEM.find(Movie.class, Long.valueOf(dto.getId()));
        if (dto.getId() != null && (dto.getId() != movie.getMovieID())) {
            LOGGER.error(String.format("Movie {%s} should exists but we couldn't find this one in the database", String.valueOf(dto.getId())));
            LOGGER.error("Closing transaction and do a rollback!");
            // [END]
            tmpEM.getTransaction().rollback();
            //Close local temporary `javax.persistence.EntityManager`
            tmpEM.close();
            return;
        }
// ########################################################################################
// #--------------------------------------- UPDATE ---------------------------------------#
// ########################################################################################
        if (movie.getMovieID() > 0) {
            if (!movie.getTitle().equals(dto.getTitle()))
                movie.setTitle(dto.getTitle());
            if (movie.getYear() != dto.getYear())
                movie.setYear(dto.getYear());
            if (!movie.getType().equalsIgnoreCase(dto.getType()))
                movie.setType(dto.getType());
            // Check for Series & CinemaMovie
            if (movie instanceof CinemaMovie)
                if (((CinemaMovie)movie).getTicketsSold() != dto.getTicketsSold())
                    ((CinemaMovie)movie).setTicketsSold(dto.getTicketsSold());
            if (movie instanceof Series)
                if (((Series)movie).getNumOfEpisodes() != dto.getNumOfEpisodes())
                    ((Series)movie).setNumOfEpisodes(dto.getNumOfEpisodes());
            // Genres - We need to mark as dirty if there are any changes
            //          because Hibernate can't track List, Set or Array
            List<String> movGenre = movie.getGenres().stream().map(n -> n.getGenre()).collect(Collectors.toList());
            if (!Utils.listEqualsIgnoreOrder(movGenre, dto.getGenres().stream().collect(Collectors.toList()))) {
                // Lists aren't equals, know we need to figure out which elements need
                // to be removed or added
                final Set<Long> genres_of_dto = new HashSet<>();
                dto.getGenres()
                    .stream()
                    .forEach(g -> genres_of_dto.add(
                        (Long)tmpEM.createQuery("SELECT g." + Genre.col_genreID + " FROM " + Genre.table + " g " + 
                            "WHERE upper(g." + Genre.col_genre + ") = :name").setParameter("name", g.toUpperCase()).getSingleResult()
                    ));
                final Set<Long> genres_of_obj = new HashSet<>();
                movie.getGenres()
                    .stream()
                    .forEach(g -> genres_of_obj.add(
                        (Long)tmpEM.createQuery("SELECT g." + Genre.col_genreID + " FROM " + Genre.table + " g " + 
                            "WHERE upper(g." + Genre.col_genre + ") = :name").setParameter("name", g.getGenre().toUpperCase()).getSingleResult()
                    ));
                /* Calculate difference between Set1 & Set2 */
                Set<Long> add = new HashSet<>(genres_of_dto);
                add.removeAll(genres_of_obj);
                LOGGER.info("Genres to add : " + add);
                Set<Long> remove = new HashSet<>(genres_of_obj);
                remove.removeAll(genres_of_dto);
                LOGGER.info("Genres to remove : " + remove);
                // Add changes to our entity
                for (long id : remove) {
                    movie.removeGenre(tmpEM.find(Genre.class, Long.valueOf(id)));
                }
                for (long id : add) {
                    movie.addGenre(tmpEM.find(Genre.class, Long.valueOf(id)));
                }
            }
            // To update the MovieCharacters we are using a similar mechanism as we did at `Genre`
            // DELETE or INSERT MovieCharacter - No update functionality

            // Contains all MovieCharacters which needs to be added
            final Set<CharacterDTO> add =
                dto.getCharacters().stream().filter(x -> x.getCharId() <= 0).collect(Collectors.toSet());
            // Snapshot of `CharacterDTO` from MovieDTO object
            final Set<Long> dto_snapshot =
                dto.getCharacters().stream().map(z -> z.getCharId()).filter(x -> x > 0).collect(Collectors.toSet());
            // Snapshot of `MovieCharacter` from Movie object
            final Set<Long> obj_snapshot =
                movie.getMovieCharacters().stream().map(z -> z.getMovCharID()).collect(Collectors.toSet());
            
            // Add & remove MovieCharacters
            /* Calculate difference between Set1 & Set2 */
            Set<Long> remove = new HashSet<>(obj_snapshot);
            remove.removeAll(dto_snapshot);
            LOGGER.info("MovieCharacters to remove : " + remove);
            // Add changes to our entity
            for (long id : remove) {
                deleteById(MovieCharacter.class, Long.valueOf(id), tmpEM);
            }
            // Add changes to our entity
            Set<MovieCharacter> movChars = new HashSet<>();
            add.stream().forEach(t -> new Function<CharacterDTO,Void>() {
                @Override public Void apply(CharacterDTO t) {
                    MovieCharacter movChar = new MovieCharacter();
                    Person person = new Person();
                    movChar.setCharacter(t.getCharacter());
                    movChar.setAlias(t.getAlias());
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
                    movChars.add(movChar);
                    return null;
                }
            }.apply(t));
            for (MovieCharacter movChar : movChars) {
                movChar.setMovie(movie);
                tmpEM.persist(movChar);
            }
            // Update MovieCharacters
            Set<Long> possible_update = new HashSet<>(dto_snapshot);
            possible_update.removeAll(remove);
            Set<MovieCharacter> update = new HashSet<>();
            possible_update.stream().forEach(id -> {
                MovieCharacter movChar = tmpEM.find(MovieCharacter.class, Long.valueOf(id));
                CharacterDTO cdto =
                    dto.getCharacters().stream().filter(x -> x.getCharId() == id).collect(Collectors.toList()).get(0);
                if (!movChar.getCharacter().equals(cdto.getCharacter()))
                    movChar.setCharacter(cdto.getCharacter());
                if (!movChar.getAlias().equals(cdto.getAlias()))
                    movChar.setAlias(cdto.getAlias());
                if (!movChar.getPerson().getName().equalsIgnoreCase(cdto.getPlayer())) {
                    Person person = new Person();
                    try {
                        person = (Person) tmpEM.createQuery("SELECT p FROM " + Person.table + " p " +
                                                                    "WHERE upper(p." + Person.col_name + ") = :name")
                                                .setParameter("name", cdto.getPlayer().toUpperCase())
                                                .getSingleResult();
                        movChar.setPerson(person);
                    } catch (NoResultException ex) {
                        person.setName(cdto.getPlayer());
                        tmpEM.persist(person);
                        movChar.setPerson(person);
                    }
                }
                update.add(movChar);
            });

            for (MovieCharacter movChar : update) {
                movChar.setMovie(movie);
                tmpEM.persist(movChar);
            }

            // [END]
            tmpEM.getTransaction().commit();
            //Close local temporary `javax.persistence.EntityManager`
            tmpEM.close();
            return; // Update completed
        }

// ########################################################################################
// #--------------------------------------- INSERT ---------------------------------------#
// ########################################################################################
        if (dto.getTicketsSold() != null)
            movie = new CinemaMovie();
        if (dto.getNumOfEpisodes() != null)
            movie = new Series();
        movie.setTitle(dto.getTitle());
        movie.setType(dto.getType());
        movie.setYear(dto.getYear());

        if (movie instanceof CinemaMovie) {
            LOGGER.debug("Tickets: " + dto.getTicketsSold());
            ((CinemaMovie)movie).setTicketsSold(dto.getTicketsSold());
        }
        if (movie instanceof Series) {
            LOGGER.debug("Episodes: " + dto.getNumOfEpisodes());
            ((Series)movie).setNumOfEpisodes(dto.getNumOfEpisodes());
        }

        final Set<Genre> genres = new HashSet<>();
        dto.getGenres()
            .stream()
            .forEach(g -> genres.add(
                (Genre)tmpEM.createQuery("SELECT g FROM " + Genre.table + " g " + 
                    "WHERE upper(g." + Genre.col_genre + ") = :name").setParameter("name", g.toUpperCase()).getSingleResult()
            ));

        // Persist our Movie entity `mov`
        tmpEM.persist(movie);

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
            movChar.setMovie(movie);
            tmpEM.persist(movChar);
        }

        // [END]
        tmpEM.getTransaction().commit();
        //Close local temporary `javax.persistence.EntityManager`
        tmpEM.close();
    }

    public void deleteMovie(long movieId) {
        //Local temporary `javax.persistence.EntityManager`
        EntityManager tmpEM = ((EntityManagerFactory)SharedModules.core().store().handleCachedObject(Mutator.ENTITYMANAGERFACTORY, null)).createEntityManager();
        // [BEGIN] #Transaction
        //vergessen rauszunehmen
        tmpEM.getTransaction().begin();
        
        boolean deleted = deleteById(Movie.class, Long.valueOf(movieId), tmpEM);

        // [END]
        tmpEM.getTransaction().commit();
        //Close local temporary `javax.persistence.EntityManager`
        tmpEM.close();
    }

    private boolean deleteById(Class<?> type, Serializable id, EntityManager em) {
        Object persistentInstance = em.find(type, id);
        if (persistentInstance != null) {
            em.remove(persistentInstance);
            return true;
        }
        return false;
    }
}
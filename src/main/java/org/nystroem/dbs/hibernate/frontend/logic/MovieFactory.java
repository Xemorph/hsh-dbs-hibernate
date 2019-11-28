package org.nystroem.dbs.hibernate.frontend.logic;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.nystroem.dbs.hibernate.SharedModules;
import org.nystroem.dbs.hibernate.entities.Movie;
import org.nystroem.dbs.hibernate.frontend.logic.dto.MovieDTO;
import org.nystroem.dbs.hibernate.store.Mutator;

public class MovieFactory {

    public List<MovieDTO> getMovies(String search) {
        //Local temporary `javax.persistence.EntityManager`
        EntityManager tmpEM = ((EntityManagerFactory)SharedModules.core().store().handleCachedObject(Mutator.ENTITYMANAGERFACTORY, null)).createEntityManager();
        // Result list
        List<MovieDTO> rs = new CopyOnWriteArrayList<>();
        //Local temporary `FullTextEntityManager`
        FullTextEntityManager ftem = Search.getFullTextEntityManager(tmpEM);
        
        // Decision -> Search
        // [BEGIN] #Transaction
        tmpEM.getTransaction().begin();
        
        QueryBuilder qb = ftem.getSearchFactory()
                .buildQueryBuilder().forEntity(Movie.class).get();
        org.apache.lucene.search.Query query = qb
              .keyword()
              .wildcard()
              .onFields(Movie.col_title)
              .matching("*"+search+"*")
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

}
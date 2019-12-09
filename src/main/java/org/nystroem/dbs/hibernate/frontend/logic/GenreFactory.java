package org.nystroem.dbs.hibernate.frontend.logic;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.nystroem.dbs.hibernate.SharedModules;
import org.nystroem.dbs.hibernate.entities.Genre;
import org.nystroem.dbs.hibernate.store.Mutator;

public class GenreFactory {

    public List<String> getGenres() {
        List<String> rs = new ArrayList<>();
        
        //Local temporary `javax.persistence.EntityManager`
        EntityManager tmpEM = ((EntityManagerFactory)SharedModules.core().store().handleCachedObject(Mutator.ENTITYMANAGERFACTORY, null)).createEntityManager();
        
        TypedQuery<Genre> q = tmpEM.createQuery("SELECT g FROM Genre g", Genre.class);
        List<Genre> genres = q.getResultList();
        // Reduziere das Genre Objekt auf ein String Objekt; Ergebnis eine Liste aus Strings (Genrename)
        genres.stream().map(n -> n.getGenre()).forEach(n -> rs.add(n));
        
        //Close local temporary `javax.persistence.EntityManager`
        tmpEM.close();
        //Return `List<String>` object
        return rs;
    }

}
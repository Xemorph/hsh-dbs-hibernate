package org.nystroem.dbs.hibernate.frontend.logic;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.nystroem.dbs.hibernate.SharedModules;
import org.nystroem.dbs.hibernate.entities.Genre;
import org.nystroem.dbs.hibernate.store.Mutator;

public class GenreFactory {

    public List<String> getGenres() {
        List<String> rs = new CopyOnWriteArrayList<>();
        
        //Local temporary `javax.persistence.EntityManager`
        EntityManager tmpEM = ((EntityManagerFactory)SharedModules.core().store().handleCachedObject(Mutator.ENTITYMANAGERFACTORY, null)).createEntityManager();
        
        Query q = tmpEM.createQuery("SELECT g FROM Genre g");
        List<?> resultset = q.getResultList();
        if (!resultset.isEmpty() && resultset.get(0) instanceof Genre) {
            List<Genre> genres = (List<Genre>) resultset;
            //reduziere das Genre Objekt auf ein String Objekt; Ergebnis eine Liste aus Strings(Genrename)
            genres.stream().map(n -> n.getGenre()).forEach(rs::add);
        }
        
        //Close local temporary `javax.persistence.EntityManager`
        tmpEM.close();
        //Return `List<String>` object
        return rs;
    }

}
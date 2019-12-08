package org.nystroem.dbs.hibernate.frontend.logic;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.nystroem.dbs.hibernate.SharedModules;
import org.nystroem.dbs.hibernate.entities.Person;
import org.nystroem.dbs.hibernate.store.Mutator;

public class PersonFactory {

    public List<String> getPersonList(String text) {
        //Local temporary `javax.persistence.EntityManager`
        EntityManager tmpEM = ((EntityManagerFactory)SharedModules.core().store().handleCachedObject(Mutator.ENTITYMANAGERFACTORY, null)).createEntityManager();
        //Local temporary `FullTextEntityManager`
        FullTextEntityManager ftem = Search.getFullTextEntityManager(tmpEM);
        
        // Decision -> Search
        // [BEGIN] #Transaction
        //Transaktion um auf DB zu sortieren
        tmpEM.getTransaction().begin();
        
        //Sortquery wird erstellt und in Zeile 51 ausgeführt
        //Sortquery holt alle Movies aus der DB und sortiert sie nach der MovieId
        QueryBuilder qb = ftem.getSearchFactory()
                .buildQueryBuilder().forEntity(Person.class).get();
        org.apache.lucene.search.Query query = qb
              .keyword()
              .wildcard()
              .onFields(Person.col_name)
              .matching("*"+text+"*")
              .createQuery();
        // wrap Lucene query in a javax.persistence.Query
        FullTextQuery persistenceQuery = ftem.createFullTextQuery(query, Person.class);
        // execute search
        List<String> persons = new ArrayList<>();

        ((List<Person>)persistenceQuery.getResultList()).stream().map(p -> p.getName()).forEach(n -> persons.add(n));

        return persons;
    }

}
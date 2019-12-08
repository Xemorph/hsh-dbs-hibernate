/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Benny Nystroem. All rights reserved.
 *  Licensed under the GNU GENERAL PUBLIC LICENSE v3 License. 
 *  See LICENSE in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
package org.nystroem.dbs.hibernate.entities;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.snowball.SnowballPorterFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Parameter;
import org.hibernate.search.annotations.Store;

@Entity
@Table(name="Person")
@AnalyzerDef(name="nameanalyzer", tokenizer=@org.hibernate.search.annotations.TokenizerDef(factory=StandardTokenizerFactory.class), filters = {
    @org.hibernate.search.annotations.TokenFilterDef(factory=LowerCaseFilterFactory.class),
    @org.hibernate.search.annotations.TokenFilterDef(factory=SnowballPorterFilterFactory.class, params = { @Parameter(name="language", value="English") }) })
@Indexed
public class Person {
    /** Konstanten */
    public static final String seq_personID = "person_id";
    public static final String table = "Person";
    public static final String col_personID = "PersonID";
    public static final String col_name = "Name";
    public static final String col_sex = "Sex";

    @Id
    @Column(name=col_personID)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator=Person.seq_personID)
    @SequenceGenerator(name=Person.seq_personID, sequenceName=Person.seq_personID, allocationSize=1, initialValue=1)
    private Long PersonID;
    @Column(name=col_name, nullable=false, unique=true)
    @Field(index=Index.YES, store=Store.YES)
    @Analyzer(definition="nameanalyzer")
    private String Name;
    @Column(name=col_sex, nullable=false)
    private String Sex = "M";

    @OneToMany(mappedBy="person")
    Set<MovieCharacter> plays = new HashSet<MovieCharacter>();

    /** Default Constructor */
    public Person() { /** Nothing here! */ }

    public long getPersonID() {
        return this.PersonID;
    }

    public String getName() {
        return this.Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public void setSex(String sex) {
        this.Sex = sex;
    }

    public String getSex() {
        return this.Sex;
    }

    public Set<MovieCharacter> getMovieCharacters() {
        return this.plays;
    }
}

/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Benny Nystroem. All rights reserved.
 *  Licensed under the GNU GENERAL PUBLIC LICENSE v3 License. 
 *  See LICENSE in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
package org.nystroem.dbs.hibernate.entities;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.snowball.SnowballPorterFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Parameter;
import org.hibernate.search.annotations.SortableField;
import org.hibernate.search.annotations.Store;

@Entity
@Table(name="Movie")
//Performance sparen
@AnalyzerDef(name="titleanalyzer", tokenizer=@org.hibernate.search.annotations.TokenizerDef(factory=StandardTokenizerFactory.class), filters = {
    @org.hibernate.search.annotations.TokenFilterDef(factory=LowerCaseFilterFactory.class),
    @org.hibernate.search.annotations.TokenFilterDef(factory=SnowballPorterFilterFactory.class, params = { @Parameter(name="language", value="English") }) })
@Indexed
public class Movie {
    /** Konstanten */
    public static final String seq_movieID = "movie_id";
    public static final String table = "Movie";
    public static final String col_movieID = "MovieID";
    public static final String col_type = "Type";
    public static final String col_title = "Title";
    public static final String col_year = "Year";

    @Id
    @Column(name=col_movieID)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator=Movie.seq_movieID)
    @SequenceGenerator(name=Movie.seq_movieID, sequenceName=Movie.seq_movieID, allocationSize=1, initialValue=1)
    @SortableField
    private Long MovieID;
    @Column(name=col_title, nullable=false)
    @Field(index=Index.YES, store=Store.NO)
    @Analyzer(definition="titleanalyzer")
    private String Title;
    @Column(name=col_type, nullable=false)
    private String Type;
    @Column(name=col_year, nullable=false)
    private Integer Year;

    @OneToMany(mappedBy="movie", orphanRemoval=true)
    private Set<MovieCharacter> movieCharacters = new HashSet<MovieCharacter>();

    @ManyToMany(cascade = { 
        CascadeType.PERSIST,
        CascadeType.MERGE
    })
    @JoinTable(
        name=MovieGenre.table,
        joinColumns = { @JoinColumn(name=MovieGenre.col_movieID) },
        inverseJoinColumns = { @JoinColumn(name=MovieGenre.col_genreID) }
    )
    private Set<Genre> genres = new HashSet<Genre>();

    /** Default Constructor */
    public Movie() { /** Nohting here! */}

    public long getMovieID() {
        return this.MovieID;
    }

    public String getTitle() {
        return this.Title;
    }

    public void setTitle(String title) {
        this.Title = title;
    }

    public String getType() {
        return this.Type;
    }

    public void setType(String type) {
        this.Type = type;
    }

    public int getYear() {
        return this.Year;
    }

    public void setYear(int year) {
        this.Year = year;
    }

    public Set<Genre> getGenres() {
        return this.genres;
    }

    public void addGenre(Genre genre) {
        this.genres.add(genre);
    }

    public Set<MovieCharacter> getMovieCharacters() {
        return this.movieCharacters;
    }
}

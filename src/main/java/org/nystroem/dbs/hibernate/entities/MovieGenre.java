package org.nystroem.dbs.hibernate.entities;

import java.sql.SQLException;

import javax.persistence.Column;

public class MovieGenre {
    /** Konstanten */
    public static final String table = "hasGenre";
    public static final String col_genreID = "GenreID";
    public static final String col_movieID = "MovieID";

    @Column(name=col_genreID, nullable=false)
    public Long genreID;
    @Column(name=col_movieID, nullable=false)
    public Long movieID;

    /** Default Constructor */
    public MovieGenre() { /** Nothing here! */ }

    public MovieGenre(long genreID, long movieID) {
        this.genreID = genreID;
        this.movieID = movieID;
    }

    public void setGenreID(long genreID) {
        this.genreID = genreID;
    }

    public void setMovieID(long movieID) {
        this.movieID = movieID;
    }

    public Long getGenreID() {
        return this.genreID;
    }

    public Long getMovieID() {
        return this.movieID;
    }

    public void insert() throws SQLException { }

    public void update() throws SQLException { }

    public void delete() throws SQLException { }

}
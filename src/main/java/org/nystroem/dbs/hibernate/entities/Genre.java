/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Benny Nystroem. All rights reserved.
 *  Licensed under the GNU GENERAL PUBLIC LICENSE v3 License. 
 *  See LICENSE in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
package org.nystroem.dbs.hibernate.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="Genre")
public class Genre {
    /** Konstanten */
    public static final String seq_genreID = "genre_id";
    public static final String table = "Genre";
    public static final String col_genre = "Genre";
    public static final String col_genreID = "GenreID";

    @Id
    @Column(name=col_genreID)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator=org.nystroem.dbs.hibernate.entities.Genre.seq_genreID)
    @SequenceGenerator(name=org.nystroem.dbs.hibernate.entities.Genre.seq_genreID, sequenceName=org.nystroem.dbs.hibernate.entities.Genre.seq_genreID, allocationSize=1, initialValue=1)
    private Long GenreID;
    @Column(name=col_genre, nullable=false)
    private String Genre;

    /** Default Constructor  */
    public Genre() { /** Nothing here! */ }

    public String getGenre() {
        return this.Genre;
    }

    public long getGenreID() {
        return this.GenreID;
    }
}
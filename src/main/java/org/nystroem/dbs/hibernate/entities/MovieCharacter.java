package org.nystroem.dbs.hibernate.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="MovieCharacter")
public class MovieCharacter {
    /** Konstante */
    public static final String seq_movcharID = "movchar_id";
    public static final String table = "MovieCharacter";
    public static final String col_char = "Character";
    public static final String col_alias = "Alias";
    public static final String col_movCharID = "MovCharID";
    public static final String col_pos = "Position";
    public static final String col_movID = "hasCharacter";
    public static final String col_persID = "plays";

    @Id
    @Column(name=col_movCharID)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator=MovieCharacter.seq_movcharID)
    @SequenceGenerator(name=MovieCharacter.seq_movcharID, sequenceName=MovieCharacter.seq_movcharID, allocationSize=1, initialValue=1)
    private Long MovCharID;
    @Column(name=col_char, nullable=false)
    private String Character;
    @Column(name=col_alias, nullable=false)
    private String Alias;
    @Column(name=col_pos, nullable=false)
    private Integer Position = 1;
    // Foreign keys
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name=MovieCharacter.col_persID, nullable=false)
    private Person person;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name=MovieCharacter.col_movID, nullable=false)
    private Movie movie;

    /** Default Constructor */
    public MovieCharacter() { /** Nothing here!  */ }

    public long getMovCharID() {
        return this.MovCharID;
    }

    public void setMovCharID(Long MovCharID) {
        this.MovCharID = MovCharID;
    }

    public String getCharacter() {
        return this.Character;
    }

    public void setCharacter(String character) {
        this.Character = character;
    }

    public String getAlias() {
        return this.Alias;
    }

    public void setAlias(String alias) {
        this.Alias = alias;
    }

    public int getPosition() {
        return this.Position;
    }

    public void setPosition(int pos) {
        this.Position = pos;
    }

    public Movie getMovie() {
        return this.movie;
    }

    public void setMovie(Movie mov) {
        this.movie = mov;
        // [Warning] This may cause performance issues if you have a large data set since this operation is O(n)
        if (!mov.getMovieCharacters().contains(this))
            mov.getMovieCharacters().add(this);
    }

    public Person getPerson() {
        return this.person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}
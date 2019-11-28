package org.nystroem.dbs.hibernate.entities;

import java.sql.SQLException;
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
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="MovieCharacter")
public class MovieCharacter {
    /** Konstante */
    // public static final String seq_movCharID = "movchar_id";
    // public static final String table = "MovieCharacter";
    protected static final String col_char = "Character";
    protected static final String col_alias = "Alias";
    protected static final String col_movCharID = "MovCharID";
    protected static final String col_pos = "Position";
    protected static final String col_movID = "hasCharacter";
    protected static final String col_persID = "plays";

    @Id @Column(name=col_movCharID) @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private Long MovCharID;
    @Column(name=col_char, nullable=false)
    private String Character;
    @Column(name=col_alias, nullable=false)
    private String Alias;
    @Column(name=col_pos, nullable=false)
    private Integer Position = 1;
    // Foreign keys
    @ManyToOne
    @JoinColumn(name=Person.col_personID, nullable=false)
    private Person person;
    @ManyToOne
    @JoinColumn(name=Movie.col_movieID, nullable=false)
    Movie movie;

    /** Default Constructor */
    public MovieCharacter() { /** Nothing here!  */ }

    public long getMovCharID() {
        return this.MovCharID;
    }

    public String getCharacter() {
        return this.Character;
    }

    public String getAlias() {
        return this.Alias;
    }

    public int getPosition() {
        return this.Position;
    }

    public Movie getMovie() {
        return this.movie;
    }

    public Person getPerson() {
        return this.person;
    }


    public void insert() throws SQLException { }

    public void update() throws SQLException { }

    public void delete() throws SQLException { }
}
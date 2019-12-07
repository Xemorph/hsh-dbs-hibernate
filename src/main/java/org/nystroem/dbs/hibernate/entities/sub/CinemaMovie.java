package org.nystroem.dbs.hibernate.entities.sub;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.search.annotations.Indexed;

@Entity
@Table(name=CinemaMovie.table)
@Indexed
public class CinemaMovie 
    extends org.nystroem.dbs.hibernate.entities.Movie 
{
    /** Konstanten */
    protected static final String table = "CinemaMovie";
    protected static final String col_tickets = "TicketsSold";

    @Column(name=CinemaMovie.col_tickets)
    private Integer TicketsSold;

    public CinemaMovie() { /** Nothing is here! */}

    public Integer getTicketsSold() {
        return this.TicketsSold;
    }

    public void setTicketsSold(int tickets) {
        this.TicketsSold = tickets;
    }

}
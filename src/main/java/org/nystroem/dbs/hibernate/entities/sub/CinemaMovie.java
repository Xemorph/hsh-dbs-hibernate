package org.nystroem.dbs.hibernate.entities.sub;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name=CinemaMovie.table)
public class CinemaMovie 
    extends org.nystroem.dbs.hibernate.entities.Movie 
{
    /** Konstanten */
    protected static final String table = "CinemaMovie";
    protected static final String col_tickets = "TicketsSold";

    @Column(name=CinemaMovie.col_tickets)
    private int TicketsSold;

    public CinemaMovie() { /** Nothing is here! */}

    public int getTicketsSold() {
        return this.TicketsSold;
    }

    public void setTicketsSold(int tickets) {
        this.TicketsSold = tickets;
    }

}
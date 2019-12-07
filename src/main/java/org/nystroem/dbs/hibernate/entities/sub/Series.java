package org.nystroem.dbs.hibernate.entities.sub;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name=Series.table)
public class Series 
    extends org.nystroem.dbs.hibernate.entities.Movie 
{
    /** Konstanten */
    protected static final String table = "Series";
    protected static final String col_numsofepisodes = "NumOfEpisodes";

    @Column(name=Series.col_numsofepisodes)
    private int NumOfEpisodes;

}
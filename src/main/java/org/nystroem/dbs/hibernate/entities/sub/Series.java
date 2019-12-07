package org.nystroem.dbs.hibernate.entities.sub;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.search.annotations.Indexed;

@Entity
@Table(name=Series.table)
@Indexed
public class Series 
    extends org.nystroem.dbs.hibernate.entities.Movie 
{
    /** Konstanten */
    protected static final String table = "Series";
    protected static final String col_numsofepisodes = "NumOfEpisodes";

    @Column(name=Series.col_numsofepisodes)
    private Integer NumOfEpisodes;

    public Integer getNumOfEpisodes() {
        return this.NumOfEpisodes;
    }

    public void setNumOfEpisodes(int num) {
        this.NumOfEpisodes = num;
    }

}
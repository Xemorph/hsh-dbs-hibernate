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
import javax.persistence.Table;

@Entity
@Table(name="Person")
public class Person {
    /** Konstanten */
    // public static final String seq_personid = "person_id";
    // private static final String table = "Person";
    protected static final String col_personID = "PersonID";
    protected static final String col_name = "Name";
    protected static final String col_sex = "Sex";

    @Id @Column(name=col_personID) @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private Long PersonID;
    @Column(name=col_name, nullable=false)
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
}

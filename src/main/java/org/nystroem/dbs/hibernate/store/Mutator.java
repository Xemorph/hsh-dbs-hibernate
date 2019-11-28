/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Benny Nystroem. All rights reserved.
 *  Licensed under the GNU GENERAL PUBLIC LICENSE v3 License. 
 *  See LICENSE in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
package org.nystroem.dbs.hibernate.store;

public enum Mutator {

    ENTITYMANAGERFACTORY("EntityManagerFactory");

    private String fancyname;

    Mutator(String fancyname) {
        this.fancyname = fancyname;
    }

    public String fancyname() {
        return this.fancyname;
    }

    @Override public String toString() {
        return String.format("[%s] ", fancyname);
    }

}
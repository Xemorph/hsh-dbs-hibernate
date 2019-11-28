/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Benny Nystroem. All rights reserved.
 *  Licensed under the GNU GENERAL PUBLIC LICENSE v3 License. 
 *  See LICENSE in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
package org.nystroem.dbs.hibernate.store;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Store {
    /** --------------------------------------------------------------------------------------------------------------------- */
    //  Private variables
    /** --------------------------------------------------------------------------------------------------------------------- */
    // Max internal cache size
    private final int MAX_ENTRIES = 100;
    // Cache mechanic & cache holder / store
    private Map<Object, Object> cache = new LinkedHashMap<Object, Object>(MAX_ENTRIES+1, .75f, true) {
        // This method is called just after a new entry has been added
        // `removeEldestEntry` checks if the eldets entry should be deleted, measured in size
        public boolean removeEldestEntry(Map.Entry<Object, Object> eldest) {
            return size() > MAX_ENTRIES;
        }
    };
    // ResultSet, similar to the ResultSet of JDBC
    // Difference: Our contains Java objects because of the usage of `Hibernate`
    private List<Object> resultset = new ArrayList<Object>();

    /** --------------------------------------------------------------------------------------------------------------------- */
    //  Private methods
    /** --------------------------------------------------------------------------------------------------------------------- */
    // Get object from cache, internal use only
    private Object recieve(Object obj) {
        this.resultset.clear(); // Reset the internal $resultset for every fetch
        this.cache.keySet()
                .stream()
                .filter(e -> Objects.equals(this.cache.get(e), obj))
                .peek(this.resultset::add);
        return (this.resultset.size() > 0 ? this.resultset : new ArrayList<Object>());
    }
    // Write an object into the cache, internal use only
    private Object write(Object key, Object obj) {
        this.cache.put(key, obj);
        return obj;
    }

    /** --------------------------------------------------------------------------------------------------------------------- */
    //  Public methods
    /** --------------------------------------------------------------------------------------------------------------------- */
    // Method to recieve Objects from cache and or
    // to save objects into the internal cache system
    public Object handleCachedObject(Object key, Object obj) {
        if (key != null && this.cache.containsKey(key))
            return this.cache.get(key);
        // Check if $obj is available in the cache
        if (Objects.equals(obj, recieve(obj)))
            return recieve(obj); // Our object is available in the cache, return it!
        // Every single state was false, save the $obj in the cache
        return write(key, obj);
    }

}
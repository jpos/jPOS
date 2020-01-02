/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.space;

import java.util.Set;

/**
 * @author Kris, Bharavi, Alejandro
 * @version $Revision$ $Date$
 */
public interface LocalSpace<K,V> extends Space<K,V> {
    /**
     * add a SpaceListener associated with a given key
     * @param key Entry's key
     * @param listener a SpaceListener
     */
    void addListener(K key, SpaceListener<K, V> listener);

    /**
     * add a SpaceListener associated with a given key for a given 
     * period of time.
     * <b>Warning: not supported by all space implementations.</b>
     * @param key Entry's key
     * @param listener a SpaceListener
     * @param timeout listener registration timeout in millis
     */
    void addListener(K key, SpaceListener<K, V> listener, long timeout);

    /**
     * removes a SpaceListener associated with a given key
     * @param key Entry's key
     * @param listener the SpaceListener
     */
    void removeListener(K key, SpaceListener<K, V> listener);

    /**
     * @return Set containing all keys in Space
     */
    Set<K> getKeySet();

    /**
     * @return number of entries in a given key
     */
    int size(K key);
}

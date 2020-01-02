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

/**
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 * @since 2.0
 * @see Space
 */
public interface SpaceListener<K,V> {
    /**
     * <p>Called by Space implementation whenever an object 
     * with the given key is being placed in the Space.</p>
     *
     * @param key   Object's key
     * @param value Object's value
     */
    void notify(K key, V value);
}

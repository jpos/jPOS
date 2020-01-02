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
 * <p><b>Space</b> uses concepts described in the Linda Coordination Language 
 * that eases the implementation of other jPOS components (such as 
 * Channels, Muxes, etc.), but it is not by any means an attempt to provide
 * a full implementation.</p>
 *
 * <p>jPOS's Space is basically a Map where each entry is a LinkedList 
 * of values that can be used as a BlockingQueue</p>
 *
 * <p>One can place entries on a queue by calling Space.out, take them
 * by calling Space.in and read (without taking) by calling Space.rd</p>
 *
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 * @since 2.0
 * @see TSpace
 * @see SpaceError
 * @see <a href="http://www.cs.yale.edu/Linda/linda-lang.html">The Linda Coordination Language</a>
 */

public interface Space<K,V> {
    /**
     * Write a new entry into the Space
     * @param key Entry's key
     * @param value Object value
     */
    void out(K key, V value);

    /**
     * Write a new entry into the Space, with an timeout value
     * @param key Entry's key
     * @param value Object value
     * @param timeout timeout value in millis
     */
    void out(K key, V value, long timeout);

    /**
     * Take an entry from the space, waiting forever until one exists.
     * @param key Entry's key
     * @return value
     */
    V in(K key);

    /**
     * Read an entry from the space, waiting forever until one exists.
     * @param key Entry's key
     * @return value
     */
    V rd(K key);

    /**
     * Take an entry from the space, waiting a limited amount of time
     * until one exists.
     * @param key Entry's key
     * @param timeout millis to wait
     * @return value or null
     */
    V in(K key, long timeout);


    /**
     * Read an entry from the space, waiting a limited amount of time
     * until one exists.
     * @param key Entry's key
     * @param timeout millis to wait
     * @return value or null
     */
    V rd(K key, long timeout);


    /**
     * In probe takes an entry from the space if one exists, 
     * return null otherwise.
     * @param key Entry's key
     * @return value or null
     */
    V inp(K key);


    /**
     * Read probe reads an entry from the space if one exists, 
     * return null otherwise.
     * @param key Entry's key
     * @return value or null
     */
    V rdp(K key);

    /**
     * Nrd (not read) waits forever until Key is not present in space
     * Resolution for expiring entries is implementation dependant, but a minimum one-second is suggested.
     * @param key Entry's key
     */
    void nrd(K key);

    /**
     * Nrd (not read) waits up to timeout until Key is not present in space
     * Resolution for expiring entries is implementation dependant, but a minimum one-second is suggested.
     * @param key Entry's key
     * @param timeout millis to wait
     * @return value or null
     */
    V nrd(K key, long timeout);

    /**
     * Write a new entry at the head of a queue.
     * @param key Entry's key
     * @param value Object value
     */
    void push(K key, V value);

    /**
     * Write a new entry at the head of the queue with a timeout value
     * @param key Entry's key
     * @param value Object value
     * @param timeout timeout value in millis
     */
    void push(K key, V value, long timeout);

    /**
     * @param keys array of keys to check
     * @return true if one or more keys are available in the space
     */
    boolean existAny(K[] keys);

    /**
     * @param keys array of keys to check
     * @param timeout to wait for any of the entries to become available in millis
     * @return true if one or more keys are available in the space
     */
    boolean existAny(K[] keys, long timeout);

    /**
     * Write a single entry at the head of the queue discarding the other entries
     * @param key Entry's key
     * @param value Object value

     */
    void put(K key, V value);

    /**
     * Write a single entry at the head of the queue discarding the other entries, with timeout.
     * @param key Entry's key
     * @param value Object value
     * @param timeout timeout value in millis
     */
    void put(K key, V value, long timeout);
}

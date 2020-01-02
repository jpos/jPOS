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
 * Intercepts space operations.
 *
 * @author Alejandro Revilla
 * @since  1.4.7
 */
public class SpaceInterceptor<K,V> implements Space<K,V> {
    protected Space<K,V> sp;
    public SpaceInterceptor (Space<K,V> sp) {
        super();
        this.sp = sp;
    }
    public void out (K key, V value) {
        sp.out (key, value);
    }
    public void out (K key, V value, long timeout) {
        sp.out (key, value, timeout);
    }
    public void push (K key, V value) {
        sp.push (key, value);
    }
    public void push (K key, V value, long timeout) {
        sp.push (key, value, timeout);
    }
    public void put (K key, V value) {
        sp.put (key, value);
    }
    public void put (K key, V value, long timeout) {
        sp.put (key, value, timeout);
    }    
    public V in  (K key) {
        return sp.in (key);
    }
    public V rd  (K key) {
        return sp.rd (key);
    }
    public V in  (K key, long timeout) {
        return sp.in (key, timeout);
    }
    public V rd  (K key, long timeout) {
        return sp.rd (key, timeout);
    }
    public V inp (K key) {
        return sp.inp (key);
    }
    public V rdp (K key) {
        return sp.rdp (key);
    }
    @Override
    public void nrd(K key) {
        sp.nrd(key);
    }
    @Override
    public V nrd(K key, long timeout) {
        return sp.nrd(key, timeout);
    }

    public boolean existAny (K[] keys) {
        return sp.existAny (keys);
    }
    public boolean existAny (K[] keys, long timeout) {
        return sp.existAny (keys, timeout);
    }
}

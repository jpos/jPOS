/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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
public class SpaceInterceptor implements Space {
    protected Space sp;
    public SpaceInterceptor (Space sp) {
        super();
        this.sp = sp;
    }
    public void out (Object key, Object value) {
        sp.out (key, value);
    }
    public void out (Object key, Object value, long timeout) {
        sp.out (key, value, timeout);
    }
    public void push (Object key, Object value) {
        sp.push (key, value);
    }
    public void push (Object key, Object value, long timeout) {
        sp.push (key, value, timeout);
    }
    public void put (Object key, Object value) {
        sp.put (key, value);
    }
    public void put (Object key, Object value, long timeout) {
        sp.put (key, value, timeout);
    }    
    public Object in  (Object key) {
        return sp.in (key);
    }
    public Object rd  (Object key) {
        return sp.rd (key);
    }
    public Object in  (Object key, long timeout) {
        return sp.in (key, timeout);
    }
    public Object rd  (Object key, long timeout) {
        return sp.rd (key, timeout);
    }
    public Object inp (Object key) {
        return sp.inp (key);
    }
    public Object rdp (Object key) {
        return sp.rdp (key);
    }
    public boolean existAny (Object[] keys) {
        return sp.existAny (keys);
    }
    public boolean existAny (Object[] keys, long timeout) {
        return sp.existAny (keys, timeout);
    }
}


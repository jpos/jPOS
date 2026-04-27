/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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
 * Reference-equality {@link Template} that matches space entries equal to a
 * captured value under the configured key.
 */
public class ObjectTemplate implements Template {

    Object key;
    Object value;

    /**
     * Constructs a template matching {@code value} under {@code key}.
     *
     * @param key entry key
     * @param value reference value compared via {@code equals}
     */
    public ObjectTemplate(Object key, Object value) {
        super();
        this.key = key;
        this.value = value;
    }

    public Object getKey() {
        return key;
    }

    @Override
    public boolean equals(Object obj) {
        return value.equals(obj);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return "ObjectTemplate [key=" + key + ", value=" + value + "]";
    }
}

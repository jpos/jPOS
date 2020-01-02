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

package org.jpos.rc;

import java.util.Objects;

/**
 * Result Code implementation
 */
public class SimpleRC implements RC {
    private String rc;
    private String display;

    private SimpleRC() {

    }

    public SimpleRC(String rc) {
        this.rc = rc;
        if (rc == null)
            throw new NullPointerException ();
    }

    public SimpleRC (String rc, String display) {
        this(rc);
        this.display = display;
    }
    public String rc() {
        return rc;
    }
    public String display() {
        return display;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleRC simpleRC = (SimpleRC) o;
        return Objects.equals(rc, simpleRC.rc) &&
          Objects.equals(display, simpleRC.display);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rc, display);
    }

    @Override
    public String toString() {
        return "SimpleRC{" +
          "rc='" + rc + '\'' +
          ", display='" + display + '\'' +
          '}';
    }
}

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

package org.jpos.util;

/**
 * @author Alejandro P. Revilla
 * @version $Revision$ $Date$
 */
public class Asserter {
    public Asserter() {
        super();
    }
    public Asserter check (Object obj) throws AssertFailedException {
        if (obj == null)
            throw new AssertFailedException();
        return this;
    }
    public Asserter check (Object obj1, Object obj2) 
        throws AssertFailedException
    {
        check(obj1).check(obj2);
        if (!obj1.equals(obj2))
            throw new AssertFailedException();
        return this;
    }
    public Asserter check (boolean b) throws AssertFailedException {
        if (!b)
            throw new AssertFailedException();
        return this;
    }
}


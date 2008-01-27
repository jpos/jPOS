/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2008 Alejandro P. Revilla
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

package org.jpos.iso;

import junit.framework.TestCase;

/**
 * @author apr
 */
public class ISOMsgTest extends TestCase
{
    public void testGetBytes() throws Exception {
        ISOMsg m = new ISOMsg("0800");
        m.set (3, "000000");
        m.set (52, "CAFEBABE".getBytes());
        m.set ("63.2.3", "Field 63.2.3");
        m.set ("63.2.4", m.getBytes (52));

        assertEquals ("000000", m.getString (3));
        assertEquals ("000000", new String(m.getBytes(3)));
        assertEquals ("CAFEBABE", new String(m.getBytes(52)));
        assertEquals ("Field 63.2.3", m.getString ("63.2.3"));
        assertEquals ("CAFEBABE", new String(m.getBytes("63.2.4")));
    }
}

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
package org.jpos.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;

public class SerializerTest {
    private Calendar cal;

    @Before
    public void setUp() throws Exception {
        cal = new GregorianCalendar(2012, Calendar.MAY, 31, 23, 47, 58);
        cal.set(Calendar.MILLISECOND, 56);
    }

    @Test
    public void testSSerializeAndDeserialize() throws Exception {
        byte[] pickled = Serializer.serialize(cal);
        assertNotNull(pickled);
        Calendar reconstituted = (Calendar) Serializer.deserialize(pickled);
        assertNotNull(reconstituted);
        assertEquals(cal, reconstituted);
        assertNotSame(cal, reconstituted);
    }
}

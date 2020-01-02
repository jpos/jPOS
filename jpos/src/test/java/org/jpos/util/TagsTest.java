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

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TagsTest {
    @Test
    public void testEmpty () {
        Tags ts = new Tags();
        assertEquals("", ts.toString());
    }

    @Test
    public void testOrder () {
        Tags ts = new Tags("def,abc");
        assertEquals("abc,def", ts.toString());
    }

    @Test
    public void testEscape () {
        Tags ts = new Tags("d\\,ef,abc\\\\");
        assertEquals("abc\\\\,d\\,ef", ts.toString());
    }

    @Test
    public void testNullTags() {
        Tags ts = new Tags((String) null);
        assertEquals(0, ts.size(), "size=0");
        assertEquals(new Tags(), ts, "empty tags");
        assertEquals(new Tags(""), ts, "empty tags \"\"");
        assertEquals(new Tags(" "), ts, "empty tags (blank)");
    }

    @Test
    public void testEqualsAndHashCode () {
        Tags ts1 = new Tags("xyz,abc");
        Tags ts2 = new Tags(ts1.toString());
        Tags ts3 = new Tags("abc", "xyz");
        assertTrue(ts1.equals(ts2));
        assertTrue(ts1.hashCode() == ts2.hashCode());
        assertTrue(ts1.equals(ts3));
        assertTrue(ts1.hashCode() == ts3.hashCode());
        ts2.add("xyz"); // dupe entry
        assertTrue(ts1.equals(ts2));
        assertTrue(ts1.hashCode() == ts2.hashCode());
        ts2.add("zyx");
        assertFalse(ts1.equals(ts2));
        assertFalse(ts1.hashCode() == ts2.hashCode());
    }

    @Test
    public void testSerialization() throws Throwable {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Tags ts1 = new Tags("xyz,abc");
        new ObjectOutputStream(baos).writeObject(ts1);
        byte[] buf = baos.toByteArray();
        ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(buf));
        Tags ts2 = (Tags) is.readObject();
        assertTrue(ts1.equals(ts2));
        assertTrue(ts1.hashCode() == ts2.hashCode());
        assertEquals(ts1.toString(), ts2.toString());
    }

    @Test
    public void testContainsAll() {
        Tags ts = new Tags("abc,def,ghi");
        assertTrue(ts.containsAll(new Tags("def,ghi")));
        assertTrue(ts.containsAll(new Tags("abc,def")));
        assertTrue(ts.containsAll(new Tags("abc,ghi,def")));
        assertTrue(ts.containsAll(new Tags()));
        assertTrue(new Tags().containsAll(new Tags()));
        assertFalse(ts.containsAll(new Tags("abc,jkl")));
    }

    @Test
    public void testContainsAny() {
        Tags ts = new Tags("abc,def,ghi");
        assertTrue(ts.containsAny(new Tags("abc,def")));
        assertTrue(ts.containsAny(new Tags("abc,jkl")));
        assertTrue(ts.containsAny(new Tags("")));
        assertTrue(new Tags().containsAny(new Tags()));
        assertFalse(ts.containsAny(new Tags("jkl,mno")));
    }
}

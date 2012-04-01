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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.jpos.testhelpers.EqualsHashCodeTestCase;
import org.junit.Test;

public class MD5TemplateTest {

    @Test
    public void testConstructor() throws Throwable {
        byte[] digest = new byte[3];
        MD5Template mD5Template = new MD5Template("1", digest);
        assertSame("mD5Template.digest", digest, mD5Template.digest);
        assertEquals("mD5Template.key", "1", mD5Template.key);
    }

    @Test
    public void testGetKey() throws Throwable {
        byte[] digest = new byte[1];
        Integer key = Integer.valueOf(0);
        Integer result = (Integer) new MD5Template(key, digest).getKey();
        assertSame("result", key, result);
    }

    @Test
    public void testGetKey1() throws Throwable {
        byte[] key = new byte[1];
        byte[] result = (byte[]) new MD5Template(key, "testString".getBytes()).getKey();
        assertSame("result", key, result);
    }

    @Test
    public void testGetKey2() throws Throwable {
        String result = (String) new MD5Template("testString", "\n".getBytes()).getKey();
        assertEquals("result", "testString", result);
    }

}

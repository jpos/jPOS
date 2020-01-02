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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

public class MD5TemplateTest {

    @Test
    public void testConstructor() throws Throwable {
        byte[] digest = new byte[3];
        MD5Template mD5Template = new MD5Template("1", digest);
        assertSame(digest, mD5Template.digest, "mD5Template.digest");
        assertEquals("1", mD5Template.key, "mD5Template.key");
    }

    @Test
    public void testGetKey() throws Throwable {
        byte[] digest = new byte[1];
        Integer key = Integer.valueOf(0);
        Integer result = (Integer) new MD5Template(key, digest).getKey();
        assertSame(key, result, "result");
    }

    @Test
    public void testGetKey1() throws Throwable {
        byte[] key = new byte[1];
        byte[] result = (byte[]) new MD5Template(key, "testString".getBytes()).getKey();
        assertSame(key, result, "result");
    }

    @Test
    public void testGetKey2() throws Throwable {
        String result = (String) new MD5Template("testString", "\n".getBytes()).getKey();
        assertEquals("testString", result, "result");
    }

}

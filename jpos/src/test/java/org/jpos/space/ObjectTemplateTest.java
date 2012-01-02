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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class ObjectTemplateTest {

    @Test
    public void testConstructor() throws Throwable {
        ObjectTemplate objectTemplate = new ObjectTemplate("", "");
        assertEquals("objectTemplate.key", "", objectTemplate.key);
        assertEquals("objectTemplate.value", "", objectTemplate.value);
    }

    @Test
    public void testEquals() throws Throwable {
        boolean result = new ObjectTemplate(null, new Object()).equals("");
        assertFalse("result", result);
    }

    @Test
    public void testEquals1() throws Throwable {
        boolean result = new ObjectTemplate(new Object(), "").equals("");
        assertTrue("result", result);
    }

    @Test
    public void testEqualsThrowsNullPointerException() throws Throwable {
        try {
            new ObjectTemplate("", null).equals("");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetKey() throws Throwable {
        Integer key = Integer.valueOf(0);
        Integer result = (Integer) new ObjectTemplate(key, Integer.valueOf(0)).getKey();
        assertSame("result", key, result);
    }

    @Test
    public void testGetKey1() throws Throwable {
        Integer key = Integer.valueOf(100);
        Integer result = (Integer) new ObjectTemplate(key, "2").getKey();
        assertSame("result", key, result);
    }

    @Test
    public void testGetKey2() throws Throwable {
        String result = (String) new ObjectTemplate("testString", "").getKey();
        assertEquals("result", "testString", result);
    }
}

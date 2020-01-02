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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

public class ObjectTemplateTest {
    @Test
    public void testConstructor() throws Throwable {
        ObjectTemplate objectTemplate = new ObjectTemplate("", "");
        assertEquals("", objectTemplate.key, "objectTemplate.key");
        assertEquals("", objectTemplate.value, "objectTemplate.value");
    }

    @Test
    public void testGetKey() throws Throwable {
        Integer key = Integer.valueOf(0);
        Integer result = (Integer) new ObjectTemplate(key, Integer.valueOf(0)).getKey();
        assertSame(key, result, "result");
    }

    @Test
    public void testGetKey1() throws Throwable {
        Integer key = Integer.valueOf(100);
        Integer result = (Integer) new ObjectTemplate(key, "2").getKey();
        assertSame(key, result, "result");
    }

    @Test
    public void testGetKey2() throws Throwable {
        String result = (String) new ObjectTemplate("testString", "").getKey();
        assertEquals("testString", result, "result");
    }

    @Test
    public void testEqualsValue() {
        String value = "someValue";
        ObjectTemplate objectTemplate = new ObjectTemplate("key", value);
        assertEquals(objectTemplate, value);
    }

    @Test
    public void testNotEqualsItSelfOrAnother() {
        String value = "someValue";
        ObjectTemplate objectTemplateA = new ObjectTemplate("key", value);
        ObjectTemplate objectTemplateB = new ObjectTemplate("key", value);
        assertFalse(objectTemplateA.equals(objectTemplateA), "should only equals on the template value");
        assertFalse(objectTemplateA.equals(objectTemplateB), "should only equals on the template value");
    }

    @Test
    public void testHaveHashCodeOfValue() {
        String value = "someValue";
        ObjectTemplate objectTemplate = new ObjectTemplate("key", value);
        assertEquals(objectTemplate.hashCode(), value.hashCode(), "Should implement hashCode on the template value");
        assertEquals(objectTemplate.hashCode(), value.hashCode(), "objectTemplate hashCode and value hashCode should be same");
    }
}

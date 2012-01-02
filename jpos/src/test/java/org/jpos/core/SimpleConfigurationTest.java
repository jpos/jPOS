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

package org.jpos.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import junitx.util.PrivateAccessor;

import org.junit.Test;

@SuppressWarnings("unchecked")
public class SimpleConfigurationTest {
    @Test
    public void testConstructor() throws Throwable {
        Properties props = new Properties();
        SimpleConfiguration simpleConfiguration = new SimpleConfiguration(props);
        assertEquals(props, PrivateAccessor.getField(simpleConfiguration, "props"));
    }

    @Test
    public void testConstructorThrowsFileNotFoundException() throws Throwable {
        try {
            new SimpleConfiguration("testSimpleConfigurationFilename");
            fail("Expected FileNotFoundException to be thrown");
        } catch (FileNotFoundException ex) {
            assertEquals("ex.getClass()", FileNotFoundException.class, ex.getClass());
        }
    }

    @Test
    public void testConstructorThrowsNullPointerException() throws Throwable {
        try {
            new SimpleConfiguration((String) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    @Test
    public void testGet() throws Throwable {
        String result = new SimpleConfiguration(new Properties()).get("testSimpleConfigurationName", null);
        assertNull("result", result);
    }

    @Test
    public void testGet1() throws Throwable {
        String result = new SimpleConfiguration(new Properties()).get("testSimpleConfigurationName", "testSimpleConfigurationDef");
        assertEquals("result", "testSimpleConfigurationDef", result);
    }

    @Test
    public void testGet2() throws Throwable {
        SimpleConfiguration simpleConfiguration = new SimpleConfiguration(new Properties());
        simpleConfiguration.put("testString", "");
        String result = simpleConfiguration.get("testString", "testSimpleConfigurationDef");
        assertEquals("result", "", result);
    }

    @Test
    public void testGet3() throws Throwable {
        String result = new SimpleConfiguration(new Properties()).get("testSimpleConfigurationName");
        assertEquals("result", "", result);
    }

    @Test
    public void testGet4() throws Throwable {
        SimpleConfiguration simpleConfiguration = new SimpleConfiguration(new Properties());
        simpleConfiguration.put("E", "");
        String result = simpleConfiguration.get("E");
        assertEquals("result", "", result);
    }

    @Test
    public void testGetAll() throws Throwable {
        SimpleConfiguration simpleConfiguration = new SimpleConfiguration();
        simpleConfiguration.put("testString", "");
        String[] result = simpleConfiguration.getAll("testString");
        assertEquals("result.length", 1, result.length);
        assertEquals("result[0]", "", result[0]);
    }

    @Test
    public void testGetAll1() throws Throwable {
        String[] result = new SimpleConfiguration(new Properties()).getAll("testSimpleConfigurationName");
        assertEquals("result.length", 0, result.length);
    }

    @Test
    public void testGetAllThrowsNullPointerException() throws Throwable {
        try {
            new SimpleConfiguration((Properties) null).getAll("testSimpleConfigurationName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetBoolean() throws Throwable {
        boolean result = new SimpleConfiguration().getBoolean("testSimpleConfigurationName", true);
        assertTrue("result", result);
    }

    @Test
    public void testGetBoolean1() throws Throwable {
        SimpleConfiguration simpleConfiguration = new SimpleConfiguration(new Properties());
        simpleConfiguration.put("testString", "");
        boolean result = simpleConfiguration.getBoolean("testString", false);
        assertFalse("result", result);
    }

    @Test
    public void testGetBoolean2() throws Throwable {
        SimpleConfiguration simpleConfiguration = new SimpleConfiguration(new Properties());
        simpleConfiguration.put("testString", "testString");
        boolean result = simpleConfiguration.getBoolean("testString", true);
        assertFalse("result", result);
    }

    @Test
    public void testGetBoolean3() throws Throwable {
        Properties props = new Properties();
        props.put("^\n", "testString");
        boolean result = new SimpleConfiguration(props).getBoolean("^\n");
        assertFalse("result", result);
    }

    @Test
    public void testGetBoolean4() throws Throwable {
        boolean result = new SimpleConfiguration(new Properties()).getBoolean("testSimpleConfigurationName");
        assertFalse("result", result);
    }

    @Test
    public void testGetBoolean5() throws Throwable {
        SimpleConfiguration simpleConfiguration = new SimpleConfiguration(new Properties());
        simpleConfiguration.put("testString", "");
        boolean result = simpleConfiguration.getBoolean("testString");
        assertFalse("result", result);
    }

    @Test
    public void testGetBoolean6() throws Throwable {
        Properties props = new Properties();
        SimpleConfiguration simpleConfiguration = new SimpleConfiguration(props);
        props.clear();
        props.put("testString", "testString");
        String name = (String) props.put("testString", new ArrayList());
        boolean result = simpleConfiguration.getBoolean(name);
        assertFalse("result", result);
    }

    @Test
    public void testGetBoolean7() throws Throwable {
        Properties props = new Properties();
        SimpleConfiguration simpleConfiguration = new SimpleConfiguration(props);
        props.clear();
        Collection arrayList = new ArrayList();
        arrayList.add("testString");
        props.put("testString", "testString");
        String name = (String) props.put("testString", arrayList);
        boolean result = simpleConfiguration.getBoolean(name);
        assertFalse("result", result);
    }

    @Test
    public void testGetBooleanThrowsNullPointerException() throws Throwable {
        try {
            new SimpleConfiguration((Properties) null).getBoolean("testSimpleConfigurationName", true);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetBooleanThrowsNullPointerException1() throws Throwable {
        try {
            new SimpleConfiguration((Properties) null).getBoolean("testSimpleConfigurationName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetDouble() throws Throwable {
        double result = new SimpleConfiguration(new Properties()).getDouble("testSimpleConfigurationName");
        assertEquals("result", 0.0, result, 1.0E-6);
    }

    @Test
    public void testGetDouble1() throws Throwable {
        double result = new SimpleConfiguration(new Properties()).getDouble("testSimpleConfigurationName", 0.0);
        assertEquals("result", 0.0, result, 1.0E-6);
    }

    @Test
    public void testGetDouble2() throws Throwable {
        double result = new SimpleConfiguration(new Properties()).getDouble("testSimpleConfigurationName", 100.0);
        assertEquals("result", 100.0, result, 1.0E-6);
    }

    @Test
    public void testGetDoubleThrowsNullPointerException() throws Throwable {
        try {
            new SimpleConfiguration((Properties) null).getDouble("testSimpleConfigurationName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetDoubleThrowsNullPointerException1() throws Throwable {
        try {
            new SimpleConfiguration((Properties) null).getDouble("testSimpleConfigurationName", 100.0);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetDoubleThrowsNumberFormatException() throws Throwable {
        Properties props = new Properties();
        props.put("X%:?6IB(BA", "testString");
        try {
            new SimpleConfiguration(props).getDouble("X%:?6IB(BA");
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("ex.getMessage()", "For input string: \"testString\"", ex.getMessage());
        }
    }

    @Test
    public void testGetDoubleThrowsNumberFormatException1() throws Throwable {
        SimpleConfiguration simpleConfiguration = new SimpleConfiguration(new Properties());
        simpleConfiguration.put("testString", "");
        try {
            simpleConfiguration.getDouble("testString", 100.0);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("ex.getMessage()", "empty String", ex.getMessage());
        }
    }

    @Test
    public void testGetInt() throws Throwable {
        int result = new SimpleConfiguration(new Properties()).getInt("testSimpleConfigurationName", 100);
        assertEquals("result", 100, result);
    }

    @Test
    public void testGetInt1() throws Throwable {
        int result = new SimpleConfiguration(new Properties()).getInt("testSimpleConfigurationName", 0);
        assertEquals("result", 0, result);
    }

    @Test
    public void testGetInt2() throws Throwable {
        int result = new SimpleConfiguration(new Properties()).getInt("testSimpleConfigurationName");
        assertEquals("result", 0, result);
    }

    @Test
    public void testGetIntThrowsNullPointerException() throws Throwable {
        try {
            new SimpleConfiguration((Properties) null).getInt("testSimpleConfigurationName", 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetIntThrowsNullPointerException1() throws Throwable {
        try {
            new SimpleConfiguration((Properties) null).getInt("testSimpleConfigurationName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetIntThrowsNumberFormatException() throws Throwable {
        SimpleConfiguration simpleConfiguration = new SimpleConfiguration(new Properties());
        simpleConfiguration.put("testString", "");
        try {
            simpleConfiguration.getInt("testString", 100);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("ex.getMessage()", "For input string: \"\"", ex.getMessage());
        }
    }

    @Test
    public void testGetIntThrowsNumberFormatException1() throws Throwable {
        Properties props = new Properties();
        props.put("YnFqoMm>b^[gMH*a^qc\f\n#\nUXhdWK\\^G", "testString");
        try {
            new SimpleConfiguration(props).getInt("YnFqoMm>b^[gMH*a^qc\f\n#\nUXhdWK\\^G");
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("ex.getMessage()", "For input string: \"testString\"", ex.getMessage());
        }
    }

    @Test
    public void testGetLong() throws Throwable {
        long result = new SimpleConfiguration().getLong("testSimpleConfigurationName", 100L);
        assertEquals("result", 100L, result);
    }

    @Test
    public void testGetLong1() throws Throwable {
        long result = new SimpleConfiguration(new Properties()).getLong("testSimpleConfigurationName", 0L);
        assertEquals("result", 0L, result);
    }

    @Test
    public void testGetLong2() throws Throwable {
        long result = new SimpleConfiguration(new Properties()).getLong("testSimpleConfigurationName");
        assertEquals("result", 0L, result);
    }

    @Test
    public void testGetLong3() throws Throwable {
        Properties props = new Properties();
        props.clear();
        props.put("testString", "testString");
        String name = (String) props.put("testString", "4");
        long result = new SimpleConfiguration(props).getLong(name);
        assertEquals("result", 4L, result);
    }

    @Test
    public void testGetLongThrowsNullPointerException() throws Throwable {
        try {
            new SimpleConfiguration(new Properties()).getLong(null, 100L);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetLongThrowsNullPointerException1() throws Throwable {
        try {
            new SimpleConfiguration((Properties) null).getLong("testSimpleConfigurationName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetLongThrowsNumberFormatException() throws Throwable {
        Properties props = new Properties();
        props.put("testString", "testString");
        try {
            new SimpleConfiguration(props).getLong("testString");
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("ex.getMessage()", "For input string: \"testString\"", ex.getMessage());
        }
    }

    @Test
    public void testGetThrowsNullPointerException() throws Throwable {
        try {
            new SimpleConfiguration((Properties) null).get("testSimpleConfigurationName", "testSimpleConfigurationDef");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetThrowsNullPointerException1() throws Throwable {
        try {
            new SimpleConfiguration((Properties) null).get("testSimpleConfigurationName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testLoadThrowsFileNotFoundException() throws Throwable {
        Properties props = new Properties();
        SimpleConfiguration simpleConfiguration = new SimpleConfiguration(props);
        try {
            simpleConfiguration.load("testSimpleConfigurationFilename");
            fail("Expected FileNotFoundException to be thrown");
        } catch (FileNotFoundException ex) {
            assertEquals("ex.getClass()", FileNotFoundException.class, ex.getClass());
        }
    }

    @Test
    public void testLoadThrowsNullPointerException() throws Throwable {
        SimpleConfiguration simpleConfiguration = new SimpleConfiguration();
        try {
            simpleConfiguration.load(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    @Test
    public void testPutThrowsNullPointerException() throws Throwable {
        SimpleConfiguration simpleConfiguration = new SimpleConfiguration((Properties) null);
        try {
            simpleConfiguration.put("testSimpleConfigurationName", "testString");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}

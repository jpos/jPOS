/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2018 jPOS Software SRL
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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class SubConfigurationTest {

    @Test
    public void testConstructor() throws Throwable {
        new SubConfiguration();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testConstructor1() throws Throwable {
        Configuration cfg = new SubConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
        assertEquals("subConfiguration.prefix", "testSubConfigurationPrefix", subConfiguration.prefix);
    }

    @Test
    public void testGet() throws Throwable {
        Configuration cfg = new SimpleConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        String result = subConfiguration.get("testSubConfigurationPropertyName");
        assertEquals("result", "", result);
        assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
    }

    @Test
    public void testGet1() throws Throwable {
        Configuration cfg = new SimpleConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        String result = subConfiguration.get("testSubConfigurationPropertyName", "testSubConfigurationDefaultValue");
        assertEquals("result", "testSubConfigurationDefaultValue", result);
        assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
    }

    @Test
    public void testGetAll() throws Throwable {
        Configuration cfg = new SimpleConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        subConfiguration.put("testString", "");
        String[] result = subConfiguration.getAll("testString");
        assertEquals("result.length", 1, result.length);
        assertEquals("result[0]", "", result[0]);
        assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
    }

    @Test
    public void testGetAll1() throws Throwable {
        Configuration cfg = new SimpleConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        String[] result = subConfiguration.getAll("testSubConfigurationPropertyName");
        assertEquals("result.length", 0, result.length);
        assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
    }

    @Test
    public void testGetAllThrowsNullPointerException() throws Throwable {
        Configuration cfg = new SubConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        try {
            subConfiguration.getAll("testSubConfigurationPropertyName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
        }
    }

    @Test
    public void testGetBoolean() throws Throwable {
        Configuration cfg = new SimpleConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        boolean result = subConfiguration.getBoolean("testSubConfigurationPropertyName");
        assertFalse("result", result);
        assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
    }

    @Test
    public void testGetBoolean1() throws Throwable {
        Configuration cfg = new SimpleConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        boolean result = subConfiguration.getBoolean("testSubConfigurationPropertyName", false);
        assertFalse("result", result);
        assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
    }

    @Test
    public void testGetBoolean2() throws Throwable {
        Configuration cfg = new SimpleConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        boolean result = subConfiguration.getBoolean("testSubConfigurationPropertyName", true);
        assertTrue("result", result);
        assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
    }

    @Test
    public void testGetBooleanThrowsNullPointerException() throws Throwable {
        Configuration cfg = new SubConfiguration(new SubConfiguration(), "testSubConfigurationPrefix");
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix1");
        try {
            subConfiguration.getBoolean("testSubConfigurationPropertyName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
        }
    }

    @Test
    public void testGetBooleanThrowsNullPointerException1() throws Throwable {
        SubConfiguration subConfiguration = new SubConfiguration();
        try {
            subConfiguration.getBoolean("testSubConfigurationPropertyName", true);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("subConfiguration.cfg", subConfiguration.cfg);
        }
    }

    @Test
    public void testGetDouble() throws Throwable {
        Configuration cfg = new SimpleConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        double result = subConfiguration.getDouble("testSubConfigurationPropertyName");
        assertEquals("result", 0.0, result, 1.0E-6);
        assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
    }

    @Test
    public void testGetDouble1() throws Throwable {
        Configuration cfg = new SimpleConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        double result = subConfiguration.getDouble("testSubConfigurationPropertyName", 0.0);
        assertEquals("result", 0.0, result, 1.0E-6);
        assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
    }

    @Test
    public void testGetDouble2() throws Throwable {
        Configuration cfg = new SimpleConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        double result = subConfiguration.getDouble("testSubConfigurationPropertyName", 100.0);
        assertEquals("result", 100.0, result, 1.0E-6);
        assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
    }

    @Test
    public void testGetDoubleThrowsNullPointerException() throws Throwable {
        Configuration cfg = new SubConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        try {
            subConfiguration.getDouble("testSubConfigurationPropertyName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
        }
    }

    @Test
    public void testGetDoubleThrowsNullPointerException1() throws Throwable {
        Configuration cfg = new SubConfiguration(new SubConfiguration(), "testSubConfigurationPrefix");
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix1");
        try {
            subConfiguration.getDouble("testSubConfigurationPropertyName", 100.0);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
        }
    }

    @Test
    public void testGetDoubleThrowsNumberFormatException() throws Throwable {
        Configuration cfg = new SimpleConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        subConfiguration.put("testString", "false");
        try {
            subConfiguration.getDouble("testString");
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("ex.getMessage()", "For input string: \"false\"", ex.getMessage());
            assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
        }
    }

    @Test
    public void testGetDoubleThrowsNumberFormatException1() throws Throwable {
        Configuration cfg = new SimpleConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        subConfiguration.put("testString", "");
        try {
            subConfiguration.getDouble("testString", 100.0);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("ex.getMessage()", "empty String", ex.getMessage());
            assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
        }
    }

    @Test
    public void testGetInt() throws Throwable {
        Configuration cfg = new SimpleConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        int result = subConfiguration.getInt("testSubConfigurationPropertyName", 100);
        assertEquals("result", 100, result);
        assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
    }

    @Test
    public void testGetInt1() throws Throwable {
        Configuration cfg = new SimpleConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        int result = subConfiguration.getInt("testSubConfigurationPropertyName", 0);
        assertEquals("result", 0, result);
        assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
    }

    @Test
    public void testGetInt2() throws Throwable {
        Configuration cfg = new SimpleConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        int result = subConfiguration.getInt("testSubConfigurationPropertyName");
        assertEquals("result", 0, result);
        assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
    }

    @Test
    public void testGetIntThrowsNullPointerException() throws Throwable {
        Configuration cfg = new SubConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        try {
            subConfiguration.getInt("testSubConfigurationPropertyName", 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
        }
    }

    @Test
    public void testGetIntThrowsNullPointerException1() throws Throwable {
        Configuration cfg = new SubConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        try {
            subConfiguration.getInt("testSubConfigurationPropertyName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
        }
    }

    @Test
    public void testGetIntThrowsNumberFormatException() throws Throwable {
        Configuration cfg = new SimpleConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        subConfiguration.put("testString", "");
        try {
            subConfiguration.getInt("testString", 100);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("ex.getMessage()", "For input string: \"\"", ex.getMessage());
            assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
        }
    }

    @Test
    public void testGetIntThrowsNumberFormatException1() throws Throwable {
        SubConfiguration cfg = new SubConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        cfg.setConfiguration(new SimpleConfiguration());
        subConfiguration.put("testString", "");
        try {
            subConfiguration.getInt("testString");
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("ex.getMessage()", "For input string: \"\"", ex.getMessage());
            assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
        }
    }

    @Test
    public void testGetLong() throws Throwable {
        Configuration cfg = new SimpleConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        long result = subConfiguration.getLong("testSubConfigurationPropertyName", 100L);
        assertEquals("result", 100L, result);
        assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
    }

    @Test
    public void testGetLong1() throws Throwable {
        Configuration cfg = new SimpleConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        long result = subConfiguration.getLong("testSubConfigurationPropertyName", 0L);
        assertEquals("result", 0L, result);
        assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
    }

    @Test
    public void testGetLong2() throws Throwable {
        Configuration cfg = new SimpleConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        long result = subConfiguration.getLong("testSubConfigurationPropertyName");
        assertEquals("result", 0L, result);
        assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
    }

    @Test
    public void testGetLongThrowsNullPointerException() throws Throwable {
        Configuration cfg = new SubConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        try {
            subConfiguration.getLong("testSubConfigurationPropertyName", 100L);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
        }
    }

    @Test
    public void testGetLongThrowsNullPointerException1() throws Throwable {
        SubConfiguration subConfiguration = new SubConfiguration();
        try {
            subConfiguration.getLong("testSubConfigurationPropertyName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("subConfiguration.cfg", subConfiguration.cfg);
        }
    }

    @Test
    public void testGetLongThrowsNumberFormatException() throws Throwable {
        Configuration cfg = new SimpleConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        subConfiguration.put("testString", "false");
        try {
            subConfiguration.getLong("testString", 100L);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("ex.getMessage()", "For input string: \"false\"", ex.getMessage());
            assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
        }
    }

    @Test
    public void testGetLongThrowsNumberFormatException1() throws Throwable {
        SubConfiguration cfg = new SubConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        cfg.setConfiguration(new SimpleConfiguration());
        subConfiguration.put("testString", "");
        try {
            subConfiguration.getLong("testString");
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("ex.getMessage()", "For input string: \"\"", ex.getMessage());
            assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
        }
    }

    @Test
    public void testGetObjectThrowsConfigurationException() throws Throwable {
        Configuration cfg = new SubConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        try {
            subConfiguration.getObject("testSubConfigurationPropertyName");
            fail("Expected ConfigurationException to be thrown");
        } catch (ConfigurationException ex) {
            assertEquals("ex.getMessage()",
                    "Error trying to create an object from property testSubConfigurationPrefixtestSubConfigurationPropertyName",
                    ex.getMessage());
            assertNull("ex.getNested().getMessage()", ex.getNested().getMessage());
            assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
        }
    }

    @Test
    public void testGetThrowsNullPointerException() throws Throwable {
        Configuration cfg = new SubConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        try {
            subConfiguration.get("testSubConfigurationPropertyName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
        }
    }

    @Test
    public void testGetThrowsNullPointerException1() throws Throwable {
        SubConfiguration subConfiguration = new SubConfiguration();
        try {
            subConfiguration.get("testSubConfigurationPropertyName", "testSubConfigurationDefaultValue");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("subConfiguration.cfg", subConfiguration.cfg);
        }
    }

    @Test
    public void testPut() throws Throwable {
        Configuration cfg = new SimpleConfiguration();
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix");
        subConfiguration.put("testSubConfigurationName", "");
        assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
    }

    @Test
    public void testPutThrowsNullPointerException() throws Throwable {
        Configuration cfg = new SubConfiguration(new SubConfiguration(new SubConfiguration(), "testSubConfigurationPrefix"),
                "testSubConfigurationPrefix1");
        SubConfiguration subConfiguration = new SubConfiguration(cfg, "testSubConfigurationPrefix2");
        try {
            subConfiguration.put("testSubConfigurationName", "testString");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertSame("subConfiguration.cfg", cfg, subConfiguration.cfg);
        }
    }

    @Test
    public void testSetConfiguration() throws Throwable {
        SubConfiguration subConfiguration = new SubConfiguration(new SubConfiguration(), "testSubConfigurationPrefix");
        Configuration newCfg = new SimpleConfiguration();
        subConfiguration.setConfiguration(newCfg);
        assertSame("subConfiguration.cfg", newCfg, subConfiguration.cfg);
    }

    @Test
    public void testSetPrefix() throws Throwable {
        SubConfiguration subConfiguration = new SubConfiguration();
        subConfiguration.setPrefix("testSubConfigurationNewPrefix");
        assertEquals("subConfiguration.prefix", "testSubConfigurationNewPrefix", subConfiguration.prefix);
    }
}

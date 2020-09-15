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

package org.jpos.core;

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import org.jpos.util.Serializer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
public class SimpleConfigurationTest {
//    @Test
//    public void testConstructor() throws Throwable {
//        Properties props = new Properties();
//        SimpleConfiguration simpleConfiguration = new SimpleConfiguration(props);
//        assertEquals(props, PrivateAccessor.getField(simpleConfiguration, "props"));
//    }

    @Test
    public void testConstructorThrowsFileNotFoundException() throws Throwable {
        try {
            new SimpleConfiguration("testSimpleConfigurationFilename");
            fail("Expected FileNotFoundException to be thrown");
        } catch (FileNotFoundException ex) {
            assertEquals(FileNotFoundException.class, ex.getClass(), "ex.getClass()");
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
        assertNull(result, "result");
    }

    @Test
    public void testGet1() throws Throwable {
        String result = new SimpleConfiguration(new Properties()).get("testSimpleConfigurationName", "testSimpleConfigurationDef");
        assertEquals("testSimpleConfigurationDef", result, "result");
    }

    @Test
    public void testGet2() throws Throwable {
        SimpleConfiguration simpleConfiguration = new SimpleConfiguration(new Properties());
        simpleConfiguration.put("testString", "");
        String result = simpleConfiguration.get("testString", "testSimpleConfigurationDef");
        assertEquals("", result, "result");
    }

    @Test
    public void testGet3() throws Throwable {
        String result = new SimpleConfiguration(new Properties()).get("testSimpleConfigurationName");
        assertEquals("", result, "result");
    }

    @Test
    public void testGet4() throws Throwable {
        SimpleConfiguration simpleConfiguration = new SimpleConfiguration(new Properties());
        simpleConfiguration.put("E", "");
        String result = simpleConfiguration.get("E");
        assertEquals("", result, "result");
    }

    @Test
    public void testGetAll() throws Throwable {
        SimpleConfiguration simpleConfiguration = new SimpleConfiguration();
        simpleConfiguration.put("testString", "");
        String[] result = simpleConfiguration.getAll("testString");
        assertEquals(1, result.length, "result.length");
        assertEquals("", result[0], "result[0]");
    }

    @Test
    public void testGetAll1() throws Throwable {
        String[] result = new SimpleConfiguration(new Properties()).getAll("testSimpleConfigurationName");
        assertEquals(0, result.length, "result.length");
    }

    @Test
    public void testGetAllThrowsNullPointerException() throws Throwable {
        try {
            new SimpleConfiguration((Properties) null).getAll("testSimpleConfigurationName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.util.Properties.get(Object)\" because \"this.props\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGetBoolean() throws Throwable {
        boolean result = new SimpleConfiguration().getBoolean("testSimpleConfigurationName", true);
        assertTrue(result, "result");
    }

    @Test
    public void testGetBoolean1() throws Throwable {
        SimpleConfiguration simpleConfiguration = new SimpleConfiguration(new Properties());
        simpleConfiguration.put("testString", "");
        boolean result = simpleConfiguration.getBoolean("testString", false);
        assertFalse(result, "result");
    }

    @Test
    public void testGetBoolean2() throws Throwable {
        SimpleConfiguration simpleConfiguration = new SimpleConfiguration(new Properties());
        simpleConfiguration.put("testString", "testString");
        boolean result = simpleConfiguration.getBoolean("testString", true);
        assertFalse(result, "result");
    }

    @Test
    public void testGetBoolean3() throws Throwable {
        Properties props = new Properties();
        props.put("^\n", "testString");
        boolean result = new SimpleConfiguration(props).getBoolean("^\n");
        assertFalse(result, "result");
    }

    @Test
    public void testGetBoolean4() throws Throwable {
        boolean result = new SimpleConfiguration(new Properties()).getBoolean("testSimpleConfigurationName");
        assertFalse(result, "result");
    }

    @Test
    public void testGetBoolean5() throws Throwable {
        SimpleConfiguration simpleConfiguration = new SimpleConfiguration(new Properties());
        simpleConfiguration.put("testString", "");
        boolean result = simpleConfiguration.getBoolean("testString");
        assertFalse(result, "result");
    }

    @Test
    public void testGetBoolean6() throws Throwable {
        Properties props = new Properties();
        SimpleConfiguration simpleConfiguration = new SimpleConfiguration(props);
        props.clear();
        props.put("testString", "testString");
        String name = (String) props.put("testString", new ArrayList());
        boolean result = simpleConfiguration.getBoolean(name);
        assertFalse(result, "result");
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
        assertFalse(result, "result");
    }

    @Test
    public void testGetBooleanThrowsNullPointerException() throws Throwable {
        try {
            new SimpleConfiguration((Properties) null).getBoolean("testSimpleConfigurationName", true);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.util.Properties.get(Object)\" because \"this.props\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGetBooleanThrowsNullPointerException1() throws Throwable {
        try {
            new SimpleConfiguration((Properties) null).getBoolean("testSimpleConfigurationName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.util.Properties.get(Object)\" because \"this.props\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGetDouble() throws Throwable {
        double result = new SimpleConfiguration(new Properties()).getDouble("testSimpleConfigurationName");
        assertEquals(0.0, result, 1.0E-6, "result");
    }

    @Test
    public void testGetDouble1() throws Throwable {
        double result = new SimpleConfiguration(new Properties()).getDouble("testSimpleConfigurationName", 0.0);
        assertEquals(0.0, result, 1.0E-6, "result");
    }

    @Test
    public void testGetDouble2() throws Throwable {
        double result = new SimpleConfiguration(new Properties()).getDouble("testSimpleConfigurationName", 100.0);
        assertEquals(100.0, result, 1.0E-6, "result");
    }

    @Test
    public void testGetDoubleThrowsNullPointerException() throws Throwable {
        try {
            new SimpleConfiguration((Properties) null).getDouble("testSimpleConfigurationName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.util.Properties.get(Object)\" because \"this.props\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGetDoubleThrowsNullPointerException1() throws Throwable {
        try {
            new SimpleConfiguration((Properties) null).getDouble("testSimpleConfigurationName", 100.0);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.util.Properties.get(Object)\" because \"this.props\" is null", ex.getMessage(), "ex.getMessage()");
            }
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
            assertEquals("For input string: \"testString\"", ex.getMessage(), "ex.getMessage()");
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
            assertEquals("empty String", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testGetInt() throws Throwable {
        int result = new SimpleConfiguration(new Properties()).getInt("testSimpleConfigurationName", 100);
        assertEquals(100, result, "result");
    }

    @Test
    public void testGetInt1() throws Throwable {
        int result = new SimpleConfiguration(new Properties()).getInt("testSimpleConfigurationName", 0);
        assertEquals(0, result, "result");
    }

    @Test
    public void testGetInt2() throws Throwable {
        int result = new SimpleConfiguration(new Properties()).getInt("testSimpleConfigurationName");
        assertEquals(0, result, "result");
    }

    @Test
    public void testGetIntThrowsNullPointerException() throws Throwable {
        try {
            new SimpleConfiguration((Properties) null).getInt("testSimpleConfigurationName", 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.util.Properties.get(Object)\" because \"this.props\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGetIntThrowsNullPointerException1() throws Throwable {
        try {
            new SimpleConfiguration((Properties) null).getInt("testSimpleConfigurationName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.util.Properties.get(Object)\" because \"this.props\" is null", ex.getMessage(), "ex.getMessage()");
            }
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
            assertEquals("For input string: \"\"", ex.getMessage(), "ex.getMessage()");
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
            assertEquals("For input string: \"testString\"", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testGetLong() throws Throwable {
        long result = new SimpleConfiguration().getLong("testSimpleConfigurationName", 100L);
        assertEquals(100L, result, "result");
    }

    @Test
    public void testGetLong1() throws Throwable {
        long result = new SimpleConfiguration(new Properties()).getLong("testSimpleConfigurationName", 0L);
        assertEquals(0L, result, "result");
    }

    @Test
    public void testGetLong2() throws Throwable {
        long result = new SimpleConfiguration(new Properties()).getLong("testSimpleConfigurationName");
        assertEquals(0L, result, "result");
    }

    @Test
    public void testGetLong3() throws Throwable {
        Properties props = new Properties();
        props.clear();
        props.put("testString", "testString");
        String name = (String) props.put("testString", "4");
        long result = new SimpleConfiguration(props).getLong(name);
        assertEquals(4L, result, "result");
    }

    @Test
    public void testGetLongThrowsNullPointerException() throws Throwable {
        try {
            new SimpleConfiguration(new Properties()).getLong(null, 100L);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"Object.hashCode()\" because \"key\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGetLongThrowsNullPointerException1() throws Throwable {
        try {
            new SimpleConfiguration((Properties) null).getLong("testSimpleConfigurationName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.util.Properties.get(Object)\" because \"this.props\" is null", ex.getMessage(), "ex.getMessage()");
            }
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
            assertEquals("For input string: \"testString\"", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testGetThrowsNullPointerException() throws Throwable {
        try {
            new SimpleConfiguration((Properties) null).get("testSimpleConfigurationName", "testSimpleConfigurationDef");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.util.Properties.get(Object)\" because \"this.props\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGetThrowsNullPointerException1() throws Throwable {
        try {
            new SimpleConfiguration((Properties) null).get("testSimpleConfigurationName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.util.Properties.get(Object)\" because \"this.props\" is null", ex.getMessage(), "ex.getMessage()");
            }
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
            assertEquals(FileNotFoundException.class, ex.getClass(), "ex.getClass()");
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
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.util.Properties.put(Object, Object)\" because \"this.props\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testSerializable() throws Throwable {
        SimpleConfiguration cfg = new SimpleConfiguration();
        cfg.put ("A", "The Quick Brown Fox Jumps Over The Lazy Dog");
        Configuration cfg1 = Serializer.serializeDeserialize(cfg);
        assertEquals(cfg.get("A"), cfg1.get("A"), "cfg.A should equal cfg1.A");
        assertEquals (cfg, cfg1, "cfg should equal cfg1");
    }

    @Test
    public void testReadSystemProperty () {
        SimpleConfiguration cfg = new SimpleConfiguration();
        System.setProperty("jpos.url", "http://jpos.org");
        cfg.put("host", "${jpos.url}");
        assertEquals("http://jpos.org", cfg.get("host"));
        cfg.put("host", "$sys{jpos.url}");
        assertEquals("http://jpos.org", cfg.get("host"));
        cfg.put("host", "$env{jpos.url}");
        assertTrue(cfg.get("host").isEmpty());
    }

    @Test
    public void testInvalidProperty() {
        SimpleConfiguration cfg = new SimpleConfiguration();
        cfg.put("host", "$invalid{jpos.url}");
        assertEquals("$invalid{jpos.url}", cfg.get("host"));
    }

    @Test
    @Disabled // regexp failing
    public void testInvalidNested() {
        SimpleConfiguration cfg = new SimpleConfiguration();
        cfg.put("invalid", "$invalid{${nested}}");
        assertEquals("$invalid{${nested}}", cfg.get("invalid"));
    }

    @Test
    public void testReadVerbatimProperty () {
        SimpleConfiguration cfg = new SimpleConfiguration();
        cfg.put ("verbatim", "$verb{${verbatim.property}}");
        assertEquals("${verbatim.property}", cfg.get("verbatim"));
    }

    @Test
    public void testReadEnvironmentVariable () {
        String envVarName = "HOME";
        if (System.getProperty("os.name").startsWith("Windows")) envVarName = "OS";

        SimpleConfiguration cfg = new SimpleConfiguration();
        cfg.put("home", "$env{"+envVarName+"}");
        assertEquals(System.getenv(envVarName), cfg.get("home"));
        cfg.put("home", "${"+envVarName+"}");
        assertEquals(System.getenv(envVarName), cfg.get("home"));
        cfg.put("home", "$sys{"+envVarName+"}");
        assertTrue(cfg.get("home").isEmpty());
    }

    @Test
    public void testgetAllProperty() {
        SimpleConfiguration cfg = new SimpleConfiguration();
        System.setProperty("jpos.url", "http://jpos.org");
        cfg.put("host", "${jpos.url}");
        assertArrayEquals(new String[] { "http://jpos.org" }, cfg.getAll("host"));
    }

    @Test
    public void testMultipleProperties() {
        System.setProperty("jpos.host", "http://jpos.org");
        System.setProperty("jpos.port", "80");
        SimpleConfiguration cfg = new SimpleConfiguration();
        cfg.put ("host", "${jpos.host}:${jpos.port}");
        assertEquals("http://jpos.org:80", cfg.get("host"));
    }


    @Test
    public void testDefaultPropertySimple() {
        SimpleConfiguration cfg = new SimpleConfiguration();
        cfg.put("myprop", "AAA ${jpos.xxx:default_value} BBB");
        assertEquals("AAA default_value BBB", cfg.get("myprop"));
    }


    @Test
    public void testDefaultPropertyComplex() {
        SimpleConfiguration cfg = new SimpleConfiguration();
        // first colon is the default separator
        // second colon should be part of the default value
        // extra $, { and } added, trying to make it fail
        cfg.put("myprop", "A$A ${jpos.xxx:default:value${} B${B");
        assertEquals("A$A default:value${ B${B", cfg.get("myprop"));
    }

    @Test
    public void testDefaultMultipleProperties() {
        System.setProperty("jpos.hello", "Hello jPOS!");
        SimpleConfiguration cfg = new SimpleConfiguration();

        // first colon is the default separator
        // second colon should be part of the default value
        // try with several replacements in the string
        String value= "AAA $sys{intro:Introduction:} BBB ${jpos.hello} CCC ${jpos.xxx:(I said: Hello jPOS!)} DDD";
        cfg.put("myprop", value);

        // first, the `intro` sys property is not defined, so we get the default value
        assertEquals("AAA Introduction: BBB Hello jPOS! CCC (I said: Hello jPOS!) DDD", cfg.get("myprop"));

        // now, we define `intro`, so we get its value from the sys propertis
        System.setProperty("intro", "Say it:");
        assertEquals("AAA Say it: BBB Hello jPOS! CCC (I said: Hello jPOS!) DDD", cfg.get("myprop"));
    }

}

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

package org.jpos.security;

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Properties;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.util.Logger;
import org.junit.jupiter.api.Test;

public class SimpleKeyFileTest {

    @Test
    public void testConstructor() throws Throwable {
        SimpleKeyFile simpleKeyFile = new SimpleKeyFile();
        assertEquals(0, simpleKeyFile.props.size(), "simpleKeyFile.props.size()");
        assertNull(simpleKeyFile.logger, "simpleKeyFile.logger");
        assertEquals("Key File", simpleKeyFile.header, "simpleKeyFile.header");
        assertNull(simpleKeyFile.realm, "simpleKeyFile.realm");
    }

    @Test
    public void testConstructorThrowsNullPointerException() throws Throwable {
        try {
            new SimpleKeyFile(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull(ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testGetLogger() throws Throwable {
        Logger logger = Logger.getLogger("testSimpleKeyFileName");
        SimpleKeyFile simpleKeyFile = new SimpleKeyFile();
        simpleKeyFile.setLogger(logger, "testSimpleKeyFileRealm");
        Logger result = simpleKeyFile.getLogger();
        assertSame(logger, result, "result");
    }

    @Test
    public void testGetPropertyThrowsSecureKeyStoreException() throws Throwable {
        try {
            new SimpleKeyFile().getProperty("testSimpleKeyFileAlias", "testSimpleKeyFileSubName");
            fail("Expected SecureKeyStoreException to be thrown");
        } catch (SecureKeyStore.SecureKeyStoreException ex) {
            assertEquals("Key can't be retrieved. Can't get property: testSimpleKeyFileAlias.testSimpleKeyFileSubName",
                    ex.getMessage(), "ex.getMessage()");
            assertNull(ex.getNested(), "ex.getNested()");
        }
    }

    @Test
    public void testGetRealm() throws Throwable {
        String result = new SimpleKeyFile().getRealm();
        assertNull(result, "result");
    }

    @Test
    public void testGetRealm1() throws Throwable {
        SimpleKeyFile simpleKeyFile = new SimpleKeyFile();
        simpleKeyFile.setLogger(Logger.getLogger("testSimpleKeyFileName"), "testSimpleKeyFileRealm");
        String result = simpleKeyFile.getRealm();
        assertEquals("testSimpleKeyFileRealm", result, "result");
    }

    @Test
    public void testInitThrowsNullPointerException() throws Throwable {
        SimpleKeyFile simpleKeyFile = new SimpleKeyFile();
        try {
            simpleKeyFile.init(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull(ex.getMessage(), "ex.getMessage()");
            assertNull(simpleKeyFile.file, "simpleKeyFile.file");
            assertEquals(0, simpleKeyFile.props.size(), "simpleKeyFile.props.size()");
        }
    }

    @Test
    public void testInitThrowsSecureKeyStoreException() throws Throwable {
        SimpleKeyFile simpleKeyFile = new SimpleKeyFile();
        try {
            simpleKeyFile.init(".");
            fail("Expected SecureKeyStoreException to be thrown");
        } catch (SecureKeyStore.SecureKeyStoreException ex) {
            assertEquals(".", simpleKeyFile.file.getName(), "simpleKeyFile.file.getName()");
            assertEquals(0, simpleKeyFile.props.size(), "simpleKeyFile.props.size()");
        }
    }

    @Test
    public void testLoadThrowsSecureKeyStoreException() throws Throwable {
        SimpleKeyFile simpleKeyFile = new SimpleKeyFile();
        try {
            simpleKeyFile.load();
            fail("Expected SecureKeyStoreException to be thrown");
        } catch (SecureKeyStore.SecureKeyStoreException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertEquals("java.lang.NullPointerException", ex.getMessage(), "ex.getMessage()");
                assertNull(ex.getNested().getMessage(), "ex.getNested().getMessage()");
            } else {
                assertEquals("java.lang.NullPointerException: Cannot invoke \"java.io.File.canRead()\" because \"this.file\" is null", ex.getMessage(), "ex.getMessage()");
                assertEquals("Cannot invoke \"java.io.File.canRead()\" because \"this.file\" is null", ex.getNested().getMessage(), "ex.getNested().getMessage()");
            }
            assertNull(simpleKeyFile.file, "simpleKeyFile.file");
            assertEquals(0, simpleKeyFile.props.size(), "simpleKeyFile.props.size()");
        }
    }

    @Test
    public void testSetConfigurationThrowsConfigurationException() throws Throwable {
        SimpleKeyFile simpleKeyFile = new SimpleKeyFile();
        Configuration cfg = new SimpleConfiguration(new Properties());
        try {
            simpleKeyFile.setConfiguration(cfg);
            fail("Expected ConfigurationException to be thrown");
        } catch (ConfigurationException ex) {
            assertEquals("", simpleKeyFile.file.getName(), "simpleKeyFile.file.getName()");
            assertEquals("Key File", simpleKeyFile.header, "simpleKeyFile.header");
            assertEquals(0, simpleKeyFile.props.size(), "simpleKeyFile.props.size()");
        }
    }

    @Test
    public void testSetKeyThrowsSecureKeyStoreException() throws Throwable {
        SimpleKeyFile simpleKeyFile = new SimpleKeyFile();
        try {
            simpleKeyFile.setKey("testSimpleKeyFileAlias", null);
            fail("Expected SecureKeyStoreException to be thrown");
        } catch (SecureKeyStore.SecureKeyStoreException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertEquals("java.lang.NullPointerException", ex.getMessage(), "ex.getMessage()");
                assertNull(ex.getNested().getMessage(), "ex.getNested().getMessage()");
            } else {
                assertEquals("java.lang.NullPointerException: Cannot invoke \"Object.getClass()\" because \"secureKey\" is null", ex.getMessage(), "ex.getMessage()");
                assertEquals("Cannot invoke \"Object.getClass()\" because \"secureKey\" is null", ex.getNested().getMessage(), "ex.getNested().getMessage()");
            }
            assertEquals(0, simpleKeyFile.props.size(), "simpleKeyFile.props.size()");
            assertNull(simpleKeyFile.file, "simpleKeyFile.file");
        }
    }

    @Test
    public void testSetLogger() throws Throwable {
        Logger logger = Logger.getLogger("testSimpleKeyFileName");
        SimpleKeyFile simpleKeyFile = new SimpleKeyFile();
        simpleKeyFile.setLogger(logger, "testSimpleKeyFileRealm");
        assertSame(logger, simpleKeyFile.logger, "simpleKeyFile.logger");
        assertEquals("testSimpleKeyFileRealm", simpleKeyFile.realm, "simpleKeyFile.realm");
    }

    @Test
    public void testSetProperty() throws Throwable {
        SimpleKeyFile simpleKeyFile = new SimpleKeyFile();
        simpleKeyFile.setProperty("testSimpleKeyFileAlias", "testSimpleKeyFileSubName", "testSimpleKeyFileValue");
        assertEquals(1, simpleKeyFile.props.size(), "simpleKeyFile.props.size()");
        assertEquals("testSimpleKeyFileValue", simpleKeyFile.props.get("testSimpleKeyFileAlias.testSimpleKeyFileSubName"),
                "simpleKeyFile.props.get(\"testSimpleKeyFileAlias.testSimpleKeyFileSubName\")");
    }

    @Test
    public void testSetPropertyThrowsNullPointerException() throws Throwable {
        SimpleKeyFile simpleKeyFile = new SimpleKeyFile();
        try {
            simpleKeyFile.setProperty("testSimpleKeyFileAlias", "testSimpleKeyFileSubName", null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull(ex.getMessage(), "ex.getMessage()");
            assertEquals(0, simpleKeyFile.props.size(), "simpleKeyFile.props.size()");
        }
    }

    @Test
    public void testStoreThrowsSecureKeyStoreException() throws Throwable {
        SimpleKeyFile simpleKeyFile = new SimpleKeyFile();
        try {
            simpleKeyFile.store();
            fail("Expected SecureKeyStoreException to be thrown");
        } catch (SecureKeyStore.SecureKeyStoreException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertEquals("java.lang.NullPointerException", ex.getMessage(), "ex.getMessage()");
                assertNull(ex.getNested().getMessage(), "ex.getNested().getMessage()");
            } else {
                assertEquals("java.lang.NullPointerException: Cannot invoke \"java.io.File.canWrite()\" because \"this.file\" is null", ex.getMessage(), "ex.getMessage()");
                assertEquals("Cannot invoke \"java.io.File.canWrite()\" because \"this.file\" is null", ex.getNested().getMessage(), "ex.getNested().getMessage()");
            }
            assertNull(simpleKeyFile.file, "simpleKeyFile.file");
        }
    }
}

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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Properties;

import org.jpos.core.Configuration;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.SubConfiguration;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.junit.jupiter.api.Test;

public class BaseSMAdapterTest {

    @Test
    public void testConstructor() throws Throwable {
        Configuration cfg = new SubConfiguration(new SimpleConfiguration(new Properties(null)), "testBaseSMAdapterPrefix");
        Logger logger = new Logger();
        BaseSMAdapter baseSMAdapter = new BaseSMAdapter(cfg, logger, "testBaseSMAdapterRealm");
        assertSame(cfg, baseSMAdapter.cfg, "baseSMAdapter.cfg");
        assertEquals("testBaseSMAdapterRealm", baseSMAdapter.realm, "baseSMAdapter.realm");
        assertSame(logger, baseSMAdapter.logger, "baseSMAdapter.logger");
    }

    @Test
    public void testConstructor1() throws Throwable {
        BaseSMAdapter baseSMAdapter = new BaseSMAdapter();
        assertNull(baseSMAdapter.realm, "baseSMAdapter.realm");
        assertNull(baseSMAdapter.logger, "baseSMAdapter.logger");
    }

    @Test
    public void testDecryptPINImplThrowsSMException() throws Throwable {
        BaseSMAdapter baseSMAdapter = new BaseSMAdapter();
        try {
            baseSMAdapter.decryptPINImpl(new EncryptedPIN());
            fail("Expected SMException to be thrown");
        } catch (SMException ex) {
            assertEquals("Operation not supported in: org.jpos.security.BaseSMAdapter", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.nested, "ex.nested");
            assertNull(ex.getNested(), "ex.getNested()");
        }
    }

    @Test
    public void testEncryptPINThrowsNullPointerException2() throws Throwable {
        try {
            new BaseSMAdapter().encryptPIN("testBaseSMAdapterPin", null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"s\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testExportKeyImplThrowsSMException() throws Throwable {
        BaseSMAdapter baseSMAdapter = new BaseSMAdapter();
        byte[] keyBytes = new byte[1];
        try {
            baseSMAdapter
                    .exportKeyImpl(new SecureDESKey((short) 100, "testBaseSMAdapterKeyType", keyBytes, "testString".getBytes()),
                            new SecureDESKey());
            fail("Expected SMException to be thrown");
        } catch (SMException ex) {
            assertEquals("Operation not supported in: org.jpos.security.BaseSMAdapter", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.nested, "ex.nested");
            assertNull(ex.getNested(), "ex.getNested()");
        }
    }

    @Test
    public void testExportPINImplThrowsSMException() throws Throwable {
        BaseSMAdapter baseSMAdapter = new BaseSMAdapter();
        try {
            baseSMAdapter.exportPINImpl(new EncryptedPIN(),
                    new SecureDESKey((short) 100, "testBaseSMAdapterKeyType", "testString".getBytes(), "".getBytes()), (byte) 0);
            fail("Expected SMException to be thrown");
        } catch (SMException ex) {
            assertEquals("Operation not supported in: org.jpos.security.BaseSMAdapter", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.nested, "ex.nested");
            assertNull(ex.getNested(), "ex.getNested()");
        }
    }

    @Test
    public void testGenerateCBC_MACImplThrowsSMException() throws Throwable {
        BaseSMAdapter jCESecurityModule = new BaseSMAdapter();
        byte[] data = new byte[2];
        try {
            jCESecurityModule.generateCBC_MACImpl(data, new SecureDESKey());
            fail("Expected SMException to be thrown");
        } catch (SMException ex) {
            assertEquals("Operation not supported in: org.jpos.security.BaseSMAdapter", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.nested, "ex.nested");
            assertNull(ex.getNested(), "ex.getNested()");
        }
    }

    @Test
    public void testGenerateCBC_MACThrowsSMException1() throws Throwable {
        assertThrows(SMException.class, () -> {
            new BaseSMAdapter().generateCBC_MAC(null, new SecureDESKey((short) 100, "testBaseSMAdapterKeyType",
                    "testBaseSMAdapterKeyHexString1", "testBaseSMAdapterKeyCheckValueHexString1"));
        });
    }

    @Test
    public void testGenerateKeyImplThrowsSMException() throws Throwable {
        BaseSMAdapter baseSMAdapter = new BaseSMAdapter();
        try {
            baseSMAdapter.generateKeyImpl((short) 100, "testBaseSMAdapterKeyType");
            fail("Expected SMException to be thrown");
        } catch (SMException ex) {
            assertEquals("Operation not supported in: org.jpos.security.BaseSMAdapter", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.nested, "ex.nested");
            assertNull(ex.getNested(), "ex.getNested()");
        }
    }

    @Test
    public void testGetLogger() throws Throwable {
        BaseSMAdapter baseSMAdapter = new BaseSMAdapter();
        Logger logger = Logger.getLogger("testBaseSMAdapterName");
        baseSMAdapter.setLogger(logger, "testBaseSMAdapterRealm");
        Logger result = baseSMAdapter.getLogger();
        assertSame(logger, result, "result");
    }

    @Test
    public void testGetRealm() throws Throwable {
        BaseSMAdapter jCESecurityModule = new BaseSMAdapter();
        jCESecurityModule.setLogger(new Logger(), "testBaseSMAdapterRealm");
        String result = jCESecurityModule.getRealm();
        assertEquals("testBaseSMAdapterRealm", result, "result");
    }

    @Test
    public void testGetSMAdapter() throws Throwable {
        BaseSMAdapter jCESecurityModule = new BaseSMAdapter();
        jCESecurityModule.setName("testString");
        BaseSMAdapter result = (BaseSMAdapter) BaseSMAdapter.getSMAdapter("testString");
        assertSame(jCESecurityModule, result, "result");
    }

    @Test
    public void testGetSMAdapterThrowsNotFoundException() throws Throwable {
        try {
            BaseSMAdapter.getSMAdapter("14CharactersXX");
            fail("Expected NotFoundException to be thrown");
        } catch (NameRegistrar.NotFoundException ex) {
            assertEquals("s-m-adapter.14CharactersXX", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testImportKeyImplThrowsSMException() throws Throwable {
        BaseSMAdapter baseSMAdapter = new BaseSMAdapter();
        byte[] encryptedKey = new byte[1];
        try {
            baseSMAdapter.importKeyImpl((short) 100, "testBaseSMAdapterKeyType", encryptedKey, new SecureDESKey(), true);
            fail("Expected SMException to be thrown");
        } catch (SMException ex) {
            assertEquals("Operation not supported in: org.jpos.security.BaseSMAdapter", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.nested, "ex.nested");
            assertNull(ex.getNested(), "ex.getNested()");
        }
    }

    @Test
    public void testImportKeyThrowsSMException2() throws Throwable {
        assertThrows(SMException.class, () -> {
            new BaseSMAdapter().importKey((short) 100, "testBaseSMAdapterKeyType", null, new SecureDESKey((short) 100,
                    "testBaseSMAdapterKeyType", "testBaseSMAdapterKeyHexString1", "testBaseSMAdapterKeyCheckValueHexString1"), true);
        });
    }

    @Test
    public void testImportPINImplThrowsSMException() throws Throwable {
        BaseSMAdapter baseSMAdapter = new BaseSMAdapter();
        try {
            baseSMAdapter.importPINImpl(new EncryptedPIN(), new SecureDESKey());
            fail("Expected SMException to be thrown");
        } catch (SMException ex) {
            assertEquals("Operation not supported in: org.jpos.security.BaseSMAdapter", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.nested, "ex.nested");
            assertNull(ex.getNested(), "ex.getNested()");
        }
    }

    @Test
    public void testImportPINImplThrowsSMException1() throws Throwable {
        BaseSMAdapter baseSMAdapter = new BaseSMAdapter(new SubConfiguration(new SimpleConfiguration(new Properties(null)),
                "testBaseSMAdapterPrefix"), new Logger(), "testBaseSMAdapterRealm");
        try {
            baseSMAdapter.importPINImpl(new EncryptedPIN("testBaseSMAdapterPinBlockHexString", (byte) 0,
                    "testBaseSMAdapterAccountNumber"), new KeySerialNumber(), new SecureDESKey((short) 100,
                    "testBaseSMAdapterKeyType", "testBaseSMAdapterKeyHexString1", "testBaseSMAdapterKeyCheckValueHexString1"));
            fail("Expected SMException to be thrown");
        } catch (SMException ex) {
            assertEquals("Operation not supported in: org.jpos.security.BaseSMAdapter", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.nested, "ex.nested");
            assertNull(ex.getNested(), "ex.getNested()");
        }
    }

    @Test
    public void testSetConfiguration() throws Throwable {
        BaseSMAdapter baseSMAdapter = new BaseSMAdapter();
        Configuration cfg = new SimpleConfiguration();
        baseSMAdapter.setConfiguration(cfg);
        assertSame(cfg, baseSMAdapter.cfg, "baseSMAdapter.cfg");
    }

    @Test
    public void testSetLogger() throws Throwable {
        BaseSMAdapter baseSMAdapter = new BaseSMAdapter();
        Logger logger = Logger.getLogger("testBaseSMAdapterName");
        baseSMAdapter.setLogger(logger, "testBaseSMAdapterRealm");
        assertSame(logger, baseSMAdapter.logger, "baseSMAdapter.logger");
        assertEquals("testBaseSMAdapterRealm", baseSMAdapter.realm, "baseSMAdapter.realm");
    }

    @Test
    public void testSetName() throws Throwable {
        BaseSMAdapter baseSMAdapter = new BaseSMAdapter();
        baseSMAdapter.setName("testBaseSMAdapterName");
        assertEquals("testBaseSMAdapterName", baseSMAdapter.getName(), "baseSMAdapter.getName()");
    }

    @Test
    public void testTranslatePINImplThrowsSMException() throws Throwable {
        BaseSMAdapter baseSMAdapter = new BaseSMAdapter();
        SecureDESKey bdk = new SecureDESKey();
        try {
            baseSMAdapter.translatePINImpl(new EncryptedPIN(), new KeySerialNumber(), bdk, bdk, (byte) 0);
            fail("Expected SMException to be thrown");
        } catch (SMException ex) {
            assertEquals("Operation not supported in: org.jpos.security.BaseSMAdapter", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.nested, "ex.nested");
            assertNull(ex.getNested(), "ex.getNested()");
        }
    }

    @Test
    public void testTranslatePINImplThrowsSMException1() throws Throwable {
        BaseSMAdapter baseSMAdapter = new BaseSMAdapter(new SubConfiguration(new SimpleConfiguration(new Properties(null)),
                "testBaseSMAdapterPrefix"), new Logger(), "testBaseSMAdapterRealm");
        try {
            baseSMAdapter.translatePINImpl(new EncryptedPIN(), new SecureDESKey((short) 100, "testBaseSMAdapterKeyType",
                    "testBaseSMAdapterKeyHexString1", "testBaseSMAdapterKeyCheckValueHexString1"), new SecureDESKey(), (byte) 0);
            fail("Expected SMException to be thrown");
        } catch (SMException ex) {
            assertEquals("Operation not supported in: org.jpos.security.BaseSMAdapter", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.nested, "ex.nested");
            assertNull(ex.getNested(), "ex.getNested()");
        }
    }

}

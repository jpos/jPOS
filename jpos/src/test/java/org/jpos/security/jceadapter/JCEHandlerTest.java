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
package org.jpos.security.jceadapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.security.Key;
import java.security.Provider;

import javax.crypto.spec.SecretKeySpec;

import org.junit.Before;
import org.junit.Test;

public class JCEHandlerTest {
    private JCEHandler jCEHandler;
    private Provider provider;

    @Before
    public void onSetup() {
        provider = mock(Provider.class);
        jCEHandler = new JCEHandler(provider);
    }

    @Test
    public void testConstructor() throws Throwable {
        assertEquals("jCEHandler.desPadding", "NoPadding", JCEHandler.DES_NO_PADDING);
        assertEquals("jCEHandler.desMode", "ECB", JCEHandler.DES_MODE_ECB);
        assertSame("jCEHandler.provider", provider, jCEHandler.provider);
    }

    @Test
    public void testConstructorThrowsJCEHandlerException() throws Throwable {
        try {
            new JCEHandler("testJCEHandlerJceProviderClassName");
            fail("Expected JCEHandlerException to be thrown");
        } catch (JCEHandlerException ex) {
            assertEquals("ex.getMessage()", "java.lang.ClassNotFoundException: testJCEHandlerJceProviderClassName", ex.getMessage());
            assertEquals("ex.getNested().getMessage()", "testJCEHandlerJceProviderClassName", ex.getNested().getMessage());
        }
    }

    @Test
    public void testDecryptDataThrowsJCEHandlerException() throws Throwable {
        jCEHandler = new JCEHandler((Provider) null);
        byte[] clearKeyBytes = new byte[1];
        Key key = jCEHandler.formDESKey((short) 192, clearKeyBytes);
        byte[] encryptedData = new byte[0];
        try {
            jCEHandler.decryptData(encryptedData, key);
            fail("Expected JCEHandlerException to be thrown");
        } catch (JCEHandlerException ex) {
            assertEquals("ex.getMessage()", "java.lang.NullPointerException", ex.getMessage());
            assertNull("ex.getNested().getMessage()", ex.getNested().getMessage());
            assertEquals("(SecretKeySpec) key.getAlgorithm()", "DESede", ((SecretKeySpec) key).getAlgorithm());
        }
    }

    @Test
    public void testDecryptDataThrowsNullPointerException() throws Throwable {
        try {
            new JCEHandler((Provider) null).decryptData("testString".getBytes(), null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testDecryptDESKeyThrowsJCEHandlerException() throws Throwable {
        jCEHandler = new JCEHandler((Provider) null);
        JCEHandler jCEHandler2 = new JCEHandler((Provider) null);
        byte[] clearKeyBytes = new byte[1];
        Key encryptingKey = jCEHandler2.formDESKey((short) 192, clearKeyBytes);
        byte[] encryptedDESKey = new byte[3];
        try {
            jCEHandler.decryptDESKey((short) 100, encryptedDESKey, encryptingKey, true);
            fail("Expected JCEHandlerException to be thrown");
        } catch (JCEHandlerException ex) {
            assertEquals("ex.getMessage()", "java.lang.NullPointerException", ex.getMessage());
            assertNull("ex.getNested().getMessage()", ex.getNested().getMessage());
            assertEquals("(SecretKeySpec) encryptingKey.getAlgorithm()", "DESede", ((SecretKeySpec) encryptingKey).getAlgorithm());
        }
    }

    @Test
    public void testDecryptDESKeyThrowsNullPointerException() throws Throwable {
        byte[] encryptedDESKey = new byte[2];
        try {
            new JCEHandler((Provider) null).decryptDESKey((short) 100, encryptedDESKey, null, true);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testDoCryptStuffThrowsJCEHandlerException() throws Throwable {
        jCEHandler = new JCEHandler((Provider) null);
        byte[] bytes = new byte[1];
        Key key = new SecretKeySpec(bytes, "testJCEHandlerParam2");
        byte[] data = new byte[3];
        try {
            jCEHandler.doCryptStuff(data, key, 100);
            fail("Expected JCEHandlerException to be thrown");
        } catch (JCEHandlerException ex) {
            assertEquals("ex.getMessage()", "java.lang.NullPointerException", ex.getMessage());
            assertNull("ex.getNested().getMessage()", ex.getNested().getMessage());
            assertEquals("(SecretKeySpec) key.getAlgorithm()", "testJCEHandlerParam2", ((SecretKeySpec) key).getAlgorithm());
        }
    }

    @Test
    public void testDoCryptStuffThrowsJCEHandlerException1() throws Throwable {
        byte[] clearKeyBytes = new byte[1];
        Key key = jCEHandler.formDESKey((short) 192, clearKeyBytes);
        JCEHandler jCEHandler2 = new JCEHandler((Provider) null);
        byte[] data = new byte[3];
        try {
            jCEHandler2.doCryptStuff(data, key, 100);
            fail("Expected JCEHandlerException to be thrown");
        } catch (JCEHandlerException ex) {
            assertEquals("ex.getMessage()", "java.lang.NullPointerException", ex.getMessage());
            assertNull("ex.getNested().getMessage()", ex.getNested().getMessage());
            assertEquals("(SecretKeySpec) key.getAlgorithm()", "DESede", ((SecretKeySpec) key).getAlgorithm());
        }
    }

    @Test
    public void testDoCryptStuffThrowsNullPointerException() throws Throwable {
        byte[] data = new byte[4];
        try {
            jCEHandler.doCryptStuff(data, null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testEncryptDataThrowsJCEHandlerException() throws Throwable {
        jCEHandler = new JCEHandler((Provider) null);
        JCEHandler jCEHandler2 = new JCEHandler((Provider) null);
        byte[] clearKeyBytes = new byte[1];
        Key key = jCEHandler2.formDESKey((short) 192, clearKeyBytes);
        byte[] data = new byte[1];
        try {
            jCEHandler.encryptData(data, key);
            fail("Expected JCEHandlerException to be thrown");
        } catch (JCEHandlerException ex) {
            assertEquals("ex.getMessage()", "java.lang.NullPointerException", ex.getMessage());
            assertNull("ex.getNested().getMessage()", ex.getNested().getMessage());
            assertEquals("(SecretKeySpec) key.getAlgorithm()", "DESede", ((SecretKeySpec) key).getAlgorithm());
        }
    }

    @Test
    public void testEncryptDataThrowsNullPointerException() throws Throwable {
        byte[] data = new byte[1];
        try {
            new JCEHandler((Provider) null).encryptData(data, null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testEncryptDESKeyThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] bytes = new byte[3];
        Key clearDESKey = new SecretKeySpec(bytes, 0, 1, "DESde");
        byte[] bytes2 = new byte[1];
        Key encryptingKey = new SecretKeySpec(bytes2, "testJCEHandlerParam2");
        try {
            new JCEHandler((Provider) null).encryptDESKey((short) 64, clearDESKey, encryptingKey);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("(SecretKeySpec) clearDESKey.getAlgorithm()", "DESde", ((SecretKeySpec) clearDESKey).getAlgorithm());
            assertEquals("(SecretKeySpec) encryptingKey.getAlgorithm()", "testJCEHandlerParam2",
                    ((SecretKeySpec) encryptingKey).getAlgorithm());
        }
    }

    @Test
    public void testEncryptDESKeyThrowsJCEHandlerException() throws Throwable {
        byte[] clearKeyBytes = new byte[1];
        Key clearDESKey = jCEHandler.formDESKey((short) 192, clearKeyBytes);
        try {
            new JCEHandler((Provider) null).encryptDESKey((short) 100, clearDESKey, clearDESKey);
            fail("Expected JCEHandlerException to be thrown");
        } catch (JCEHandlerException ex) {
            assertEquals("ex.getMessage()", "Unsupported key length: 100 bits", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
            assertEquals("(SecretKeySpec) clearDESKey.getAlgorithm()", "DESede", ((SecretKeySpec) clearDESKey).getAlgorithm());
        }
    }

    @Test
    public void testEncryptDESKeyThrowsNullPointerException() throws Throwable {
        byte[] bytes = new byte[1];
        Key encryptingKey = new SecretKeySpec(bytes, "testJCEHandlerParam2");
        try {
            new JCEHandler((Provider) null).encryptDESKey((short) 100, null, encryptingKey);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("(SecretKeySpec) encryptingKey.getAlgorithm()", "testJCEHandlerParam2",
                    ((SecretKeySpec) encryptingKey).getAlgorithm());
        }
    }

    @Test
    public void testExtractDESKeyMaterialThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] clearKeyBytes = new byte[1];
        Key clearDESKey = jCEHandler.formDESKey((short) 192, clearKeyBytes);
        JCEHandler jCEHandler2 = new JCEHandler((Provider) null);
        try {
            jCEHandler2.extractDESKeyMaterial((short) 128, clearDESKey);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("(SecretKeySpec) clearDESKey.getAlgorithm()", "DESede", ((SecretKeySpec) clearDESKey).getAlgorithm());
        }
    }

    @Test
    public void testExtractDESKeyMaterialThrowsJCEHandlerException() throws Throwable {
        JCEHandler jCEHandler2 = new JCEHandler((Provider) null);
        byte[] clearKeyBytes = new byte[1];
        Key clearDESKey = jCEHandler.formDESKey((short) 192, clearKeyBytes);
        try {
            jCEHandler2.extractDESKeyMaterial((short) 100, clearDESKey);
            fail("Expected JCEHandlerException to be thrown");
        } catch (JCEHandlerException ex) {
            assertEquals("ex.getMessage()", "Unsupported key length: 100 bits", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
            assertEquals("(SecretKeySpec) clearDESKey.getAlgorithm()", "DESede", ((SecretKeySpec) clearDESKey).getAlgorithm());
        }
    }

    @Test
    public void testExtractDESKeyMaterialThrowsJCEHandlerException1() throws Throwable {
        byte[] bytes = new byte[1];
        Key clearDESKey = new SecretKeySpec(bytes, "testJCEHandlerParam2");
        try {
            jCEHandler.extractDESKeyMaterial((short) 100, clearDESKey);
            fail("Expected JCEHandlerException to be thrown");
        } catch (JCEHandlerException ex) {
            assertEquals("ex.getMessage()", "Unsupported key algorithm: testJCEHandlerParam2", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
            assertEquals("(SecretKeySpec) clearDESKey.getAlgorithm()", "testJCEHandlerParam2",
                    ((SecretKeySpec) clearDESKey).getAlgorithm());
        }
    }

    @Test
    public void testExtractDESKeyMaterialThrowsNullPointerException() throws Throwable {
        try {
            jCEHandler.extractDESKeyMaterial((short) 100, null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testFormDESKey() throws Throwable {
        byte[] clearKeyBytes = new byte[4];
        SecretKeySpec result = (SecretKeySpec) jCEHandler.formDESKey((short) 64, clearKeyBytes);
        assertEquals("result.getAlgorithm()", "DES", result.getAlgorithm());
    }

    @Test
    public void testFormDESKey1() throws Throwable {
        byte[] clearKeyBytes = new byte[1];
        SecretKeySpec result = (SecretKeySpec) jCEHandler.formDESKey((short) 192, clearKeyBytes);
        assertEquals("result.getAlgorithm()", "DESede", result.getAlgorithm());
    }

    @Test
    public void testFormDESKeyThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] clearKeyBytes = new byte[1];
        try {
            jCEHandler.formDESKey((short) 128, clearKeyBytes);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testFormDESKeyThrowsIllegalArgumentException() throws Throwable {
        byte[] clearKeyBytes = new byte[0];
        try {
            jCEHandler.formDESKey((short) 192, clearKeyBytes);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException ex) {
            assertEquals("ex.getMessage()", "Empty key", ex.getMessage());
        }
    }

    @Test
    public void testFormDESKeyThrowsIllegalArgumentException1() throws Throwable {
        byte[] clearKeyBytes = new byte[0];
        try {
            jCEHandler.formDESKey((short) 64, clearKeyBytes);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException ex) {
            assertEquals("ex.getMessage()", "Empty key", ex.getMessage());
        }
    }

    @Test
    public void testFormDESKeyThrowsJCEHandlerException() throws Throwable {
        byte[] clearKeyBytes = new byte[1];
        try {
            jCEHandler.formDESKey((short) 100, clearKeyBytes);
            fail("Expected JCEHandlerException to be thrown");
        } catch (JCEHandlerException ex) {
            assertEquals("ex.getMessage()", "Unsupported DES key length: 100 bits", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
        }
    }

    @Test
    public void testGenerateDESKeyThrowsJCEHandlerException() throws Throwable {
        try {
            new JCEHandler((Provider) null).generateDESKey((short) 63);
            fail("Expected JCEHandlerException to be thrown");
        } catch (JCEHandlerException ex) {
            assertEquals("ex.getMessage()", "java.lang.NullPointerException", ex.getMessage());
            assertNull("ex.getNested().getMessage()", ex.getNested().getMessage());
        }
    }

    @Test
    public void testGenerateDESKeyThrowsJCEHandlerException1() throws Throwable {
        try {
            new JCEHandler((Provider) null).generateDESKey((short) 64);
            fail("Expected JCEHandlerException to be thrown");
        } catch (JCEHandlerException ex) {
            assertEquals("ex.getMessage()", "java.lang.NullPointerException", ex.getMessage());
            assertNull("ex.getNested().getMessage()", ex.getNested().getMessage());
        }
    }

    @Test
    public void testGenerateDESKeyThrowsJCEHandlerException2() throws Throwable {
        try {
            new JCEHandler((Provider) null).generateDESKey((short) 65);
            fail("Expected JCEHandlerException to be thrown");
        } catch (JCEHandlerException ex) {
            assertEquals("ex.getMessage()", "java.lang.NullPointerException", ex.getMessage());
            assertNull("ex.getNested().getMessage()", ex.getNested().getMessage());
        }
    }

    @Test
    public void testGetBytesLength() throws Throwable {
        jCEHandler.getBytesLength((short) 128);
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testGetBytesLength1() throws Throwable {
        jCEHandler.getBytesLength((short) 192);
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testGetBytesLength2() throws Throwable {
        int result = jCEHandler.getBytesLength((short) 64);
        assertEquals("result", 8, result);
    }

    @Test
    public void testGetBytesLengthThrowsJCEHandlerException() throws Throwable {
        try {
            jCEHandler.getBytesLength((short) 100);
            fail("Expected JCEHandlerException to be thrown");
        } catch (JCEHandlerException ex) {
            assertEquals("ex.getMessage()", "Unsupported key length: 100 bits", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
        }
    }
}

package org.jpos.security.jceadapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.crypto.spec.SecretKeySpec;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.SubConfiguration;
import org.jpos.security.EncryptedPIN;
import org.jpos.security.SMException;
import org.jpos.security.SecureDESKey;
import org.jpos.util.Logger;
import org.junit.Test;

public class JCESecurityModuleTest {

    @Test
    public void testCalculateKeyCheckValueThrowsNullPointerException() throws Throwable {
        JCESecurityModule jCESecurityModule = new JCESecurityModule();
        try {
            jCESecurityModule.calculateKeyCheckValue(new SecretKeySpec("testString".getBytes(), "testJCESecurityModuleParam2"));
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testConstructor() throws Throwable {
        JCESecurityModule jCESecurityModule = new JCESecurityModule();
        assertNull("jCESecurityModule.getRealm()", jCESecurityModule.getRealm());
        assertNull("jCESecurityModule.getLogger()", jCESecurityModule.getLogger());
    }

    @Test
    public void testConstructorThrowsConfigurationException() throws Throwable {
        try {
            new JCESecurityModule(new SimpleConfiguration(), Logger.getLogger("."), "testJCESecurityModuleRealm");
            fail("Expected ConfigurationException to be thrown");
        } catch (ConfigurationException ex) {
            assertTrue("Test completed without Exception", true);
            // dependencies on static and environment state led to removal of 4
            // assertions
        }
    }

    @Test
    public void testConstructorThrowsConfigurationException1() throws Throwable {
        Configuration cfg = new SimpleConfiguration();
        try {
            new JCESecurityModule(cfg, new Logger(), "testJCESecurityModuleRealm");
            fail("Expected ConfigurationException to be thrown");
        } catch (ConfigurationException ex) {
            // expected
        }
    }

    @Test
    public void testConstructorThrowsNullPointerException() throws Throwable {
        try {
            new JCESecurityModule(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testConstructorThrowsNullPointerException1() throws Throwable {
        try {
            new JCESecurityModule(null, "testJCESecurityModuleJceProviderClassName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testConstructorThrowsNullPointerException2() throws Throwable {
        Configuration cfg = new SubConfiguration();
        try {
            new JCESecurityModule(cfg, new Logger(), "testJCESecurityModuleRealm");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testConstructorThrowsSMException() throws Throwable {
        try {
            new JCESecurityModule("testJCESecurityModuleLmkFile");
            fail("Expected SMException to be thrown");
        } catch (SMException ex) {
            // expected
        }
    }

    @Test
    public void testConstructorThrowsSMException2() throws Throwable {
        try {
            new JCESecurityModule("testJCESecurityModuleLmkFile", "");
            fail("Expected SMException to be thrown");
        } catch (SMException ex) {
            // expected
        }
    }

    @Test
    public void testConstructorThrowsSMException3() throws Throwable {
        try {
            new JCESecurityModule("testJCESecurityModuleLmkFile", null);
            fail("Expected SMException to be thrown");
        } catch (SMException ex) {
            // expected
        }
    }

    @Test
    public void testDecryptPINImplThrowsNullPointerException1() throws Throwable {
        try {
            new JCESecurityModule().decryptPINImpl(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testEncryptPINImplThrowsNullPointerException5() throws Throwable {
        try {
            new JCESecurityModule().encryptPINImpl(null, "testJCESecurityModuleAccountNumber");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testEncryptPINImplThrowsSMException() throws Throwable {
        try {
            new JCESecurityModule().encryptPINImpl("11Character", "12Characters");
            fail("Expected SMException to be thrown");
        } catch (SMException ex) {
            assertEquals("ex.getMessage()", "Unsupported PIN Length: 11", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
        }
    }

    @Test
    public void testEncryptPINImplThrowsSMException1() throws Throwable {
        try {
            new JCESecurityModule().encryptPINImpl("11Character", "11Character");
            fail("Expected SMException to be thrown");
        } catch (SMException ex) {
            assertEquals(
                    "ex.getMessage()",
                    "Invalid Account Number: 11Character. The length of the account number must be 12 (the 12 right-most digits of the account number excluding the check digit)",
                    ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
        }
    }

    @Test
    public void testEncryptPINImplThrowsSMException2() throws Throwable {
        try {
            new JCESecurityModule().encryptPINImpl("11Character", "13CharactersX");
            fail("Expected SMException to be thrown");
        } catch (SMException ex) {
            assertEquals(
                    "ex.getMessage()",
                    "Invalid Account Number: 13CharactersX. The length of the account number must be 12 (the 12 right-most digits of the account number excluding the check digit)",
                    ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
        }
    }

    @Test
    public void testEncryptPINImplThrowsSMException3() throws Throwable {
        try {
            new JCESecurityModule().encryptPINImpl("12Characters", "12Characters");
            fail("Expected SMException to be thrown");
        } catch (SMException ex) {
            assertEquals("ex.getMessage()", "Unsupported PIN Length: 12", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
        }
    }

    @Test
    public void testEncryptPINImplThrowsSMException4() throws Throwable {
        try {
            new JCESecurityModule().encryptPINImpl("13CharactersX", "testJCESecurityModuleAccountNumber");
            fail("Expected SMException to be thrown");
        } catch (SMException ex) {
            assertEquals("ex.getMessage()", "Invalid PIN length: 13", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
        }
    }

    @Test
    public void testExportKeyImplThrowsNullPointerException() throws Throwable {
        try {
            new JCESecurityModule().exportKeyImpl(null, new SecureDESKey((short) 100, "testJCESecurityModuleKeyType",
                    "testJCESecurityModuleKeyHexString1", "testJCESecurityModuleKeyCheckValueHexString1"));
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testExportKeyImplThrowsNullPointerException1() throws Throwable {
        try {
            new JCESecurityModule().exportKeyImpl(new SecureDESKey((short) 100, "testJCESecurityModuleKeyType",
                    "testJCESecurityModuleKeyHexString1", "testJCESecurityModuleKeyCheckValueHexString1"), new SecureDESKey());
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testExportPINImplThrowsNullPointerException() throws Throwable {
        try {
            new JCESecurityModule().exportPINImpl(null, new SecureDESKey(), (byte) 0);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGenerateKeyImplThrowsNullPointerException() throws Throwable {
        try {
            new JCESecurityModule().generateKeyImpl((short) 100, "testJCESecurityModuleKeyType");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testImportKeyImplThrowsNullPointerException() throws Throwable {
        byte[] encryptedKey = new byte[2];
        try {
            new JCESecurityModule().importKeyImpl((short) 100, "testJCESecurityModuleKeyType", encryptedKey, new SecureDESKey(
                    (short) 100, "testJCESecurityModuleKeyType", "testJCESecurityModuleKeyHexString1",
                    "testJCESecurityModuleKeyCheckValueHexString1"), true);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testImportKeyImplThrowsNullPointerException1() throws Throwable {
        try {
            new JCESecurityModule().importKeyImpl((short) 100, "testJCESecurityModuleKeyType", ">".getBytes(), null, true);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testImportPINImplThrowsNullPointerException() throws Throwable {
        try {
            new JCESecurityModule().importPINImpl(new EncryptedPIN("testJCESecurityModulePinBlockHexString", (byte) 0,
                    "testJCESecurityModuleAccountNumber"), null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testImportPINImplThrowsNullPointerException1() throws Throwable {
        byte[] keyBytes = new byte[0];
        try {
            new JCESecurityModule().importPINImpl(new EncryptedPIN("testJCESecurityModulePinBlockHexString", (byte) 0,
                    "testJCESecurityModuleAccountNumber"), new SecureDESKey((short) 100, "testJCESecurityModuleKeyType", keyBytes,
                    "testString".getBytes()));
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testImportPINImplThrowsNullPointerException2() throws Throwable {
        try {
            new JCESecurityModule().importPINImpl(null, new SecureDESKey());
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSetConfigurationThrowsConfigurationException() throws Throwable {
        JCESecurityModule jCESecurityModule = new JCESecurityModule();
        Configuration cfg = new SimpleConfiguration();
        try {
            jCESecurityModule.setConfiguration(cfg);
            fail("Expected ConfigurationException to be thrown");
        } catch (ConfigurationException ex) {
            // expected
        }
    }

    @Test
    public void testSetConfigurationThrowsConfigurationException1() throws Throwable {
        JCESecurityModule jCESecurityModule = new JCESecurityModule();
        jCESecurityModule.setLogger(Logger.getLogger("."), "testJCESecurityModuleRealm");
        try {
            jCESecurityModule.setConfiguration(new SimpleConfiguration());
            fail("Expected ConfigurationException to be thrown");
        } catch (ConfigurationException ex) {
            assertTrue("Test completed without Exception", true);
            // dependencies on static and environment state led to removal of 8
            // assertions
        }
    }

    @Test
    public void testSetConfigurationThrowsNullPointerException() throws Throwable {
        JCESecurityModule jCESecurityModule = new JCESecurityModule();
        Configuration cfg = new SubConfiguration();
        try {
            jCESecurityModule.setConfiguration(cfg);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testTranslatePINImplThrowsNullPointerException() throws Throwable {
        try {
            new JCESecurityModule().translatePINImpl(new EncryptedPIN("testString".getBytes(), (byte) 0,
                    "testJCESecurityModuleAccountNumber"), null, new SecureDESKey(), (byte) 0);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testTranslatePINImplThrowsNullPointerException1() throws Throwable {
        byte[] keyBytes = new byte[0];
        try {
            new JCESecurityModule().translatePINImpl(new EncryptedPIN(), new SecureDESKey((short) 100,
                    "testJCESecurityModuleKeyType", keyBytes, "testString".getBytes()), new SecureDESKey(), (byte) 0);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testTranslatePINImplThrowsNullPointerException2() throws Throwable {
        byte[] keyBytes = new byte[0];
        try {
            new JCESecurityModule().translatePINImpl(null, new SecureDESKey((short) 100, "testJCESecurityModuleKeyType", keyBytes,
                    "testString".getBytes()), new SecureDESKey(), (byte) 0);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}

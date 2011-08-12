package org.jpos.security.jceadapter;

import static org.junit.Assert.*;

import javax.crypto.spec.SecretKeySpec;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.SubConfiguration;
import org.jpos.iso.ISOUtil;
import org.jpos.security.EncryptedPIN;
import org.jpos.security.SMAdapter;
import org.jpos.security.SMException;
import org.jpos.security.SecureDESKey;
import org.jpos.util.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

public class JCESecurityModuleTest {

    static JCESecurityModule jcesecmod;

    /**
     * Encrypted under standard LMK test key: keytype 001 ZMK (variant 0, scheme U)
     * Clear key value: 4 times 12345678
     */
    static SecureDESKey zpk = new SecureDESKey(SMAdapter.LENGTH_DES3_2KEY
          ,SMAdapter.TYPE_ZPK+":0U","34E2FC8EAD7CD07BFA2B7ED5FE4D8212" ,"6FB1");

    /**
     * Encrypted under standard LMK test key: keytype 002 TPK (variant 0, scheme U)
     * Clear key value: 4 times 12345678
     */
    static SecureDESKey tpk = new SecureDESKey(SMAdapter.LENGTH_DES3_2KEY
          ,SMAdapter.TYPE_TPK+":0U","E9F05D2F2DB8A8579CA3E806B35E336F" ,"6FB1");


    private static final String PREFIX = "src/main/resources/cfg/";

    @BeforeClass
    public static void setUpClass() throws Exception {
      jcesecmod = new JCESecurityModule(PREFIX+"secret.lmk");
    }

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
    public void testDecryptPINImpl() throws Throwable {
        EncryptedPIN ep  =  new EncryptedPIN("E0F7E27FF5DA09A9",(byte)0, "12Characters");
        ep.setAccountNumber("12Characters");
        String pin = jcesecmod.decryptPINImpl(ep);
        String expected = "123456789012";
        assertEquals(expected, pin);
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
    public void testExportPINImpl() throws Throwable {
        EncryptedPIN ep = jcesecmod.encryptPINImpl("1234", "123456789012");
        EncryptedPIN pinUnderZPK = jcesecmod.exportPINImpl(ep, zpk, SMAdapter.FORMAT01);
        byte[] expected = ISOUtil.hex2byte("3C0CA40863092C3A");
        assertArrayEquals(expected, pinUnderZPK.getPINBlock());
        assertEquals(SMAdapter.FORMAT01, pinUnderZPK.getPINBlockFormat());
        assertEquals("123456789012", pinUnderZPK.getAccountNumber());
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
    public void testImportPINImpl() throws Throwable {
        EncryptedPIN pinUnderKd1 = new EncryptedPIN("3C0CA40863092C3A", SMAdapter.FORMAT01,"1234567890120");
        EncryptedPIN ep = jcesecmod.importPINImpl(pinUnderKd1, zpk);
        String pin = jcesecmod.decryptPINImpl(ep);
        assertEquals("1234", pin);
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

    @Test
    public void testTranslatePINImpl() throws Throwable {
        EncryptedPIN pinUnderZPK = new EncryptedPIN("3C0CA40863092C3A", SMAdapter.FORMAT01,"1234567890120");
        EncryptedPIN pinUnderTPK = jcesecmod.translatePINImpl(pinUnderZPK, zpk, tpk, SMAdapter.FORMAT01);
        //Clear keys are that same so after translation expected result must be unchanged
        byte[] expected = ISOUtil.hex2byte("3C0CA40863092C3A");
        assertArrayEquals(expected, pinUnderTPK.getPINBlock());
        assertEquals(SMAdapter.FORMAT01, pinUnderTPK.getPINBlockFormat());
        assertEquals("123456789012", pinUnderTPK.getAccountNumber());
    }
}

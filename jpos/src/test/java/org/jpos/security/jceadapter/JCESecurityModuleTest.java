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

import java.util.Date;
import static org.junit.Assert.*;

import javax.crypto.spec.SecretKeySpec;

import org.javatuples.Pair;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.SubConfiguration;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOUtil;
import org.jpos.security.EncryptedPIN;
import org.jpos.security.MKDMethod;
import org.jpos.security.PaddingMethod;
import org.jpos.security.SKDMethod;
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

    /**
     * Encrypted under standard LMK test key: keytype 002 PVK (variant 0, scheme Z)
     * Clear key value: 2 times 12345678
     */
    static SecureDESKey pvkA = new SecureDESKey(SMAdapter.LENGTH_DES
          ,SMAdapter.TYPE_PVK+":0Z","141E1DA3D2D7F3F4" ,"6FB1");

    /**
     * Encrypted under standard LMK test key: keytype 002 PVK (variant 0, scheme Z)
     * Clear key value: 2 times 12345678
     */
    static SecureDESKey pvkB = new SecureDESKey(SMAdapter.LENGTH_DES
          ,SMAdapter.TYPE_PVK+":0Z","141E1DA3D2D7F3F4" ,"6FB1");

    /**
     * Encrypted under standard LMK test key: keytype 402 CVK (variant 4, scheme U)
     * Clear key value: 4 times 12345678
     */
    static SecureDESKey cvk = new SecureDESKey(SMAdapter.LENGTH_DES3_2KEY
          ,SMAdapter.TYPE_CVK+":4U","479D751977AA598CB481F548226DBF2B" ,"6FB1");

    /**
     * Encrypted under standard LMK test key: keytype 402 CVK (variant 4, scheme Z)
     * Clear key value: 2 times 12345678
     */
    static SecureDESKey cvkA = new SecureDESKey(SMAdapter.LENGTH_DES
          ,SMAdapter.TYPE_CVK+":4Z","56FBB74CDEAD6949" ,"6FB1");

    /**
     * Encrypted under standard LMK test key: keytype 402 CVK (variant 4, scheme Z)
     * Clear key value: 2 times 87654321
     */
    static SecureDESKey cvkB = new SecureDESKey(SMAdapter.LENGTH_DES
          ,SMAdapter.TYPE_CVK+":4Z","69E636CF27A47EEE" ,"AAB1");

    /**
     * Encrypted under standard LMK test key: keytype 109 MK-AC (variant 1, scheme U)
     * Clear key value: 4 times 12345678
     */
    static SecureDESKey imkac = new SecureDESKey(SMAdapter.LENGTH_DES3_2KEY
          ,SMAdapter.TYPE_MK_AC+":1U","0D39A43C864D1B40F33998B80BB02C95" ,"6FB1");

    /**
     * Encrypted under standard LMK test key: keytype 702 MK-SMI (variant 2, scheme U)
     * Clear key value: 4 times 12345678
     */
    static SecureDESKey imksmi = new SecureDESKey(SMAdapter.LENGTH_DES3_2KEY
          ,SMAdapter.TYPE_MK_SMI+":2U","E86D8A2FC81DEC4E91F9FE76EDAF3C3B" ,"6FB1");

    /**
     * Encrypted under standard LMK test key: keytype 703 MK-SMC (variant 3, scheme U)
     * Clear key value: 4 times 12345678
     */
    static SecureDESKey imksmc = new SecureDESKey(SMAdapter.LENGTH_DES3_2KEY
          ,SMAdapter.TYPE_MK_SMC+":3U","9ED29EDD0BA8B771106EB77D819F7394" ,"6FB1");

    /**
     * Encrypted under standard LMK test key: keytype 709 MK-CVC3 (variant 7, scheme U)
     * Clear key value: 4 times 12345678
     */
    static SecureDESKey imkcvc3 = new SecureDESKey(SMAdapter.LENGTH_DES3_2KEY
          ,SMAdapter.TYPE_MK_CVC3+":7U","DBD2D13CCA57AAAF3E477E0646EF10C9" ,"6FB1");

    /**
     * Pin value 1234 for account number 1234567890123 encrypted
     * under LMK in internal pinblock format
     */
    static EncryptedPIN pinUnderLMK;

    /**
     * Pin value 1234 for account number 1234567890123 encrypted
     * under TPK in pinblock 01 format
     */
    static EncryptedPIN pinUnderTPK;

    /**
     * Pin value 1234 for account number 1234567890123456 encrypted
     * under ZPK in pinblock 01 format
     */
    static EncryptedPIN pinUnderZPK;

    /**
     * 16 digits account number
     */
    static final String accountNoA = "1234567890123456";

    /**
     * Card Sequence Number
     */
    static final String accountNoA_CSN = "00";
    
    /**
     * Application Transaction Counter
     */
    static final byte[] atc01 = ISOUtil.hex2byte("0002");

    static final byte[] arqc01 = ISOUtil.hex2byte("4B07E35A87F27D2E");

    /**
     * APDU Header. Part of data for Secure Messaging MAC algorrithms
     */
    static final byte[] apdu01 = ISOUtil.hex2byte("8424000210");

    private static final String PREFIX = "src/main/resources/cfg/";

    @BeforeClass
    public static void setUpClass() throws Exception {
      jcesecmod = new JCESecurityModule(PREFIX+"secret.lmk");
      pinUnderLMK = jcesecmod.encryptPIN("1234", "1234567890123");
      pinUnderTPK = jcesecmod.exportPINImpl(pinUnderLMK, tpk, SMAdapter.FORMAT01);
      pinUnderZPK  =  new EncryptedPIN("ABE38E29B58EA392", SMAdapter.FORMAT01, accountNoA);
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
            assertEquals("ex.getMessage()", "Invalid PIN decimal digits: 11Character", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
        }
    }

    @Test
    public void testEncryptPINImplThrowsSMException1() throws Throwable {
        try {
            jcesecmod.encryptPINImpl("12345678901", "11Character");
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
            jcesecmod.encryptPINImpl("12345678901", "13CharactersX");
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
            jcesecmod.encryptPINImpl("1234567890123", "12Characters");
            fail("Expected SMException to be thrown");
        } catch (SMException ex) {
            assertEquals("ex.getMessage()", "Invalid PIN length: 13", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
        }
    }

    @Test
    public void testEncryptPINImplThrowsSMException4() throws Throwable {
        try {
            jcesecmod.encryptPINImpl("1234567890123", "testJCESecurityModuleAccountNumber");
            fail("Expected SMException to be thrown");
        } catch (SMException ex) {
            assertEquals("ex.getMessage()", "Invalid PIN length: 13", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
        }
    }

    @Test
    public void testEncryptPINImpl1() throws Throwable {
        EncryptedPIN ep = jcesecmod.encryptPINImpl("123456789012", "12Characters");
        byte[] expected = ISOUtil.hex2byte("E0F7E27FF5DA09A9");
        assertArrayEquals(expected, ep.getPINBlock());
        assertEquals(SMAdapter.FORMAT00, ep.getPINBlockFormat());
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

    @Test
    public void testCalculateCVVImpl1() throws Throwable {
        String accountNo = "123456789012";
        Date expDate = ISODate.parseISODate("1108"+"01000000");
        String serviceCode = "000";
        String expected = "204";
        String cvv = jcesecmod.calculateCVV(accountNo, cvk, null, expDate, serviceCode);
        assertEquals(expected, cvv);
    }

    @Test
    public void testVerifyCVVImpl1() throws Throwable {
        String accountNo = "123456789012";
        Date expDate = ISODate.parseISODate("1108"+"01000000");
        String serviceCode = "000";
        String cvv = "204";
        boolean result = jcesecmod.verifyCVV(accountNo, cvk, null, cvv, expDate, serviceCode);
        assertTrue(result);
    }

    @Test
    public void testCalculateCVVImpl2() throws Throwable {
        String accountNo = "123456789012";
        Date expDate = ISODate.parseISODate("1108"+"01000000");
        String serviceCode = "000";
        String expected = "453";
        String cvv = jcesecmod.calculateCVV(accountNo, cvkA, cvkB, expDate, serviceCode);
        assertEquals(expected, cvv);
    }

    @Test
    public void testVerifyCVVImpl2() throws Throwable {
        String accountNo = "123456789012";
        Date expDate = ISODate.parseISODate("1108"+"01000000");
        String serviceCode = "000";
        String cvv = "453";
        boolean result = jcesecmod.verifyCVV(accountNo, cvkA, cvkB, cvv, expDate, serviceCode);
        assertTrue(result);
    }

    @Test
    public void testCalculatePVVImpl1() throws Throwable {
        SecureDESKey pvk = tpk; //pvk and zpk are same type
        int pvki = 0;
        String pvv = jcesecmod.calculatePVV(pinUnderLMK, pvk, null, pvki);
        String expected = "1226";
        assertEquals(expected, pvv);
    }

    @Test
    public void testVerifyPVVImpl1() throws Throwable {
        SecureDESKey pvk = tpk; //pvk and zpk are same type
        int pvki = 0;
        String pvv = "1226";
        boolean result = jcesecmod.verifyPVV(pinUnderTPK, tpk, pvk, null, pvki, pvv);
        assertTrue(result);
    }

    @Test
    public void testCalculatePVVImpl2() throws Throwable {
        int pvki = 0;
        String pvv = jcesecmod.calculatePVV(pinUnderLMK, pvkA, pvkB, pvki);
        String expected = "1226";
        assertEquals(expected, pvv);
    }

    @Test
    public void testVerifyPVVImpl2() throws Throwable {
        int pvki = 0;
        String pvv = "1226";
        boolean result = jcesecmod.verifyPVV(pinUnderTPK, tpk, pvkA, pvkB, pvki, pvv);
        assertTrue(result);
    }

    @Test
    public void testVerifyDCVVImpl1() throws Throwable {
        String accountNo = "1234567890123456";
        Date expDate = ISODate.parseISODate("1310"+"01000000");
        String serviceCode = "226";
        String dcvv = "422";
        byte[] atc = ISOUtil.hex2byte("3210");
        boolean result = jcesecmod.verifydCVV(accountNo, imkac, dcvv, expDate
                        ,serviceCode, atc, MKDMethod.OPTION_A);
        assertTrue(result);
    }

    @Test
    public void testVerifyDCVVImpl2() throws Throwable {
        String accountNo = "123456789012";
        Date expDate = ISODate.parseISODate("1310"+"01000000");
        String serviceCode = "226";
        String dcvv = "719";
        byte[] atc = ISOUtil.hex2byte("3210");
        boolean result = jcesecmod.verifydCVV(accountNo, imkac, dcvv, expDate
                        ,serviceCode, atc, MKDMethod.OPTION_A);
        assertTrue(result);
    }

    @Test
    public void testVerifyDCVVImpl3() throws Throwable {
        String accountNo = "123456789012";
        Date expDate = ISODate.parseISODate("1310"+"01000000");
        String serviceCode = "226";
        String dcvv = "824";
        byte[] atc = ISOUtil.hex2byte("3210");
        boolean result = jcesecmod.verifydCVV(accountNo, imkac, dcvv, expDate
                        ,serviceCode, atc, MKDMethod.OPTION_B);
        assertTrue(result);
    }

    @Test
    public void testVerifyDCVVImpl4() throws Throwable {
        String accountNo = "1234567890123456789";
        Date expDate = ISODate.parseISODate("1310"+"01000000");
        String serviceCode = "226";
        String dcvv = "562";
        byte[] atc = ISOUtil.hex2byte("3210");
        boolean result = jcesecmod.verifydCVV(accountNo, imkac, dcvv, expDate
                        ,serviceCode, atc, MKDMethod.OPTION_B);
        assertTrue(result);
    }

    @Test
    public void testVerifyDCVVImplException1() throws Throwable {
        String accountNo = "";
        Date expDate = ISODate.parseISODate("1310"+"01000000");
        String serviceCode = "226";
        String dcvv = "562";
        byte[] atc = ISOUtil.hex2byte("3210");
        try {
            jcesecmod.verifydCVV(accountNo, imkac, dcvv, expDate
                        ,serviceCode, atc, MKDMethod.OPTION_A);
            fail("Expected SMException to be thrown");
        } catch (SMException ex){
            assertEquals("ex.getMessage()", "String index out of range: -4", ex.getNested().getMessage());
        }
    }

    @Test
    public void testVerifyDCVVImplException2() throws Throwable {
        String accountNo = null;
        Date expDate = ISODate.parseISODate("1310"+"01000000");
        String serviceCode = "226";
        String dcvv = "562";
        byte[] atc = ISOUtil.hex2byte("3210");
        try {
            jcesecmod.verifydCVV(accountNo, imkac, dcvv, expDate
                        ,serviceCode, atc, MKDMethod.OPTION_A);
            fail("Expected SMException to be thrown");
        } catch (SMException ex){
            assertNull("ex.getMessage()", ex.getNested().getMessage());
        }
    }

    @Test
    public void testVerifyCVC3Impl1() throws Throwable {
        String accountNo = "1234567890123456";
        String accntSeqNo = "00";
        byte[] data = ISOUtil.hex2byte("1234567890123456D12012061000110000000F");
        String cvc3 = "464";
        byte[] atc = ISOUtil.hex2byte("2710");
        byte[] upn = ISOUtil.hex2byte("00002710");
        boolean result = jcesecmod.verifyCVC3(imkcvc3, accountNo, accntSeqNo, atc
                        ,upn, data, MKDMethod.OPTION_A, cvc3);
        assertTrue(result);
    }

    @Test
    public void testVerifyCVC3Impl2() throws Throwable {
        String accountNo = "1234567890123456";
        String accntSeqNo = "00";
        byte[] data = ISOUtil.hex2byte("1234567890123456D12012061000110000000FFFFFFFFFFF");
        String cvc3 = "45423";
        byte[] atc = ISOUtil.hex2byte("2710");
        byte[] upn = ISOUtil.hex2byte("00002710");
        boolean result = jcesecmod.verifyCVC3(imkcvc3, accountNo, accntSeqNo, atc
                        ,upn, data, MKDMethod.OPTION_A, cvc3);
        assertTrue(result);
    }

    @Test
    public void testVerifyCVC3Impl3() throws Throwable {
        String accountNo = "1234567890123456";
        String accntSeqNo = "00";
        byte[] ivcvc3 = ISOUtil.hex2byte("CD76");
        String cvc3 = "39464";
        byte[] atc = ISOUtil.hex2byte("2710");
        byte[] upn = ISOUtil.hex2byte("00002710");
        boolean result = jcesecmod.verifyCVC3(imkcvc3, accountNo, accntSeqNo, atc
                        ,upn, ivcvc3, MKDMethod.OPTION_A, cvc3);
        assertTrue(result);
    }

    @Test
    public void testVerifyCVC3Impl4() throws Throwable {
        String accountNo = "1234567890123456";
        String accntSeqNo = "00";
        byte[] data = ISOUtil.hex2byte("1234567890123456D12012061000110000000F");
        String cvc3 = "03518";
        byte[] atc = ISOUtil.hex2byte("2710");
        byte[] upn = ISOUtil.hex2byte("00002710");
        boolean result = jcesecmod.verifyCVC3(imkcvc3, accountNo, accntSeqNo, atc
                        ,upn, data, MKDMethod.OPTION_B, cvc3);
        assertTrue(result);
    }

    @Test
    public void testVerifyCVC3Impl5() throws Throwable {
        String accountNo = "123456789012345";
        String accntSeqNo = "00";
        byte[] data = ISOUtil.hex2byte("123456789012345D12012061000110000000");
        String cvc3 = "12612";
        byte[] atc = ISOUtil.hex2byte("2710");
        byte[] upn = ISOUtil.hex2byte("00002710");
        boolean result = jcesecmod.verifyCVC3(imkcvc3, accountNo, accntSeqNo, atc
                        ,upn, data, MKDMethod.OPTION_B, cvc3);
        assertTrue(result);
    }

    @Test
    public void testVerifyCVC3ImplException6() throws Throwable {
        String accountNo = "1234567890123456";
        String accntSeqNo = "00";
        byte[] data = ISOUtil.hex2byte("123456789012345D12012061000110000000");
        String cvc3 = null;
        byte[] atc = ISOUtil.hex2byte("2710");
        byte[] upn = ISOUtil.hex2byte("00002710");
        boolean result = jcesecmod.verifyCVC3(imkcvc3, accountNo, accntSeqNo, atc
                        ,upn, data, MKDMethod.OPTION_A, cvc3);
        assertTrue(result);
    }

    @Test
    public void testVerifyCVC3ImplException2() throws Throwable {
        String accountNo = null;
        String accntSeqNo = "00";
        byte[] data = ISOUtil.hex2byte("123456789012345D12012061000110000000");
        String cvc3 = "12612";
        byte[] atc = ISOUtil.hex2byte("2710");
        byte[] upn = ISOUtil.hex2byte("00002710");
        try {
            jcesecmod.verifyCVC3(imkcvc3, accountNo, accntSeqNo, atc
            ,upn, data, MKDMethod.OPTION_A, cvc3);
            fail("Expected SMException to be thrown");
        } catch (SMException ex){
            assertNull("ex.getMessage()", ex.getNested().getMessage());
        }
    }

    @Test
    public void testGenerateSM_MACImpl1() throws Throwable {
        String accountNo = accountNoA;
        String accntSeqNo = accountNoA_CSN;
        byte[] apdu = apdu01;
        byte[] atc = atc01;
        byte[] arqc = arqc01;
        byte[] data  = ISOUtil.hex2byte("1122334455667788");
        apdu = ISOUtil.concat(apdu, atc);
        apdu = ISOUtil.concat(apdu, arqc);
        apdu = ISOUtil.concat(apdu, data);
        byte[] result = jcesecmod.generateSM_MAC(MKDMethod.OPTION_A, SKDMethod.MCHIP
                        ,imksmi, accountNo, accntSeqNo, atc, arqc, apdu);
        assertArrayEquals(ISOUtil.hex2byte("217CF53EA0E7C327"), result);
    }

    @Test
    public void testGenerateSM_MACImpl2() throws Throwable {
        String accountNo = accountNoA;
        String accntSeqNo = accountNoA_CSN;
        byte[] apdu = apdu01;
        byte[] atc = atc01;
        byte[] arqc = arqc01;
        byte[] data  = ISOUtil.hex2byte("11");
        apdu = ISOUtil.concat(apdu, atc);
        apdu = ISOUtil.concat(apdu, arqc);
        apdu = ISOUtil.concat(apdu, data);
        byte[] result = jcesecmod.generateSM_MAC(MKDMethod.OPTION_A, SKDMethod.MCHIP
                        ,imksmi, accountNo, accntSeqNo, atc, arqc, apdu);
        assertArrayEquals(ISOUtil.hex2byte("5E14A5A5C4B98C0C"), result);
    }

    @Test
    public void testGenerateSM_MACImpl3() throws Throwable {
        String accountNo = accountNoA;
        String accntSeqNo = accountNoA_CSN;
        byte[] apdu = apdu01;
        byte[] atc = atc01;
        byte[] arqc = arqc01;
        byte[] data  = ISOUtil.hex2byte("1122334455667788");
        apdu = ISOUtil.concat(apdu, atc);
        apdu = ISOUtil.concat(apdu, arqc);
        apdu = ISOUtil.concat(apdu, data);
        byte[] result = jcesecmod.generateSM_MAC(MKDMethod.OPTION_A, SKDMethod.VSDC
                        ,imksmi, accountNo, accntSeqNo, atc, arqc, apdu);
        assertArrayEquals(ISOUtil.hex2byte("E218CC0B7FEC6876"), result);
    }

    @Test
    public void testGenerateSM_MACImpl4() throws Throwable {
        String accountNo = accountNoA;
        String accntSeqNo = accountNoA_CSN;
        byte[] apdu = apdu01;
        byte[] atc = atc01;
        byte[] arqc = arqc01;
        byte[] data  = ISOUtil.hex2byte("11");
        apdu = ISOUtil.concat(apdu, atc);
        apdu = ISOUtil.concat(apdu, arqc);
        apdu = ISOUtil.concat(apdu, data);
        byte[] result = jcesecmod.generateSM_MAC(MKDMethod.OPTION_A, SKDMethod.VSDC
                        ,imksmi, accountNo, accntSeqNo, atc, arqc, apdu);
        System.out.println(ISOUtil.hexString(result));
        assertArrayEquals(ISOUtil.hex2byte("C1F2C04136BD48E6"), result);
    }


    @Test
    public void testTranslatePINGenerateSM_MACImpl1() throws Throwable {
        String accountNo = accountNoA;
        String accntSeqNo = accountNoA_CSN;
        byte[] apdu = apdu01;
        byte[] atc = atc01;
        byte[] arqc = arqc01;
        EncryptedPIN pin  =  pinUnderZPK;
        apdu = ISOUtil.concat(apdu, atc);
        apdu = ISOUtil.concat(apdu, arqc);
        EncryptedPIN expectdPIN = new EncryptedPIN("F473D25D9B478970", SMAdapter.FORMAT34, accountNo);
        Pair<EncryptedPIN, byte[]> result = jcesecmod.translatePINGenerateSM_MAC(
                         MKDMethod.OPTION_A, SKDMethod.MCHIP, null
                        ,imksmi, accountNo, accntSeqNo, atc, arqc, apdu
                        ,null, pin, zpk, imksmc, null, SMAdapter.FORMAT34);
        assertArrayEquals(expectdPIN.getPINBlock(), result.getValue0().getPINBlock());
        assertArrayEquals(ISOUtil.hex2byte("831B043B4A314FD2"), result.getValue1());
    }

    @Test
    public void testTranslatePINGenerateSM_MACImpl2() throws Throwable {
        String accountNo = accountNoA;
        String accntSeqNo = accountNoA_CSN;
        byte[] apdu = apdu01;
        byte[] atc = atc01;
        byte[] arqc = arqc01;
        EncryptedPIN pin  =  pinUnderZPK;
        apdu = ISOUtil.concat(apdu, atc);
        apdu = ISOUtil.concat(apdu, arqc);
        EncryptedPIN expectdPIN = new EncryptedPIN("E60663E4B11CDB2DE4667CC9433384B4", SMAdapter.FORMAT41, accountNo);
        Pair<EncryptedPIN, byte[]> result = jcesecmod.translatePINGenerateSM_MAC(
                         MKDMethod.OPTION_A, SKDMethod.EMV_CSKD, null
                        ,imksmi, accountNo, accntSeqNo, atc, arqc, apdu
                        ,null, pin, zpk, imksmc, imkac, SMAdapter.FORMAT41);
        assertArrayEquals(expectdPIN.getPINBlock(), result.getValue0().getPINBlock());
        assertArrayEquals(ISOUtil.hex2byte("0405DB9BFB25BE6F"), result.getValue1());
    }

    @Test
    public void testTranslatePINGenerateSM_MACImpl3() throws Throwable {
        String accountNo = accountNoA;
        String accntSeqNo = accountNoA_CSN;
        byte[] apdu = apdu01;
        byte[] atc = atc01;
        byte[] arqc = arqc01;
        EncryptedPIN pin  =  pinUnderZPK;
        apdu = ISOUtil.concat(apdu, atc);
        apdu = ISOUtil.concat(apdu, arqc);
        EncryptedPIN expectdPIN = new EncryptedPIN("158C4C2E67041975DEB907E2E57EC85D", SMAdapter.FORMAT41, accountNo);
        Pair<EncryptedPIN, byte[]> result = jcesecmod.translatePINGenerateSM_MAC(
                         MKDMethod.OPTION_A, SKDMethod.EMV_CSKD, PaddingMethod.VSDC
                        ,imksmi, accountNo, accntSeqNo, atc, arqc, apdu
                        ,null, pin, zpk, imksmc, imkac, SMAdapter.FORMAT41);
        assertArrayEquals(expectdPIN.getPINBlock(), result.getValue0().getPINBlock());
        assertArrayEquals(ISOUtil.hex2byte("7DE6117DEB56D37F"), result.getValue1());
    }

    @Test
    public void testTranslatePINGenerateSM_MACImpl4() throws Throwable {
        String accountNo = accountNoA;
        String accntSeqNo = accountNoA_CSN;
        byte[] apdu = apdu01;
        byte[] atc = atc01;
        byte[] arqc = arqc01;
        EncryptedPIN pin  =  pinUnderZPK;
        apdu = ISOUtil.concat(apdu, atc);
        apdu = ISOUtil.concat(apdu, arqc);
        EncryptedPIN expectdPIN = new EncryptedPIN("F473D25D9B478970E72651C08FE487EF", SMAdapter.FORMAT34, accountNo);
        Pair<EncryptedPIN, byte[]> result = jcesecmod.translatePINGenerateSM_MAC(
                         MKDMethod.OPTION_A, SKDMethod.EMV_CSKD, null
                        ,imksmi, accountNo, accntSeqNo, atc, arqc, apdu
                        ,null, pin, zpk, imksmc, null, SMAdapter.FORMAT34);
        assertArrayEquals(expectdPIN.getPINBlock(), result.getValue0().getPINBlock());
        assertArrayEquals(ISOUtil.hex2byte("299E98C2B5A38B27"), result.getValue1());
    }

    @Test
    public void testTranslatePINGenerateSM_MACImpl5() throws Throwable {
        String accountNo = accountNoA;
        String accntSeqNo = accountNoA_CSN;
        byte[] apdu = apdu01;
        byte[] atc = atc01;
        byte[] arqc = arqc01;
        EncryptedPIN pin  =  pinUnderZPK;
        apdu = ISOUtil.concat(apdu, atc);
        apdu = ISOUtil.concat(apdu, arqc);
        EncryptedPIN expectdPIN = new EncryptedPIN("EF0F091EDA14326440C47C0F7C572473", SMAdapter.FORMAT41, accountNo);
        Pair<EncryptedPIN, byte[]> result = jcesecmod.translatePINGenerateSM_MAC(
                         MKDMethod.OPTION_A, SKDMethod.VSDC, null
                        ,imksmi, accountNo, accntSeqNo, atc, arqc, apdu
                        ,null, pin, zpk, imksmc, imkac, SMAdapter.FORMAT41);
        assertArrayEquals(expectdPIN.getPINBlock(), result.getValue0().getPINBlock());
        assertArrayEquals(ISOUtil.hex2byte("9F1B829D179E55C2"), result.getValue1());
    }

    @Test
    public void testTranslatePINGenerateSM_MACImpl6() throws Throwable {
        String accountNo = accountNoA;
        String accntSeqNo = accountNoA_CSN;
        byte[] apdu = apdu01;
        byte[] atc = atc01;
        byte[] arqc = arqc01;
        EncryptedPIN oldpin= new EncryptedPIN("33BADC0F07C6FB29", SMAdapter.FORMAT01, accountNo);
        EncryptedPIN pin   = pinUnderZPK;
        apdu = ISOUtil.concat(apdu, atc);
        apdu = ISOUtil.concat(apdu, arqc);
        EncryptedPIN expectdPIN = new EncryptedPIN("74253653C81CE99140C47C0F7C572473", SMAdapter.FORMAT42, accountNo);
        Pair<EncryptedPIN, byte[]> result = jcesecmod.translatePINGenerateSM_MAC(
                         MKDMethod.OPTION_A, SKDMethod.VSDC, null
                        ,imksmi, accountNo, accntSeqNo, atc, arqc, apdu
                        ,oldpin, pin, zpk, imksmc, imkac, SMAdapter.FORMAT42);
        assertArrayEquals(expectdPIN.getPINBlock(), result.getValue0().getPINBlock());
        assertArrayEquals(ISOUtil.hex2byte("6F403E51DCE1E4A6"), result.getValue1());
    }

}

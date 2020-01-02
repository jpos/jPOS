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

package org.jpos.security.jceadapter;

import static org.junit.jupiter.api.Assertions.fail;

import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.ISOUtil;
import org.jpos.security.EncryptedPIN;
import org.jpos.security.KeySerialNumber;
import org.jpos.security.SMAdapter;
import org.jpos.security.SecureDESKey;
import org.jpos.security.SecureKeyStore;
import org.jpos.security.SimpleKeyFile;
import org.jpos.util.Log;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Properties;

public class DUKPTTest {
    JCESecurityModule sm;
    SecureKeyStore ks;
    Log log;

    public static final String PIN = "1234";
    public static final String PAN = "4012345678909";
    public static final String PAN2 = "9999999800009901";
    public static final byte[] PINBLK =
            ISOUtil.hex2byte("041274EDCBA9876F");
    public static final byte[] ENCRYPTED_PINBLK =
            ISOUtil.hex2byte("6B1431D0D9B23093");
    public static final byte[] INITIAL_KSN =
            ISOUtil.hex2byte("FFFF9876543210E00000");
    public static final byte[] INITIAL_PINPAD_KEY =
            ISOUtil.hex2byte("6AC292FAA1315B4D");
    public static final byte[] BDKL = ISOUtil.hex2byte("0123456789ABCDEF");
    public static final byte[] BDKR = ISOUtil.hex2byte("FEDCBA9876543210");
    public static final byte[] BDK =
            ISOUtil.hex2byte("0123456789ABCDEFFEDCBA9876543210");

    @BeforeEach
    public void setUp() throws Exception
    {
        initLogger();
        initSM();
        initKS();
    }

    @Test
    public void test_DUKPT() throws Exception
    {
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "E00002"), ISOUtil.hex2byte ("B76997F83C1479DB"), PAN);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "E00003"), ISOUtil.hex2byte ("925BC2A39652CF75"), PAN);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "E00009"), ISOUtil.hex2byte ("8DC939C56D0FD13C"), PAN);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "E0000F"), ISOUtil.hex2byte ("C578B541B9A58A5B"), PAN);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "E00010"), ISOUtil.hex2byte ("6268FFC127118969"), PAN);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "EFF800"), ISOUtil.hex2byte ("A6552D24B01E71A0"), PAN);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "EFFC00"), ISOUtil.hex2byte ("6DEF7FD593810AC7"), PAN);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "F00000"), ISOUtil.hex2byte ("3FAC6F8763C0B60C"), PAN);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "EFF800"), ISOUtil.hex2byte ("A6552D24B01E71A0"), PAN);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "E00002"), ISOUtil.hex2byte ("E6F851D98E8DD722"), PAN2);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "E00003"), ISOUtil.hex2byte ("DE4FF9ABA523F853"), PAN2);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "E00006"), ISOUtil.hex2byte ("148F2CD3554F09F3"), PAN2);
        // Test 3DES
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "E00001"), ISOUtil.hex2byte ("1B9C1845EB993A7A"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "E00002"), ISOUtil.hex2byte ("10A01C8D02C69107"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "E00003"), ISOUtil.hex2byte ("18DC07B94797B466"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "E00004"), ISOUtil.hex2byte ("0BC79509D5645DF7"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "E00005"), ISOUtil.hex2byte ("5BC0AF22AD87B327"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "E00006"), ISOUtil.hex2byte ("A16DF70AE36158D8"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "E00007"), ISOUtil.hex2byte ("27711C16CB257F8E"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "E00008"), ISOUtil.hex2byte ("50E55547A5027551"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "E00009"), ISOUtil.hex2byte ("536CF7F678ACFC8D"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "E0000A"), ISOUtil.hex2byte ("EDABBA23221833FE"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "E0000B"), ISOUtil.hex2byte ("2328981C57B4BDBA"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "E0000C"), ISOUtil.hex2byte ("038D03CC926CF286"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "E0000D"), ISOUtil.hex2byte ("6C8AA97088B62C68"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "E0000E"), ISOUtil.hex2byte ("F17C9E1D72CD4950"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "E0000F"), ISOUtil.hex2byte ("B170F6E7F7F2F64A"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "E00010"), ISOUtil.hex2byte ("D5D9638559EF53D6"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "E00011"), ISOUtil.hex2byte ("D544F8CDD292C863"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "E00012"), ISOUtil.hex2byte ("7A21BD10F36DC41D"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "E00013"), ISOUtil.hex2byte ("78649BD17D0DFA60"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "E00014"), ISOUtil.hex2byte ("7E7E16EA0C31AD56"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "E00015"), ISOUtil.hex2byte ("72105C22EBC791E6"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "EFF800"), ISOUtil.hex2byte ("33365F5CC6F23C35"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "EFF801"), ISOUtil.hex2byte ("3A86BF003F835C9D"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "EFF802"), ISOUtil.hex2byte ("3DB977D05C36DF3F"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "EFF804"), ISOUtil.hex2byte ("BA83243305712099"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "EFF808"), ISOUtil.hex2byte ("B0DA04AC90A36D85"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "EFF810"), ISOUtil.hex2byte ("2CF02BD9C309EEDA"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "EFF820"), ISOUtil.hex2byte ("9D1E2F77AEEE81C6"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "EFF840"), ISOUtil.hex2byte ("40870B0F8BA2011C"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "EFF880"), ISOUtil.hex2byte ("22E340D6ABB40981"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "EFF900"), ISOUtil.hex2byte ("1A4C10AFBA03A430"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "EFFA00"), ISOUtil.hex2byte ("849763B43E5F9CFF"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "EFFC00"), ISOUtil.hex2byte ("DEFC6F09F8927B71"), PAN,true);
        test_DUKPT ("test-bdk", new KeySerialNumber ("987654", "3210", "F00000"), ISOUtil.hex2byte ("73EC88AD0AC5830E"), PAN,true);
    }

    @Test
    public void test_dataEncrypt() throws Exception {
        SecureDESKey bdk = (SecureDESKey) ks.getKey("test-bdk");
        byte[] original = "The quick brown fox jumps over the lazy dog".getBytes();
        byte[] cryptogram = sm.dataEncrypt(bdk, original);
        byte[] cleartext = sm.dataDecrypt(bdk, cryptogram);
        assertEqual(original, cleartext);
        cryptogram[0] = (byte) (cryptogram[0] ^ 0xAA);
        try {
            sm.dataDecrypt(bdk, cryptogram);
            fail("SMException not raised");
        } catch (Exception ignored) { }
    }

    private void test_DUKPT(String keyName, KeySerialNumber ksn, byte[] pinUnderDukpt, String pan)
            throws Exception
    {
        test_DUKPT(keyName,ksn,pinUnderDukpt,pan,false);
    }

    private void test_DUKPT(String keyName, KeySerialNumber ksn, byte[] pinUnderDukpt, String pan,boolean tdes)
            throws Exception
    {
        LogEvent evt = log.createInfo("test_DUKPT " + ksn);
        evt.addMessage(ksn);
        EncryptedPIN pin = new EncryptedPIN(
                pinUnderDukpt, SMAdapter.FORMAT01, pan
        );
        SecureDESKey bdk = ks.getKey(keyName);
        evt.addMessage(pin);
        evt.addMessage(ksn);
        evt.addMessage(bdk);

        EncryptedPIN pinUnderLMK = sm.importPIN(pin, ksn, bdk,tdes);
        evt.addMessage(pinUnderLMK);
        evt.addMessage(
                "<decrypted-pin>" + sm.decryptPIN(pinUnderLMK) + "</decrypted-pin>"
        );
        Logger.log(evt);
    }

    private void initKS() throws Exception
    {
        ks = new SimpleKeyFile("build/resources/test/org/jpos/security/keys-test");
        ((LogSource) ks).setLogger(log.getLogger(), "KS");
    }

    private void initSM() throws ConfigurationException
    {
        sm = new JCESecurityModule();
        sm.setLogger(log.getLogger(), "SSM");

        Properties props = new Properties();
        props.put("lmk", "build/resources/test/org/jpos/security/lmk-test");
        props.put("provider", "com.sun.crypto.provider.SunJCE");

        sm.setConfiguration(new SimpleConfiguration(props));
    }

    private void initLogger()
    {
        Logger logger = new Logger();
        logger.addListener(new SimpleLogListener());
        log = new Log(logger, "SM Test");
    }

    public void assertEqual(Object o0, Object o1)
    {
        if (!o0.equals(o1))
        {
            throw new RuntimeException("assertion failed");
        }
    }

    public void assertEqual(byte[] b0, byte[] b1)
    {
        if (!Arrays.equals(b0, b1))
        {
            throw new RuntimeException("byte[] assertion failed");
        }
    }
}

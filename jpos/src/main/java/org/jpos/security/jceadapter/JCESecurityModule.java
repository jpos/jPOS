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

package  org.jpos.security.jceadapter;

import org.javatuples.Pair;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.Environment;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
import org.jpos.security.*;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.jpos.util.SimpleMsg;

import javax.crypto.SecretKey;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Cipher;

/**
 * JCESecurityModule is an implementation of a security module in software.
 * <p>
 * It doesn't require any hardware device to work.<br>
 * JCESecurityModule also implements the SMAdapter, so you can view it: either
 * as a self contained security module adapter that doesn't need a security module
 * or a security module that plugs directly to jpos, so doesn't need
 * a separate adapter.<br>
 * It relies on Java(tm) Cryptography Extension (JCE), hence its name.<br>
 * JCESecurityModule relies on the JCEHandler class to do the low level JCE work.
 * <p>
 * WARNING: This version of JCESecurityModule is meant for testing purposes and
 * NOT for life operation, since the Local Master Keys are stored in CLEAR on
 * the system's disk. Comming versions of JCESecurity Module will rely on
 * java.security.KeyStore for a better protection of the Local Master Keys.
 *
 * @author Hani Samuel Kirollos
 * @author Robert Demski
 * @version $Revision$ $Date$
 */
@SuppressWarnings("unchecked")
public class JCESecurityModule extends BaseSMAdapter<SecureDESKey> {
    /**
     * Pattern representing key type string value.
     */
    private static final Pattern KEY_TYPE_PATTERN = Pattern.compile("([^:;]*)([:;])?([^:;])?([^:;])?");

    /**
     * Pattern for split two clear pins.
     */
    private static final Pattern SPLIT_PIN_PATTERN = Pattern.compile("[ :;,/.]");

    /**
     * NUmber of LMK pairs
     */
    private final static int LMK_PAIRS_NO = 0xe;

    /**
     * LMK variants appled to first byte of LMK pair
     */
    private final static int[] variants = {
        0x00, 0xa6, 0x5a, 0x6a, 0xde, 0x2b, 0x50, 0x74, 0x9c,0xfa
    };

    /**
     * LMK scheme variants appiled to first byte of second key in pair
     */
    private final static int[] schemeVariants = {
        0x00, 0xa6, 0x5a, 0x6a, 0xde, 0x2b
    };

    /**
     * Index of scheme variant for left LMK key for double length keys
     */
    private final static int KEY_U_LEFT      = 1;
    /**
     * Index of scheme variant for right LMK key for double length keys
     */
    private final static int KEY_U_RIGHT     = 2;
    /**
     * Index of scheme variant for left LMK key for triple length keys
     */
    private final static int KEY_T_LEFT      = 3;
    /**
     * Index of scheme variant for middle LMK key for triple length keys
     */
    private final static int KEY_T_MEDIUM    = 4;
    /**
     * Index of scheme variant for right LMK key for triple length keys
     */
    private final static int KEY_T_RIGHT     = 5;

    private static MessageDigest SHA1_MESSAGE_DIGEST = null;

    private static final byte[] _VARIANT_RIGHT_HALF = new byte[]
            {
                    (byte) 0xC0, (byte) 0xC0, (byte) 0xC0, (byte) 0xC0,
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
            };

    static {
      try {
          SHA1_MESSAGE_DIGEST = MessageDigest.getInstance("SHA-1");
      } catch (NoSuchAlgorithmException ex) {} //NOPMD: SHA-1 is a standard digest
    }

    /**
     * Creates an uninitialized JCE Security Module, you need to setConfiguration to initialize it
     */
    public JCESecurityModule () {
        super();
    }

    /**
     * @param lmkFile Local Master Keys filename of the JCE Security Module
     * @throws SMException
     */
    public JCESecurityModule (String lmkFile) throws SMException
    {
        Objects.requireNonNull(lmkFile);
        init(null, lmkFile, false);
    }

    public JCESecurityModule (String lmkFile, String jceProviderClassName) throws SMException
    {
        Objects.requireNonNull(lmkFile);
        init(jceProviderClassName, lmkFile, false);
    }

    public JCESecurityModule (Configuration cfg, Logger logger, String realm) throws ConfigurationException
    {
        setLogger(logger, realm);
        setConfiguration(cfg);
    }

    /**
     * Configures a JCESecurityModule
     * @param cfg The following properties are read:<br>
     *    lmk: Local Master Keys file (The only required parameter)<br>
     *    jce: JCE Provider Class Name, if not provided, it defaults to: com.sun.crypto.provider.SunJCE<br>
     *    rebuildlmk: (true/false), rebuilds the Local Master Keys file with new keys (WARNING: old keys will be erased)<br>
     *    cbc-mac: Cipher Block Chaining MAC algorithm name for given JCE Provider.<br>
     *             Default is ISO9797ALG3MACWITHISO7816-4PADDING from BouncyCastle provider (known as Retail-MAC)<br>
     *             that is suitable for most of interfaces with double length MAC key<br>
     *             ANSI X9.19 aka ISO/IEC 9797-1 MAC algorithm 3 padding method 2 - ISO7816<br>
     *    ede-mac: Encrypt Decrypt Encrypt MAC algorithm name for given JCE Provider.<br>
     *             Default is DESEDEMAC from BouncyCastle provider<br>
     *             that is suitable for BASE24 with double length MAC key<br>
     *             ANSI X9.19<br>
     * @throws ConfigurationException
     */
    @Override
    public void setConfiguration (Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;
        try {
            init(cfg.get("provider"), cfg.get("lmk", null), cfg.getBoolean("rebuildlmk"));
        } catch (SMException e) {
            throw  new ConfigurationException(e);
        }
    }

    @Override
    public SecureDESKey generateKeyImpl (short keyLength, String keyType) throws SMException {
        Key generatedClearKey = jceHandler.generateDESKey(keyLength);
        return encryptToLMK(keyLength, keyType, generatedClearKey);
    }

    @Override
    public SecureDESKey importKeyImpl (short keyLength, String keyType, byte[] encryptedKey,
            SecureDESKey kek, boolean checkParity) throws SMException {
        // decrypt encrypted key
        Key clearKEY = jceHandler.decryptDESKey(keyLength, encryptedKey, decryptFromLMK(kek),
                checkParity);
        // Encrypt Key under LMK
        return encryptToLMK(keyLength, keyType, clearKEY);
    }

    @Override
    public byte[] exportKeyImpl (SecureDESKey key, SecureDESKey kek) throws SMException {
        // get key in clear
        Key clearKey = decryptFromLMK(key);
        // Encrypt key under kek
        return jceHandler.encryptDESKey(key.getKeyLength(), clearKey, decryptFromLMK(kek));
    }

    private int getKeyTypeIndex (short keyLength, String keyType) throws SMException {
        int index;
        if (keyType==null)
            return 0;
        String majorType = getMajorType(keyType);
        if (!keyTypeToLMKIndex.containsKey(majorType))
            throw new SMException("Unsupported key type: " + majorType);
        index = keyTypeToLMKIndex.get(majorType);
        index |= getVariant(keyType) << 8;
        return index;
    }

    private static String getMajorType (String keyType) {
        Matcher m = KEY_TYPE_PATTERN.matcher(keyType);
        m.find();
        if (m.group(1) != null)
            return m.group(1);
        throw new IllegalArgumentException("Missing key type");
    }

    private static int getVariant (String keyType) {
        int variant = 0;
        Matcher m = KEY_TYPE_PATTERN.matcher(keyType);
        m.find();
        if (m.group(3) != null)
            try {
                variant = Integer.valueOf(m.group(3));
            } catch (NumberFormatException ex){
                throw new NumberFormatException("Value "+m.group(4)+" is not valid key variant");
            }
        return variant;
    }

    private static KeyScheme getScheme (int keyLength, String keyType) {
        KeyScheme scheme = KeyScheme.Z;
        switch (keyLength){
            case SMAdapter.LENGTH_DES:
                scheme = KeyScheme.Z; break;
            case SMAdapter.LENGTH_DES3_2KEY:
                scheme = KeyScheme.X; break;
            case SMAdapter.LENGTH_DES3_3KEY:
                scheme = KeyScheme.Y; break;
        }
        if (keyType==null)
            return scheme;
        Matcher m = KEY_TYPE_PATTERN.matcher(keyType);
        m.find();
        if (m.group(4) != null)
            try {
                scheme = KeyScheme.valueOf(m.group(4));
            } catch (IllegalArgumentException ex){
                throw new IllegalArgumentException("Value "+m.group(4)+" is not valid key scheme");
            }
        return scheme;
    }

    @Override
    public EncryptedPIN encryptPINImpl (String pin, String accountNumber) throws SMException {
        byte[] clearPINBlock = calculatePINBlock(pin, FORMAT00, accountNumber);
        // Encrypt
        byte[] translatedPINBlock = jceHandler.encryptData(clearPINBlock, getLMK(PINLMKIndex));
        return new EncryptedPIN(translatedPINBlock, FORMAT00, accountNumber, false);
    }

    @Override
    public String decryptPINImpl (EncryptedPIN pinUnderLmk) throws SMException {
        byte[] clearPINBlock = jceHandler.decryptData(pinUnderLmk.getPINBlock(),
                getLMK(PINLMKIndex));
        return calculatePIN(clearPINBlock, pinUnderLmk.getPINBlockFormat(), pinUnderLmk.getAccountNumber());
    }

    @Override
    public EncryptedPIN importPINImpl (EncryptedPIN pinUnderKd1, SecureDESKey kd1) throws SMException {
        // read inputs
        String accountNumber = pinUnderKd1.getAccountNumber();
        // Use FORMAT00 for encrypting PIN under LMK
        byte destinationPINBlockFormat = FORMAT00;
        // get clear PIN
        byte[] clearPINBlock = jceHandler.decryptData(pinUnderKd1.getPINBlock(),
                decryptFromLMK(kd1));
        // extract clear pin (as entered by card holder)
        String pin = calculatePIN(clearPINBlock, pinUnderKd1.getPINBlockFormat(),
                accountNumber);
        // Format PIN Block using proprietary FORMAT00 to be encrypetd under LMK
        clearPINBlock = calculatePINBlock(pin, destinationPINBlockFormat, accountNumber);
        // encrypt PIN
        byte[] translatedPINBlock = jceHandler.encryptData(clearPINBlock, getLMK(PINLMKIndex));
        return new EncryptedPIN(translatedPINBlock, destinationPINBlockFormat,
                accountNumber, false);
    }

    @Override
    public EncryptedPIN exportPINImpl (EncryptedPIN pinUnderLmk, SecureDESKey kd2,
            byte destinationPINBlockFormat) throws SMException {
        String accountNumber = pinUnderLmk.getAccountNumber();
        // process
        // get clear PIN
        byte[] clearPINBlock = jceHandler.decryptData(pinUnderLmk.getPINBlock(),
                getLMK(PINLMKIndex));
        // extract clear pin
        String pin = calculatePIN(clearPINBlock, pinUnderLmk.getPINBlockFormat(),
                accountNumber);
        clearPINBlock = calculatePINBlock(pin, destinationPINBlockFormat, accountNumber);
        // encrypt PIN
        byte[] translatedPINBlock = jceHandler.encryptData(clearPINBlock, decryptFromLMK(kd2));
        return new EncryptedPIN(translatedPINBlock, destinationPINBlockFormat,
                accountNumber, false);
    }

    @Override
    public EncryptedPIN generatePINImpl(String accountNumber, int pinLen, List<String> excludes)
            throws SMException {
        if (excludes==null)
          excludes = Arrays.asList();
        String pin;
        {
          Random rd = new SecureRandom();
          int max = (int)Math.pow(10, Math.min(pinLen, 9));
          int max2 = (int)Math.pow(10, Math.max(pinLen - 9,0));
          do {
            long pinl = rd.nextInt(max);
            if (pinLen > 9){
              pinl *= max2;
              pinl += rd.nextInt(max2);
            }
            pin = ISOUtil.zeropad(pinl, pinLen);
          } while (excludes.contains(pin));
        }

        return encryptPINImpl(pin, accountNumber);
    }

    /**
     * Visa way to decimalize data block
     * @param b
     * @return decimalized data string
     */
    private static String decimalizeVisa(byte[] b){
        char[] bec = ISOUtil.hexString(b).toUpperCase().toCharArray();
        char[] bhc = new char[bec.length];
        int k = 0;
        //Select 0-9 chars
        for ( char c : bec )
          if (c<'A')
            bhc[k++] = c;
        //Select A-F chars and map them to 0-5
        char adjust = 'A'-'0';
        for ( char c : bec )
          if (c>='A')
            bhc[k++] = (char) (c-adjust);
        return new String(bhc);
    }

    protected Key concatKeys(SecureDESKey keyA, SecureDESKey keyB)
            throws SMException {
        if ( keyA!=null && keyA.getKeyLength()==SMAdapter.LENGTH_DES
          && keyB!=null && keyB.getKeyLength()==SMAdapter.LENGTH_DES) {
          Key cvkAclear = decryptFromLMK(keyA);
          Key cvkBclear = decryptFromLMK(keyB);
          return jceHandler.formDESKey(SMAdapter.LENGTH_DES3_2KEY
                  ,ISOUtil.concat(cvkAclear.getEncoded(), cvkBclear.getEncoded()));
        }
        if (keyA!=null && keyA.getKeyLength()!=SMAdapter.LENGTH_DES)
          return decryptFromLMK(keyA);
        if (keyB!=null && keyB.getKeyLength()!=SMAdapter.LENGTH_DES)
          return decryptFromLMK(keyB);
        return null;
    }

    protected String calculateCVV(String accountNo, Key cvk, Date expDate,
                                String serviceCode) throws SMException {
        String ed = ISODate.formatDate(expDate, "yyMM");
        return calculateCVD(accountNo, cvk, ed, serviceCode);
    }

    protected String calculateCVD(String accountNo, Key cvk, String expDate,
                                String serviceCode) throws SMException {
        Key udka = jceHandler.formDESKey(SMAdapter.LENGTH_DES
                ,Arrays.copyOfRange(cvk.getEncoded(), 0, 8));

        byte[] block = ISOUtil.hex2byte(
                ISOUtil.zeropadRight(accountNo
                    + expDate
                    + serviceCode, 32));
        byte[] ba = Arrays.copyOfRange(block, 0, 8);
        byte[] bb = Arrays.copyOfRange(block, 8,16);

        //Encrypt ba with udka
        byte[] bc = jceHandler.encryptData(ba, udka);
        byte[] bd = ISOUtil.xor(bc, bb);
        //Encrypt bd Tripple DES
        byte[] be = jceHandler.encryptData(bd, cvk);
        return decimalizeVisa(be).substring(0,3);
    }

    @Override
    protected String calculateCVVImpl(String accountNo, SecureDESKey cvkA, SecureDESKey cvkB,
                                   Date expDate, String serviceCode) throws SMException {
        return calculateCVV(accountNo,concatKeys(cvkA, cvkB),expDate,serviceCode);
    }

    @Override
    protected String calculateCVDImpl(String accountNo, SecureDESKey cvkA, SecureDESKey cvkB,
                                   String expDate, String serviceCode) throws SMException {
        return calculateCVD(accountNo, concatKeys(cvkA, cvkB), expDate, serviceCode);
    }

    protected void checkCAVVArgs(String upn, String authrc, String sfarc)
            throws SMException {
        if (upn == null)
            throw new SMException("Unpredictable Number can not be null");
        if (authrc == null)
            throw new SMException("Authorization Result Code can not be null");
        if (sfarc == null)
            throw new SMException("Secend Factor Authorization Result Code"
                    + " can not be null");
        if (upn.length() != 4 )
            throw new SMException("Length of Unpredictable Number"
                  + " must be 4 but got "+upn.length());
        if (authrc.length() != 1 )
            throw new SMException("Length of Authorization Result Code"
                  + " must be 1 but got "+authrc.length());
        if (sfarc.length() != 2 )
            throw new SMException("Length of Secend Factor Authorization Result"
                  + " Code must be 2 but got "+sfarc.length());
    }

    @Override
    protected String calculateCAVVImpl(String accountNo, SecureDESKey cvk, String upn,
                                       String authrc, String sfarc) throws SMException {
        checkCAVVArgs(upn, authrc,sfarc);
        return calculateCVD(accountNo,concatKeys(cvk, null),upn,authrc+sfarc);
    }

    @Override
    protected boolean verifyCVVImpl(String accountNo, SecureDESKey cvkA, SecureDESKey cvkB,
                     String cvv, Date expDate, String serviceCode) throws SMException {
        String result = calculateCVV(accountNo, concatKeys(cvkA, cvkB), expDate, serviceCode);
        return result.equals(cvv);
    }

    @Override
    protected boolean verifyCVVImpl(String accountNo, SecureDESKey cvkA, SecureDESKey cvkB,
                     String cvv, String expDate, String serviceCode) throws SMException {
        String result = calculateCVD(accountNo, concatKeys(cvkA, cvkB), expDate, serviceCode);
        return result.equals(cvv);
    }

    @Override
    protected boolean verifyCAVVImpl(String accountNo, SecureDESKey cvk, String cavv,
                     String upn, String authrc, String sfarc) throws SMException {
        checkCAVVArgs(upn, authrc,sfarc);
        String result = calculateCVD(accountNo, concatKeys(cvk, null), upn, authrc+sfarc);
        return result.equals(cavv);
    }

    protected String calculatedCVV(String accountNo, SecureDESKey imkac,
                     String expDate, String serviceCode, byte[] atc, MKDMethod mkdm)
                     throws SMException {

        if (mkdm==null)
          mkdm = MKDMethod.OPTION_A;

        byte[] panpsn = formatPANPSN_dCVD(accountNo, null, mkdm);
        Key mkac = deriveICCMasterKey(decryptFromLMK(imkac), panpsn);

        String alteredPAN = ISOUtil.hexString(atc) + accountNo.substring(4);

        return calculateCVD(alteredPAN, mkac, expDate, serviceCode);
    }

    @Override
    protected boolean verifydCVVImpl(String accountNo, SecureDESKey imkac, String dcvv,
                     Date expDate, String serviceCode, byte[] atc, MKDMethod mkdm)
                     throws SMException {

        String ed = ISODate.formatDate(expDate, "yyMM");
        String res = calculatedCVV(accountNo, imkac, ed,
                serviceCode, atc, mkdm
        );
        return res.equals(dcvv);
    }

    @Override
    protected boolean verifydCVVImpl(String accountNo, SecureDESKey imkac, String dcvv,
                     String expDate, String serviceCode, byte[] atc, MKDMethod mkdm)
                     throws SMException {

        String res = calculatedCVV(accountNo, imkac, expDate,
                serviceCode, atc, mkdm
        );
        return res.equals(dcvv);
    }

    protected String calculateCVC3(SecureDESKey imkcvc3, String accountNo, String acctSeqNo,
                     byte[] atc, byte[] upn, byte[] data, MKDMethod mkdm)
                     throws SMException {
        if (mkdm==null)
          mkdm = MKDMethod.OPTION_A;
        byte[] panpsn = formatPANPSN_dCVD(accountNo, acctSeqNo, mkdm);
        Key mkcvc3 = deriveICCMasterKey(decryptFromLMK(imkcvc3), panpsn);
        byte[] ivcvc3 = data;
        if (ivcvc3.length != 2)
          //Compute IVCVC3
          ivcvc3 = calculateIVCVC3(mkcvc3, data);
        //Concatenate IVCVC3, UPN and ATC
        byte[] b = ISOUtil.concat(ivcvc3, upn);
        b = ISOUtil.concat(b, atc);
        //Encrypt prepared blok
        b = jceHandler.encryptData(b, mkcvc3);
        //Format last two bytes to integer
        int cvc3l = (b[6] & 0xff) << 8;
        cvc3l |= b[7] & 0xff;
        //Convert to string representation
        return String.format("%05d",cvc3l);
    }

    @Override
    protected boolean verifyCVC3Impl(SecureDESKey imkcvc3, String accountNo, String acctSeqNo,
                     byte[] atc, byte[] upn, byte[] data, MKDMethod mkdm, String cvc3)
                     throws SMException {

        String calcCVC3 = calculateCVC3(imkcvc3, accountNo, acctSeqNo,
                atc, upn, data, mkdm
        );
        cvc3 = cvc3==null?"":cvc3;
        //get some last digits
        calcCVC3 = calcCVC3.substring(5-cvc3.length());
        return calcCVC3.equals(cvc3);
    }

    private byte[] calculateIVCVC3(Key mkcvc3, byte[] data)
            throws JCEHandlerException {
        byte[] paddedData = paddingISO9797Method2(data);
        byte[] mac = calculateMACISO9797Alg3(mkcvc3, paddedData);
        return Arrays.copyOfRange(mac,6,8);
    }

    /**
     * ISO/IEC 9797-1 padding method 2
     * @param d da to be padded
     * @return padded data
     */
    private byte[] paddingISO9797Method2(byte[] d) {
        //Padding - first byte 0x80 rest 0x00
        byte[] t = new byte[d.length - d.length%8 + 8];
        System.arraycopy(d, 0, t, 0, d.length);
        for (int i=d.length;i<t.length;i++)
          t[i] = (byte)(i==d.length?0x80:0x00);
        d = t;
        return d;
    }

    /**
     * Calculate MAC according to ISO/IEC 9797-1 Alg 3
     * @param key DES double length key
     * @param d data to calculate MAC on it
     * @return 8 byte of mac value
     * @throws JCEHandlerException
     */
    private byte[] calculateMACISO9797Alg3(Key key, byte[] d) throws JCEHandlerException {
        Key kl = jceHandler.formDESKey(SMAdapter.LENGTH_DES
                            ,Arrays.copyOfRange(key.getEncoded(), 0, 8));
        Key kr = jceHandler.formDESKey(SMAdapter.LENGTH_DES
                            ,Arrays.copyOfRange(key.getEncoded(), 8, 16));
        if (d.length%8 != 0) {
            //Padding with 0x00 bytes
            byte[] t = new byte[d.length - d.length%8 + 8];
            System.arraycopy(d, 0, t, 0, d.length);
            d = t;
        }
        //MAC_CBC alg 3
        byte[] y_i = ISOUtil.hex2byte("0000000000000000");
        byte[] yi  = new byte[8];
        for ( int i=0;i<d.length;i+=8){
            System.arraycopy(d, i, yi, 0, yi.length);
            y_i = jceHandler.encryptData(ISOUtil.xor(yi, y_i), kl);
        }
        y_i = jceHandler.decryptData(y_i, kr);
        y_i = jceHandler.encryptData(y_i, kl);
        return y_i;
    }

    /**
     * Prepare 8-bytes data from PAN and PAN Sequence Number.
     * <p>
     * Used at EMV operations: {@link #verifyARQCImpl ARQC verification},
     * {@link #generateARPCImpl ARPC generation},
     * {@link #generateSM_MACImpl secure message MAC generation}
     * and {@link #translatePINGenerateSM_MACImpl secure message PIN translation}
     */
    private static byte[] formatPANPSN(String pan, String psn, MKDMethod mkdm)
            throws SMException {
        switch (mkdm){
            case OPTION_A:
                return formatPANPSNOptionA(pan, psn);
            case OPTION_B:
                if ( pan.length() <= 16)
                    //use OPTION_A
                    return formatPANPSNOptionA(pan, psn);
                return formatPANPSNOptionB(pan, psn);
            default:
                throw new SMException("Unsupported ICC Master Key derivation method");
        }
    }

    /**
     * Prepare 8-bytes data from PAN and PAN Sequence Number.
     * <p>
     * Used at dCVV verification {@link #verifydCVVImpl verifydCVV}
     * and CVC3 {@link #verifyCVC3Impl verifyCVC3}
     */
    private static byte[] formatPANPSN_dCVD(String pan, String psn, MKDMethod mkdm)
            throws SMException {
        switch (mkdm){
            case OPTION_A:
                return formatPANPSNOptionA(pan, psn);
            case OPTION_B:
                return formatPANPSNOptionB(pan, psn);
            default:
                throw new SMException("Unsupported ICC Master Key derivation method");
        }
    }

    /**
     * Format bytes representing Application PAN and
     * PAN Sequence Number in BCD format.
     * <p>
     * Concatenate from left to right decimal digits of PAN and
     * PAN Sequence Number digits. If {@code psn} is not present, it is
     * replaced by a "00" digits. If the result is less than 16 digits long,
     * pad it to the left with hexadecimal zeros in order to obtain an
     * 16-digit number. If the Application PAN has an odd number of decimal
     * digits then concatenate a "0" padding digit to the left thereby
     * ensuring that the result is an even number of digits.
     *
     * @param pan application primary account number
     * @param psn PAN Sequence Number
     * @return up to 11 bytes representing Application PAN
     */
    private static byte[] preparePANPSN(String pan, String psn){
        if (psn == null || psn.isEmpty())
            psn = "00";
        String ret = pan + psn;
        //convert digits to bytes and padd with "0"
        //to left for ensure even number of digits
        return ISOUtil.hex2byte(ret);
    }

    /**
     * Prepare 8-bytes data from PAN and PAN Sequence Number (Option A)
     * <ul>
     * <li> Prepare Application PAN and PAN Sequence Number by {@see #preparePANPSN}
     * <li> Select first 16 digits
     * </ul>
     * @param pan application primary account number
     * @param psn PAN Sequence Number
     * @return 8-bytes representing first 16 digits
     */
    private static byte[] formatPANPSNOptionA(String pan, String psn){
        if ( pan.length() < 14 )
            try {
                pan = ISOUtil.zeropad(pan, 14);
            } catch( ISOException ex ) {} //NOPMD: ISOException condition is checked before.
        byte[] b = preparePANPSN(pan, psn);
        return Arrays.copyOfRange(b, b.length-8, b.length);
    }

    /**
     * Prepare bytes data from PAN and PAN Sequence Number (Option B)
     *
     * <h5>Do the following:</h5>
     * <ul>
     * <li> Prepare Application PAN and PAN Sequence Number by {@see #preparePANPSN}
     * <li> Hash the prepared result using the SHA-1 hashing algorithm
     *      to obtain the 20-byte hash result
     * <li> Decimalize result of hasing by {@see #decimalizeVisa }
     * <li> Select first 16 decimal digits
     * </ul>
     * @param pan application primary account number
     * @param psn PAN Sequence Number
     * @return 8-bytes representing first 16 decimalised digits
     */
    private static byte[] formatPANPSNOptionB(String pan, String psn){
        byte[] b = preparePANPSN(pan, psn);
        //20-bytes sha-1 digest
        byte[] r = SHA1_MESSAGE_DIGEST.digest(b);
        //decimalized HEX string of digest
        String rs = decimalizeVisa(r);
        //return 16-bytes decimalizd digest
        return ISOUtil.hex2byte(rs.substring(0,16));
    }

    /**
     * Derive ICC Master Key from Issuer Master Key and preformated PAN/PANSeqNo
     *
     * Compute two 8-byte numbers:
     * <li> left part is a result of Tripple-DES encription {@code panpsn}
     * with {@code imk} as the key
     * <li> right part is a result of Tripple-DES binary inverted
     * {@code panpsn} with {@code imk} as the key
     * <li> concatenate left and right parts
     * <br>
     * Described in EMV v4.2 Book 2, Annex A1.4.1 Master Key Derivation point 2
     *
     * @param imk 16-bytes Issuer Master Key
     * @param panpsn preformated PAN and PAN Sequence Number
     * @return derived 16-bytes ICC Master Key with adjusted DES parity
     * @throws JCEHandlerException
     */
    protected Key deriveICCMasterKey(Key imk, byte[] panpsn)
            throws JCEHandlerException {
        byte[] l = Arrays.copyOfRange(panpsn, 0, 8);
        //left part of derived key
        l = jceHandler.encryptData(l, imk);
        byte[] r = Arrays.copyOfRange(panpsn, 0, 8);
        //inverse clear right part of key
        r = ISOUtil.xor(r, JCESecurityModule.fPaddingBlock);
        //right part of derived key
        r = jceHandler.encryptData(r,imk);
        //derived key
        byte[] mk = ISOUtil.concat(l,r);
        //fix DES parity of key
        Util.adjustDESParity(mk);
        //form JCE Tripple-DES Key
        return jceHandler.formDESKey(SMAdapter.LENGTH_DES3_2KEY, mk);
    }

    protected String calculatePVV(EncryptedPIN pinUnderLmk, Key key, int keyIdx
                               ,List<String> excludes) throws SMException {
        String pin = decryptPINImpl(pinUnderLmk);
        if (excludes!=null && excludes.contains(pin))
          throw new WeakPINException("Given PIN is on excludes list");
        String block = pinUnderLmk.getAccountNumber().substring(1);
        block += Integer.toString(keyIdx%10);
        block += pin.substring(0, 4);
        byte[] b = ISOUtil.hex2byte(block);
        b = jceHandler.encryptData(b, key);

        return decimalizeVisa(b).substring(0,4);
    }

    @Override
    protected String calculatePVVImpl(EncryptedPIN pinUnderLmk, SecureDESKey pvkA,
                               SecureDESKey pvkB, int pvkIdx, List<String> excludes)
            throws SMException {
        Key key = concatKeys(pvkA, pvkB);
        return calculatePVV(pinUnderLmk, key, pvkIdx, excludes);
    }

    @Override
    protected String calculatePVVImpl(EncryptedPIN pinUnderKd1, SecureDESKey kd1,
                       SecureDESKey pvkA, SecureDESKey pvkB, int pvkIdx,
                       List<String> excludes) throws SMException {
        Key key = concatKeys(pvkA, pvkB);
        EncryptedPIN pinUnderLmk = importPINImpl(pinUnderKd1, kd1);
        return calculatePVV(pinUnderLmk, key, pvkIdx, excludes);
    }

    @Override
    public boolean verifyPVVImpl(EncryptedPIN pinUnderKd1, SecureDESKey kd1, SecureDESKey pvkA,
                             SecureDESKey pvkB, int pvki, String pvv) throws SMException {
        Key key = concatKeys(pvkA, pvkB);
        EncryptedPIN pinUnderLmk = importPINImpl(pinUnderKd1, kd1);
        String result = calculatePVV(pinUnderLmk, key, pvki, null);
        return result.equals(pvv);
    }

    @Override
    public EncryptedPIN translatePINImpl (EncryptedPIN pinUnderKd1, SecureDESKey kd1,
            SecureDESKey kd2, byte destinationPINBlockFormat) throws SMException {
        EncryptedPIN translatedPIN;
        Key clearKd1 = decryptFromLMK(kd1);
        Key clearKd2 = decryptFromLMK(kd2);
        translatedPIN = translatePINExt(null, pinUnderKd1, clearKd1, clearKd2
                        ,destinationPINBlockFormat, null, PaddingMethod.MCHIP);
        return translatedPIN;
    }

    private EncryptedPIN translatePINExt (EncryptedPIN oldPinUnderKd1, EncryptedPIN pinUnderKd1, Key kd1,
            Key kd2, byte destinationPINBlockFormat, Key udk, PaddingMethod padm) throws SMException {
        String accountNumber = pinUnderKd1.getAccountNumber();
        // get clear PIN
        byte[] clearPINBlock = jceHandler.decryptData(pinUnderKd1.getPINBlock(), kd1);
        String pin = calculatePIN(clearPINBlock, pinUnderKd1.getPINBlockFormat(),
                accountNumber);
        // Reformat PIN Block
        byte[] translatedPINBlock;
        if (isVSDCPinBlockFormat(destinationPINBlockFormat)) {
          String udka = ISOUtil.hexString(Arrays.copyOfRange(udk.getEncoded(), 0, 8));
          if (destinationPINBlockFormat == SMAdapter.FORMAT42 ) {
              byte[] oldClearPINBlock = jceHandler.decryptData(oldPinUnderKd1.getPINBlock(), kd1);
              String oldPIN = calculatePIN(oldClearPINBlock, oldPinUnderKd1.getPINBlockFormat(),
                      accountNumber);
              clearPINBlock = calculatePINBlock(pin+":"+oldPIN, destinationPINBlockFormat, udka);
          } else
              clearPINBlock = calculatePINBlock(pin, destinationPINBlockFormat, udka);

          accountNumber = udka.substring(4);
        } else {
          clearPINBlock = calculatePINBlock(pin, destinationPINBlockFormat, accountNumber);
        }

        switch(padm){
          case VSDC:
              //Add VSDC pin block padding
              clearPINBlock = ISOUtil.concat(new byte[]{0x08}, clearPINBlock);
              clearPINBlock = paddingISO9797Method2(clearPINBlock);
              break;
          case CCD:
              //Add CCD pin block padding
              clearPINBlock = paddingISO9797Method2(clearPINBlock);
              break;
          default:
        }

        // encrypt PIN
        if (padm == PaddingMethod.CCD)
          translatedPINBlock = jceHandler.encryptDataCBC(clearPINBlock, kd2
                  ,Arrays.copyOf(zeroBlock, zeroBlock.length));
        else
          translatedPINBlock = jceHandler.encryptData(clearPINBlock, kd2);
        return new EncryptedPIN(translatedPINBlock
                        ,destinationPINBlockFormat, accountNumber, false);
    }

    /**
     * Derive the session key used for Application Cryptogram verification
     * or for secure messaging, the diversification value is the ATC
     * @param mkac unique ICC Master Key for Application Cryptogams or Secure Messaging
     * @param atc ICC generated Application Transaction Counter as diversification value
     * @return derived 16-bytes Session Key with adjusted DES parity
     * @throws JCEHandlerException
     */
    private Key deriveSK_VISA(Key mkac, byte[] atc) throws JCEHandlerException {

        byte[] skl = new byte[8];
        System.arraycopy(atc, atc.length-2, skl, 6, 2);
        skl = ISOUtil.xor(skl, Arrays.copyOfRange(mkac.getEncoded(),0 ,8));

        byte[] skr = new byte[8];
        System.arraycopy(atc, atc.length-2, skr, 6, 2);
        skr = ISOUtil.xor(skr, ISOUtil.hex2byte("000000000000FFFF"));
        skr = ISOUtil.xor(skr, Arrays.copyOfRange(mkac.getEncoded(),8 ,16));
        Util.adjustDESParity(skl);
        Util.adjustDESParity(skr);
        return jceHandler.formDESKey(SMAdapter.LENGTH_DES3_2KEY, ISOUtil.concat(skl,skr));
    }

    /**
     * Common Session Key Derivation Method for secure messaging.
     * <p>
     * The diversification value is the <em>RAND</em>, which is ARQC
     * incremeted by 1 (with overflow) after each script command
     * for that same ATC value.
     * Described in EMV v4.2 Book 2, Annex A1.3.1 Common Session Key
     * Derivation Option for secure messaging.
     *
     * @param mksm unique ICC Master Key for Secure Messaging
     * @param rand Application Cryptogram as diversification value
     * @return derived 16-bytes Session Key with adjusted DES parity
     * @throws JCEHandlerException
     */
    private Key deriveCommonSK_SM(Key mksm, byte[] rand) throws JCEHandlerException {
      byte[] rl = Arrays.copyOf(rand,8);
      rl[2] = (byte)0xf0;
      byte[] skl = jceHandler.encryptData(rl, mksm);
      byte[] rr = Arrays.copyOf(rand,8);
      rr[2] = (byte)0x0f;
      byte[] skr = jceHandler.encryptData(rr, mksm);
      Util.adjustDESParity(skl);
      Util.adjustDESParity(skr);
      return jceHandler.formDESKey(SMAdapter.LENGTH_DES3_2KEY, ISOUtil.concat(skl,skr));
    }

    /**
     * Common Session Key Derivation Method for Application Cryptograms.
     * <p>
     * Described in EMV v4.2 Book 2, Annex A1.3.1 Common Session Key Derivation
     * Option for Application Cryptograms.
     *
     * @param mkac unique ICC Master Key for Application Cryptogams.
     * @param atc ICC generated Application Transaction Counter as diversification value.
     * @return derived 16-bytes Session Key with adjusted DES parity.
     * @throws JCEHandlerException
     */
    private Key deriveCommonSK_AC(Key mkac, byte[] atc) throws JCEHandlerException {

        byte[] r = new byte[8];
        System.arraycopy(atc, atc.length-2, r, 0, 2);

        return deriveCommonSK_SM(mkac, r);
    }

    /**
     * MasterCard Proprietary Session Key Derivation (SKD) method.
     * <p>
     * Described in M/Chip 4 version 1.1 Security & Key Management manual
     * paragraph 7 ICC Session Key Derivation.
     *
     * @param mkac unique ICC Master Key for Application Cryptogams
     * @param atc ICC generated Application Transaction Counter as diversification value
     * @param upn terminal generated random as diversification value
     * @return derived 16-bytes Session Key with adjusted DES parity
     * @throws JCEHandlerException
     */
    private Key deriveSK_MK(Key mkac, byte[] atc, byte[] upn) throws JCEHandlerException {

        byte[] r = new byte[8];
        System.arraycopy(atc, atc.length-2, r, 0, 2);
        System.arraycopy(upn, upn.length-4, r, 4, 4);

        return deriveCommonSK_SM(mkac, r);
    }

    private void constraintMKDM(MKDMethod mkdm, SKDMethod skdm) throws SMException {
        if (mkdm == MKDMethod.OPTION_B)
            throw new SMException("Master Key Derivation Option B"
                    + " is not used in practice with scheme "+skdm);
    }

    private void constraintARPCM(SKDMethod skdm, ARPCMethod arpcMethod) throws SMException {
        if (arpcMethod == ARPCMethod.METHOD_2 )
            throw new SMException("ARPC generation method 2"
                    + " is not used in practice with scheme "+skdm);
    }

    /**
     * Calculate ARQC.
     * <p>
     * Entry point e.g. for simulator systems
     */
    protected byte[] calculateARQC(MKDMethod mkdm, SKDMethod skdm
            ,SecureDESKey imkac, String accountNo, String accntSeqNo, byte[] atc
            ,byte[] upn, byte[] transData) throws SMException {
        if (mkdm==null)
            mkdm = MKDMethod.OPTION_A;

        byte[] panpsn = formatPANPSN(accountNo, accntSeqNo, mkdm);
        Key mkac = deriveICCMasterKey(decryptFromLMK(imkac), panpsn);
        Key skac = mkac;
        switch(skdm){
            case VSDC:
                constraintMKDM(mkdm, skdm);
                break;
            case MCHIP:
                constraintMKDM(mkdm, skdm);
                skac = deriveSK_MK(mkac,atc,upn);
                break;
            case EMV_CSKD:
                skac = deriveCommonSK_AC(mkac, atc);
                break;
            default:
                throw new SMException("Session Key Derivation "+skdm+" not supported");
        }

        return calculateMACISO9797Alg3(skac, transData);
    }

    @Override
    protected boolean verifyARQCImpl(MKDMethod mkdm, SKDMethod skdm, SecureDESKey imkac
            ,String accountNo, String accntSeqNo, byte[] arqc, byte[] atc
            ,byte[] upn, byte[] transData) throws SMException {

        byte[] res = calculateARQC(mkdm, skdm, imkac, accountNo, accntSeqNo
                ,atc, upn, transData);
        return Arrays.equals(arqc, res);
    }

    @Override
    public byte[] generateARPCImpl(MKDMethod mkdm, SKDMethod skdm, SecureDESKey imkac
            ,String accountNo, String accntSeqNo, byte[] arqc, byte[] atc, byte[] upn
            ,ARPCMethod arpcMethod, byte[] arc, byte[] propAuthData)
            throws SMException {
        if (mkdm==null)
            mkdm = MKDMethod.OPTION_A;

        byte[] panpsn = formatPANPSN(accountNo, accntSeqNo, mkdm);
        Key mkac = deriveICCMasterKey(decryptFromLMK(imkac), panpsn);
        Key skarpc = mkac;
        switch(skdm){
            case VSDC:
                constraintMKDM(mkdm, skdm);
                constraintARPCM(skdm, arpcMethod);
                break;
            case MCHIP:
                constraintMKDM(mkdm, skdm);
                constraintARPCM(skdm, arpcMethod);
                break;
            case EMV_CSKD:
                skarpc = deriveCommonSK_AC(mkac, atc);
                break;
            default:
                throw new SMException("Session Key Derivation "+skdm+" not supported");
        }

        return calculateARPC(skarpc, arqc, arpcMethod, arc, propAuthData);
    }

    @Override
    public byte[] verifyARQCGenerateARPCImpl(MKDMethod mkdm, SKDMethod skdm, SecureDESKey imkac
            ,String accountNo, String accntSeqNo, byte[] arqc, byte[] atc, byte[] upn 
            ,byte[] transData, ARPCMethod arpcMethod, byte[] arc, byte[] propAuthData)
            throws SMException {
        if (mkdm==null)
            mkdm = MKDMethod.OPTION_A;

        byte[] panpsn = formatPANPSN(accountNo, accntSeqNo, mkdm);
        Key mkac = deriveICCMasterKey(decryptFromLMK(imkac), panpsn);
        Key skac = mkac;
        Key skarpc = mkac;
        switch(skdm){
            case VSDC:
                constraintMKDM(mkdm, skdm);
                constraintARPCM(skdm, arpcMethod);
                break;
            case MCHIP:
                constraintMKDM(mkdm, skdm);
                constraintARPCM(skdm, arpcMethod);
                skac = deriveSK_MK(mkac,atc,upn);
                break;
            case EMV_CSKD:
                skac = deriveSK_MK(mkac, atc, new byte[4]);
                skarpc = skac;
                break;
            default:
                throw new SMException("Session Key Derivation "+skdm+" not supported");
        }

        if (!Arrays.equals(arqc, calculateMACISO9797Alg3(skac, transData)))
            return null;

        return calculateARPC(skarpc, arqc, arpcMethod, arc, propAuthData);
    }

    /**
     * Calculate ARPC.
     * <p>
     * Entry point e.g. for simulator systems
     */
    protected byte[] calculateARPC(Key skarpc, byte[] arqc, ARPCMethod arpcMethod
             ,byte[] arc, byte[] propAuthData) throws SMException {
        if (arpcMethod == null)
            arpcMethod = ARPCMethod.METHOD_1;

        byte[] b = new byte[8];
        switch(arpcMethod) {
            case METHOD_1:
                System.arraycopy(arc, arc.length-2, b, 0, 2);
                b = ISOUtil.xor(arqc, b);
                return jceHandler.encryptData(b, skarpc);
            case METHOD_2:
                b = ISOUtil.concat(arqc, arc);
                if (propAuthData != null)
                    b = ISOUtil.concat(b, propAuthData);
                b = paddingISO9797Method2(b);
                b = calculateMACISO9797Alg3(skarpc, b);
                return Arrays.copyOf(b, 4);
            default:
                throw new SMException("ARPC Method "+arpcMethod+" not supported");
        }
    }

    @Override
    protected byte[] generateSM_MACImpl(MKDMethod mkdm, SKDMethod skdm
            ,SecureDESKey imksmi, String accountNo, String accntSeqNo
            ,byte[] atc, byte[] arqc, byte[] data) throws SMException {
        if (mkdm==null)
          mkdm = MKDMethod.OPTION_A;
        byte[] panpsn = formatPANPSN(accountNo, accntSeqNo, mkdm);
        Key mksmi = deriveICCMasterKey(decryptFromLMK(imksmi), panpsn);
        Key smi;
        switch(skdm){
          case VSDC:
            smi = deriveSK_VISA(mksmi, atc);
            data = paddingISO9797Method2(data);
            break;
          case MCHIP:
          case EMV_CSKD:
            smi = deriveCommonSK_SM(mksmi,arqc);
            data = paddingISO9797Method2(data);
            break;
          default:
            throw new SMException("Session Key Derivation "+skdm+" not supported");
        }
        return calculateMACISO9797Alg3(smi, data);
    }

    @Override
    protected Pair<EncryptedPIN,byte[]> translatePINGenerateSM_MACImpl(
            MKDMethod mkdm, SKDMethod skdm, PaddingMethod padm, SecureDESKey imksmi
           ,String accountNo, String accntSeqNo, byte[] atc, byte[] arqc
           ,byte[] data, EncryptedPIN currentPIN, EncryptedPIN newPIN
           ,SecureDESKey kd1, SecureDESKey imksmc, SecureDESKey imkac
           ,byte destinationPINBlockFormat) throws SMException {
        if (mkdm==null)
          mkdm = MKDMethod.OPTION_A;
        byte[] panpsn = formatPANPSN(accountNo, accntSeqNo, mkdm);
        Key mksmc = deriveICCMasterKey(decryptFromLMK(imksmc), panpsn);
        Key smc;
        PaddingMethod derivedPADM;
        switch(skdm){
          case VSDC:
            smc = deriveSK_VISA(mksmc, atc);
            derivedPADM = PaddingMethod.VSDC;
            break;
          case MCHIP:
            smc = deriveCommonSK_SM(mksmc,arqc);
            derivedPADM = PaddingMethod.MCHIP;
            break;
          case EMV_CSKD:
            smc = deriveCommonSK_SM(mksmc,arqc);
            derivedPADM = PaddingMethod.CCD;
            break;
          default:
            throw new SMException("Session Key Derivation "+skdm+" not supported");
        }

        //Use derived Padding Method if not specified
        if ( padm == null )
          padm = derivedPADM;

        Key udk = null;
        if (isVSDCPinBlockFormat(destinationPINBlockFormat))
          udk = deriveICCMasterKey(decryptFromLMK(imkac), panpsn);

        EncryptedPIN pin =  translatePINExt(currentPIN, newPIN, decryptFromLMK(kd1)
                            ,smc, destinationPINBlockFormat, udk, padm);
        data = ISOUtil.concat(data, pin.getPINBlock());
        byte[] mac = generateSM_MACImpl(mkdm, skdm, imksmi, accountNo
                               ,accntSeqNo, atc, arqc, data);
        return new Pair(pin, mac);
    }

    private boolean isVSDCPinBlockFormat(byte pinBlockFormat) {
        return pinBlockFormat==SMAdapter.FORMAT41 || pinBlockFormat==SMAdapter.FORMAT42;
    }

    @Override
    public byte[] encryptDataImpl(CipherMode cipherMode, SecureDESKey kd
            ,byte[] data, byte[] iv) throws SMException {
        Key dek = decryptFromLMK(kd);
        return jceHandler.doCryptStuff(data, dek, Cipher.ENCRYPT_MODE, cipherMode, iv);
    }

    @Override
    public byte[] decryptDataImpl(CipherMode cipherMode, SecureDESKey kd
            ,byte[] data, byte[] iv) throws SMException {
        Key dek = decryptFromLMK(kd);
        return jceHandler.doCryptStuff(data, dek, Cipher.DECRYPT_MODE, cipherMode, iv);
    }

    /**
     * Generates CBC-MAC (Cipher Block Chaining Message Authentication Code)
     * for some data.
     *
     * @param data the data to be MACed
     * @param kd the key used for MACing
     * @return generated CBC-MAC bytes
     * @throws SMException
     */
    @Override
    protected byte[] generateCBC_MACImpl (byte[] data, SecureDESKey kd) throws SMException {
        LogEvent evt = new LogEvent(this, "jce-provider-cbc-mac");
        try {
          return generateMACImpl(data,kd,cfg.get("cbc-mac","ISO9797ALG3MACWITHISO7816-4PADDING"),evt);
        } catch (Exception e) {
          Logger.log(evt);
          throw  e instanceof SMException ? (SMException)e : new SMException(e);
        }
    }

    /**
     * Generates EDE-MAC (Encrypt Decrypt Encrypt Message Authentication Code)
     * for some data.
     *
     * @param data the data to be MACed
     * @param kd the key used for MACing
     * @return generated EDE-MAC bytes
     * @throws SMException
     */
    @Override
    protected byte[] generateEDE_MACImpl (byte[] data, SecureDESKey kd) throws SMException {
        LogEvent evt = new LogEvent(this, "jce-provider-ede-mac");
        try {
          return generateMACImpl(data,kd,cfg.get("ede-mac","DESEDEMAC"),evt);
        } catch (Exception e) {
          Logger.log(evt);
          throw  e instanceof SMException ? (SMException)e : new SMException(e);
        }
    }

    private byte[] generateMACImpl (byte[] data, SecureDESKey kd,
        String macAlgorithm, LogEvent evt) throws SMException {
        try {
          return jceHandler.generateMAC(data,decryptFromLMK(kd),macAlgorithm);
        } catch (JCEHandlerException e){
          evt.addMessage(e);
          if (e.getCause() instanceof InvalidKeyException)
            throw new SMException(e);
          else
            throw new SMException("Unable to load MAC algorithm whose name is: " + macAlgorithm +
              ". Check that is used correct JCE provider and/or it is proper configured for this module.",e);
        }
    }

    /**
     * Generates a random clear key component.
     * @param keyLength
     * @return clear key componenet
     * @throws SMException
     */
    public String generateClearKeyComponent (short keyLength) throws SMException {
        String clearKeyComponenetHexString;
        SimpleMsg[] cmdParameters =  {
            new SimpleMsg("parameter", "Key Length", keyLength)
        };
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Generate Clear Key Component", cmdParameters));
        
        try {
            Key clearKey = jceHandler.generateDESKey(keyLength);
            byte[] clearKeyData = jceHandler.extractDESKeyMaterial(keyLength, clearKey);
            clearKeyComponenetHexString = ISOUtil.hexString(clearKeyData);
            evt.addMessage(new SimpleMsg("result", "Generated Clear Key Componenet", clearKeyComponenetHexString));
        } catch (JCEHandlerException e) {
            evt.addMessage(e);
            throw  e;
        } finally {
            Logger.log(evt);
        }
        return  clearKeyComponenetHexString;
    }

    /**
     * Generates key check value.<br>
     * @param secureDESKey SecureDESKey with untrusted or fake Key Check Value
     * @return generated Key Check Value
     * @throws SMException
     */
    @Override
    protected byte[] generateKeyCheckValueImpl (SecureDESKey secureDESKey) throws SMException {
        return  calculateKeyCheckValue(decryptFromLMK(secureDESKey));
    }

    @Override
    public SecureDESKey translateKeySchemeImpl (SecureDESKey key, KeyScheme keyScheme)
            throws SMException {

        if (key == null)
            throw new SMException("Missing key to change");

        if (keyScheme == null)
            throw new SMException("Missing desired key schame");

        if (keyScheme == key.getScheme())
            return key;

        switch (key.getScheme()) {
            case Z:
                throw new SMException("Single length DES key using the variant method"
                        + " can not be converted to any other key");
            case U:
            case T:
                throw new SMException("DES key using the variant method can not "
                        + "be converted to less secure key using X9.17 method");
            case X:
                if (keyScheme != KeyScheme.U)
                    throw new SMException("Double length key using X9.17 method may be  "
                            + "converted only to double length key using variant method");
                break;
            case Y:
                if (keyScheme != KeyScheme.T)
                    throw new SMException("Triple length key using X9.17 method may be  "
                            + "converted only to triple length key using variant method");
                break;
            default:
                throw new SMException("Change key scheme allowed only for keys"
                        + "using variant method");
        }

        // get key in clear
        Key clearKey = decryptFromLMK(key);
        // Encrypt key under LMK
        String keyType = getMajorType(key.getKeyType()) + ":" + key.getVariant() + keyScheme;
        return encryptToLMK(key.getKeyLength(), keyType, clearKey);
    }

    /**
     * Forms a key from 3 clear components and returns it encrypted under its corresponding LMK
     * The corresponding LMK is determined from the keyType
     * @param keyLength e.g. LENGTH_DES, LENGTH_DES3_2, LENGTH_DES3_3, ..
     * @param keyType possible values are those defined in the SecurityModule inteface. e.g., ZMK, TMK,...
     * @param clearComponent1HexString HexString containing the first component
     * @param clearComponent2HexString HexString containing the second component
     * @param clearComponent3HexString HexString containing the second component
     * @return forms an SecureDESKey from two clear components
     * @throws SMException
     */
    public SecureDESKey formKEYfromThreeClearComponents(
            short keyLength,
            String keyType,
            String clearComponent1HexString,
            String clearComponent2HexString,
            String clearComponent3HexString) throws SMException {
        SecureDESKey secureDESKey;
        LogEvent evt = new LogEvent(this, "s-m-operation");

        try {
            byte[] clearComponent1 = ISOUtil.hex2byte(clearComponent1HexString);
            byte[] clearComponent2 = clearComponent2HexString != null ? ISOUtil.hex2byte(clearComponent2HexString) : new byte[keyLength >> 3];
            byte[] clearComponent3 = clearComponent3HexString != null ? ISOUtil.hex2byte(clearComponent3HexString) : new byte[keyLength >> 3];
            byte[] clearKeyBytes = ISOUtil.xor(ISOUtil.xor(clearComponent1, clearComponent2),
                    clearComponent3);
            Key clearKey = jceHandler.formDESKey(keyLength, clearKeyBytes);
            secureDESKey = encryptToLMK(keyLength, keyType, clearKey);
            SimpleMsg[] cmdParameters =  {
                new SimpleMsg("parameter", "Key Length", keyLength),
                new SimpleMsg("parameter", "Key Type", keyType),
                new SimpleMsg("parameter", "Component 1 Check Value", calculateKeyCheckValue(jceHandler.formDESKey(keyLength, clearComponent1))),
                new SimpleMsg("parameter", "Component 2 Check Value", calculateKeyCheckValue(jceHandler.formDESKey(keyLength, clearComponent2))),
                new SimpleMsg("parameter", "Component 3 Check Value", calculateKeyCheckValue(jceHandler.formDESKey(keyLength, clearComponent3)))
            };                        
            evt.addMessage(new SimpleMsg("command", "Form Key from Three Clear Components", cmdParameters));            
            evt.addMessage(new SimpleMsg("result", "Formed Key", secureDESKey));
        } catch (JCEHandlerException e) {
            evt.addMessage(e);
            throw  e;
        } finally {
            Logger.log(evt);
        }
        return  secureDESKey;
    }

    @Override
    public SecureDESKey formKEYfromClearComponents (short keyLength, String keyType, String... components)
      throws SMException
    {
        if (components.length < 1 || components.length > 3)
            throw new SMException("Invalid number of clear key components");
        String[] s = new String[3];
        int i=0;
        for (String component : components)
            s[i++] = component;

        return formKEYfromThreeClearComponents(keyLength, keyType, s[0], s[1], s[2]);
    }


    /**
     * Calculates a key check value over a clear key
     * @param key
     * @return the key check value
     * @exception SMException
     */
    protected byte[] calculateKeyCheckValue (Key key) throws SMException {
        byte[] encryptedZeroBlock = jceHandler.encryptData(zeroBlock, key);
        return ISOUtil.trim(encryptedZeroBlock, 3);
    }

    /**
     * Encrypts a clear DES Key under LMK to form a SecureKey
     * @param keyLength
     * @param keyType
     * @param clearDESKey
     * @return secureDESKey
     * @throws SMException
     */
    protected SecureDESKey encryptToLMK (short keyLength, String keyType, Key clearDESKey) throws SMException {
        Key novar, left, medium, right;
        byte[] clearKeyBytes = jceHandler.extractDESKeyMaterial(keyLength, clearDESKey);
        byte[] bl = new byte[SMAdapter.LENGTH_DES>>3];
        byte[] bm = new byte[SMAdapter.LENGTH_DES>>3];
        byte[] br = new byte[SMAdapter.LENGTH_DES>>3];
        byte[] encrypted = null;
        // enforce correct (odd) parity before encrypting the key
        Util.adjustDESParity(clearKeyBytes);
        int lmkIndex = getKeyTypeIndex(keyLength, keyType);
        switch ( getScheme(keyLength, keyType) ){
            case Z:
            case X:
            case Y:
                novar = getLMK( lmkIndex );
                encrypted = jceHandler.encryptData(clearKeyBytes, novar);
                break;
            case U:
                left  = getLMK( KEY_U_LEFT  << 12 | lmkIndex & 0xfff );
                right = getLMK( KEY_U_RIGHT << 12 | lmkIndex & 0xfff );
                System.arraycopy(clearKeyBytes, 0, bl, 0, bl.length);
                System.arraycopy(clearKeyBytes, bl.length, br, 0, br.length);
                bl = jceHandler.encryptData(bl, left);
                br = jceHandler.encryptData(br, right);
                encrypted = ISOUtil.concat(bl, br);
                break;
            case T:
                left  = getLMK( KEY_T_LEFT   << 12 | lmkIndex & 0xfff );
                medium= getLMK( KEY_T_MEDIUM << 12 | lmkIndex & 0xfff );
                right = getLMK( KEY_T_RIGHT  << 12 | lmkIndex & 0xfff );
                System.arraycopy(clearKeyBytes, 0, bl, 0, bl.length);
                System.arraycopy(clearKeyBytes, bl.length, bm, 0, bm.length);
                System.arraycopy(clearKeyBytes, bl.length+bm.length, br, 0, br.length);
                bl = jceHandler.encryptData(bl, left);
                bm = jceHandler.encryptData(bm, medium);
                br = jceHandler.encryptData(br, right);
                encrypted = ISOUtil.concat(bl, bm);
                encrypted = ISOUtil.concat(encrypted, br);
                break;
        }
        SecureDESKey secureDESKey = new SecureDESKey(keyLength, keyType, encrypted,
                calculateKeyCheckValue(clearDESKey));
        return  secureDESKey;
    }

    /**
     * Decrypts a secure DES key from encryption under LMK
     * @param secureDESKey (Key under LMK)
     * @return clear key
     * @throws SMException
     */
    protected Key decryptFromLMK (SecureDESKey secureDESKey) throws SMException {
        Key left, medium, right;
        byte[] keyBytes = secureDESKey.getKeyBytes();
        byte[] bl = new byte[SMAdapter.LENGTH_DES>>3];
        byte[] bm = new byte[SMAdapter.LENGTH_DES>>3];
        byte[] br = new byte[SMAdapter.LENGTH_DES>>3];
        byte[] clearKeyBytes = null;
        Integer lmkIndex = getKeyTypeIndex(secureDESKey.getKeyLength(), secureDESKey.getKeyType());
        if (lmkIndex==null)
           throw new SMException("Unsupported key type: " + secureDESKey.getKeyType());
        lmkIndex |= secureDESKey.getVariant()<<8;
        switch ( secureDESKey.getScheme() ) {
            case Z:
            case X:
            case Y:
                clearKeyBytes = jceHandler.decryptData(keyBytes, getLMK( lmkIndex ));
                break;
            case U:
                left  = getLMK( KEY_U_LEFT  << 12 | lmkIndex & 0xfff );
                right = getLMK( KEY_U_RIGHT << 12 | lmkIndex & 0xfff );
                System.arraycopy(keyBytes, 0, bl, 0, bl.length);
                System.arraycopy(keyBytes, bl.length, br, 0, br.length);
                bl = jceHandler.decryptData(bl, left);
                br = jceHandler.decryptData(br, right);
                clearKeyBytes = ISOUtil.concat(bl, br);
                clearKeyBytes = ISOUtil.concat(clearKeyBytes, 0, clearKeyBytes.length, clearKeyBytes, 0, br.length );
                break;
            case T:
                left  = getLMK( KEY_T_LEFT   << 12 | lmkIndex & 0xfff );
                medium= getLMK( KEY_T_MEDIUM << 12 | lmkIndex & 0xfff );
                right = getLMK( KEY_T_RIGHT  << 12 | lmkIndex & 0xfff );
                System.arraycopy(keyBytes, 0, bl, 0, bl.length);
                System.arraycopy(keyBytes, bl.length, bm, 0, bm.length);
                System.arraycopy(keyBytes, bl.length+bm.length, br, 0, br.length);
                bl = jceHandler.decryptData(bl, left);
                bm = jceHandler.decryptData(bm, medium);
                br = jceHandler.decryptData(br, right);
                clearKeyBytes = ISOUtil.concat(bl, bm);
                clearKeyBytes = ISOUtil.concat(clearKeyBytes, br);
                break;
        }
        if (!Util.isDESParityAdjusted(clearKeyBytes))
            throw new JCEHandlerException("Parity not adjusted");
        return jceHandler.formDESKey(secureDESKey.getKeyLength(), clearKeyBytes);
    }

    private char[] formatPINBlock(String pin, int checkDigit){
      char[] block = ISOUtil.hexString(fPaddingBlock).toCharArray();
      char[] pinLenHex = String.format("%02X", pin.length()).toCharArray();
      pinLenHex[0] = (char)('0' + checkDigit);

      // pin length then pad with 'F'
      System.arraycopy(pinLenHex, 0, block, 0, pinLenHex.length);
      System.arraycopy(pin.toCharArray(), 0
                      ,block, pinLenHex.length, pin.length());
      return block;
    }

    private String[] splitPins(String pins) {
      String[] pin = new String[2];
      String[] p = SPLIT_PIN_PATTERN.split(pins);
      pin[0] = p[0];
      if (p.length >= 2)
          pin[1] = p[1];
      return pin;
    }

    /**
     * Calculates the clear PIN Block
     * @param pin as entered by the card holder on the PIN entry device
     * @param pinBlockFormat
     * @param accountNumber (the 12 right-most digits of the account number excluding the check digit)
     * @return The clear PIN Block
     * @throws SMException
     *
     */
    protected byte[] calculatePINBlock (String pin, byte pinBlockFormat, String accountNumber) throws SMException {
        byte[] pinBlock = null;
        String oldPin = null;
        if (pinBlockFormat==SMAdapter.FORMAT42){
          String[] p = splitPins(pin);
          pin = p[0];
          oldPin = p[1];
          if (oldPin.length() < MIN_PIN_LENGTH || oldPin.length() > MAX_PIN_LENGTH)
              throw  new SMException("Invalid OLD PIN length: " + oldPin.length());
          if (!ISOUtil.isNumeric(oldPin, 10))
              throw  new SMException("Invalid OLD PIN decimal digits: " + oldPin);
        }
        if (pin.length() < MIN_PIN_LENGTH || pin.length() > MAX_PIN_LENGTH)
            throw  new SMException("Invalid PIN length: " + pin.length());
        if (!ISOUtil.isNumeric(pin, 10))
            throw  new SMException("Invalid PIN decimal digits: " + pin);
        if (isVSDCPinBlockFormat(pinBlockFormat)) {
          if (accountNumber.length() != 16 )
            throw  new SMException("Invalid UDK-A: " + accountNumber
                    + ". The length of the UDK-A must be 16 hexadecimal digits");
        } else if (accountNumber.length() != 12)
            throw  new SMException("Invalid Account Number: " + accountNumber + ". The length of the account number must be 12 (the 12 right-most digits of the account number excluding the check digit)");
        switch (pinBlockFormat) {
            case FORMAT00: // same as FORMAT01
            case FORMAT01:
                {
                    // Block 1
                    byte[] block1 = ISOUtil.hex2byte(new String(formatPINBlock(pin,0x0)));

                    // Block 2
                    byte[] block2 = ISOUtil.hex2byte("0000" + accountNumber);
                    // pinBlock
                    pinBlock = ISOUtil.xor(block1, block2);
                }
                break;
            case FORMAT03:
                {
                    char[] block = ISOUtil.hexString(fPaddingBlock).toCharArray();
                    System.arraycopy(pin.toCharArray(), 0
                                    ,block, 0, pin.length());
                    pinBlock = ISOUtil.hex2byte (new String(block));
                }
                break;
            case FORMAT05:
                {
                    // Block 1
                    char[] block1 = formatPINBlock(pin, 0x1);

                    // Block rnd
                    byte[] rnd = new byte[8];
                    Random rd = new SecureRandom();
                    rd.nextBytes(rnd);

                    // Block 2
                    char[] block2 = ISOUtil.hexString(rnd).toCharArray();

                    // merge blocks
                    System.arraycopy(block1, 0
                                    ,block2, 0, pin.length() + 2
                    );
                    // pinBlock
                    pinBlock = ISOUtil.hex2byte(new String(block2));
                }
                break;
            case FORMAT34:
                {
                    pinBlock = ISOUtil.hex2byte (new String(formatPINBlock(pin,0x2)));
                }
                break;
            case FORMAT35:
                {
                    // Block 1
                    byte[] block1 = ISOUtil.hex2byte(new String(formatPINBlock(pin,0x2)));

                    // Block 2
                    byte[] block2 = ISOUtil.hex2byte("0000" + accountNumber);
                    // pinBlock
                    pinBlock = ISOUtil.xor(block1, block2);
                }
                break;
            case FORMAT41:
                {
                    // Block 1
                    byte[] block1 = ISOUtil.hex2byte(new String(formatPINBlock(pin,0x0)));

                    // Block 2 - account number should contain Unique DEA Key A (UDK-A)
                    byte[] block2 = ISOUtil.hex2byte("00000000"
                                + accountNumber.substring(accountNumber.length()-8) );
                    // pinBlock
                    pinBlock = ISOUtil.xor(block1, block2);
                }
                break;
            case FORMAT42:
                {
                    // Block 1
                    byte[] block1 = ISOUtil.hex2byte(new String(formatPINBlock(pin,0x0)));

                    // Block 2 - account number should contain Unique DEA Key A (UDK-A)
                    byte[] block2 = ISOUtil.hex2byte("00000000"
                                + accountNumber.substring(accountNumber.length()-8) );
                    // Block 3 - old pin
                    byte[] block3 = ISOUtil.hex2byte(ISOUtil.zeropadRight(oldPin, 16));
                    // pinBlock
                    pinBlock = ISOUtil.xor(block1, block2);
                    pinBlock = ISOUtil.xor(pinBlock, block3);
                }
                break;
            default:
                throw  new SMException("Unsupported PIN format: " + pinBlockFormat);
        }
        return  pinBlock;
    }

    private void validatePinBlock(char[] pblock, int checkDigit, int padidx, int offset)
            throws SMException {
      validatePinBlock(pblock, checkDigit, padidx, offset, 'F');
    }

    private void validatePinBlock(char[] pblock, int checkDigit
                             ,int padidx, int offset, char padDigit)
            throws SMException {
      // test pin block check digit
      if (checkDigit >= 0 && pblock[0] - '0' != checkDigit)
          throw new SMException("PIN Block Error - invalid check digit");
      // test pin block pdding
      int i = pblock.length - 1;
      while (i >= padidx)
          if (pblock[i--] != padDigit && padDigit > 0)
              throw new SMException("PIN Block Error - invalid padding");
      // test pin block digits
      while (i >= offset)
          if (pblock[i--] >= 'A')
              throw new SMException("PIN Block Error - illegal pin digit");
      // test pin length
      int pinLength = padidx - offset;
      if (pinLength < MIN_PIN_LENGTH || pinLength > MAX_PIN_LENGTH)
          throw new SMException("PIN Block Error - invalid pin length: " + pinLength);
    }

    /**
     * Calculates the clear pin (as entered by card holder on the pin entry device)
     * givin the clear PIN block
     * @param pinBlock clear PIN Block
     * @param pinBlockFormat
     * @param accountNumber
     * @return the pin
     * @throws SMException
     */
    protected String calculatePIN (byte[] pinBlock, byte pinBlockFormat, String accountNumber) throws SMException {
        String pin = null;
        if (isVSDCPinBlockFormat(pinBlockFormat)) {
          if (accountNumber.length() != 16 )
            throw  new SMException("Invalid UDK-A: " + accountNumber
                    + ". The length of the UDK-A must be 16 hexadecimal digits");
        } else if (accountNumber.length() != 12)
            throw  new SMException("Invalid Account Number: " + accountNumber + ". The length of the account number must be 12 (the 12 right-most digits of the account number excluding the check digit)");
        switch (pinBlockFormat) {
            case FORMAT00: // same as format 01
            case FORMAT01:
                {
                    // Block 2
                    byte[] bl2 = ISOUtil.hex2byte("0000" + accountNumber);
                    // get Block1
                    byte[] bl1 = ISOUtil.xor(pinBlock, bl2);
                    int pinLength = bl1[0] & 0x0f;
                    char[] block1 = ISOUtil.hexString(bl1).toCharArray();
                    int offset = 2;
                    int checkDigit = 0x0;
                    int padidx = pinLength + offset;
                    // test pin block
                    validatePinBlock(block1,checkDigit,padidx,offset);
                    // get pin
                    pin = new String(Arrays.copyOfRange(block1, offset, padidx));
                }
                break;
            case FORMAT03: 
                {
                    String bl1 = ISOUtil.hexString(pinBlock);
                    int padidx = bl1.indexOf('F');
                    if ( padidx < 0) padidx = 12;
                    char[] block1 = bl1.toCharArray();
                    int checkDigit = -0x1;
                    int offset = 0;

                    // test pin block
                    validatePinBlock(block1,checkDigit,padidx,offset);
                    // get pin
                    pin = new String(Arrays.copyOfRange(block1, offset, padidx));
                }
                break;
            case FORMAT05:
                {
                    // get Block1
                    byte[] bl1 = pinBlock;
                    int pinLength = bl1[0] & 0x0f;
                    char[] block1 = ISOUtil.hexString(bl1).toCharArray();
                    int offset = 2;
                    int checkDigit = 0x01;
                    int padidx = pinLength + offset;
                    // test pin block
                    validatePinBlock(block1, checkDigit, padidx, offset, (char) 0);
                    // get pin
                    pin = new String(Arrays.copyOfRange(block1, offset, padidx));
                }
                break;
            case FORMAT34:
                {
                    int pinLength = pinBlock[0] & 0x0f;
                    char[] block1 = ISOUtil.hexString(pinBlock).toCharArray();
                    int offset = 2;
                    int checkDigit = 0x2;
                    int padidx = pinLength + offset;
                    // test pin block
                    validatePinBlock(block1,checkDigit,padidx,offset);
                    // get pin
                    pin = new String(Arrays.copyOfRange(block1, offset, padidx));
                }
                break;
            case FORMAT35:
                {
                    // Block 2
                    byte[] bl2 = ISOUtil.hex2byte("0000" + accountNumber);
                    // get Block1
                    byte[] bl1 = ISOUtil.xor(pinBlock, bl2);
                    int pinLength = bl1[0] & 0x0f;
                    char[] block1 = ISOUtil.hexString(bl1).toCharArray();
                    int offset = 2;
                    int checkDigit = 0x2;
                    int padidx = pinLength + offset;
                    // test pin block
                    validatePinBlock(block1,checkDigit,padidx,offset);
                    // get pin
                    pin = new String(Arrays.copyOfRange(block1, offset, padidx));
                }
                break;
            case FORMAT41:
                {
                    // Block 2 - account number should contain Unique DEA Key A (UDK-A)
                    byte[] bl2 = ISOUtil.hex2byte("00000000"
                                + accountNumber.substring(accountNumber.length()-8) );
                    // get Block1
                    byte[] bl1 = ISOUtil.xor(pinBlock, bl2);
                    int pinLength = bl1[0] & 0x0f;
                    char[] block1 = ISOUtil.hexString(bl1).toCharArray();
                    int offset = 2;
                    int checkDigit = 0x0;
                    int padidx = pinLength + offset;
                    // test pin block
                    validatePinBlock(block1,checkDigit,padidx,offset);
                    // get pin
                    pin = new String(Arrays.copyOfRange(block1, offset, padidx));
                }
                break;
            case FORMAT42:
                {
                    // Block 2 - account number should contain Unique DEA Key A (UDK-A)
                    byte[] bl2 = ISOUtil.hex2byte("00000000"
                                + accountNumber.substring(accountNumber.length()-8) );
                    // get Block1
                    byte[] bl1 = ISOUtil.xor(pinBlock, bl2);
                    int pinLength = bl1[0] & 0x0f;
                    char[] block1 = ISOUtil.hexString(bl1).toCharArray();
                    int offset = 2;
                    int checkDigit = 0x0;
                    int padidx = pinLength + offset;
                    // test pin block
                    validatePinBlock(block1,checkDigit,padidx,offset,'0');
                    // get pin
                    pin = new String(Arrays.copyOfRange(block1, offset, padidx));
                }
                break;
            default:
                throw  new SMException("Unsupported PIN Block format: " + pinBlockFormat);
        }
        return  pin;
    }

    /**
     * Initializes the JCE Security Module
     * @param jceProviderClassName
     * @param lmkFile Local Master Keys File used by JCE Security Module to store the LMKs
     * @param lmkRebuild if set to true, the lmkFile gets overwritten with newly generated keys (WARNING: this would render all your previously stored SecureKeys unusable)
     * @throws SMException
     */
    private void init (String jceProviderClassName, String lmkFile, boolean lmkRebuild) throws SMException {
        File lmk = lmkFile != null ? new File(lmkFile) : null;
        if (lmk == null && !lmkRebuild)
            throw new SMException ("null lmkFile - needs rebuild");
        try {
            keyTypeToLMKIndex = new TreeMap<>();
            keyTypeToLMKIndex.put(SMAdapter.TYPE_ZMK, 0x000);
            keyTypeToLMKIndex.put(SMAdapter.TYPE_ZPK, 0x001);
            keyTypeToLMKIndex.put(SMAdapter.TYPE_PVK, 0x002);
            keyTypeToLMKIndex.put(SMAdapter.TYPE_TPK, 0x002);
            keyTypeToLMKIndex.put(SMAdapter.TYPE_TMK, 0x002);
            keyTypeToLMKIndex.put(SMAdapter.TYPE_TAK, 0x003);
//            keyTypeToLMKIndex.put(PINLMKIndex,        0x004);
            keyTypeToLMKIndex.put(SMAdapter.TYPE_CVK, 0x402);
            keyTypeToLMKIndex.put(SMAdapter.TYPE_ZAK, 0x008);
            keyTypeToLMKIndex.put(SMAdapter.TYPE_BDK, 0x009);
            keyTypeToLMKIndex.put(SMAdapter.TYPE_MK_AC, 0x109);
            keyTypeToLMKIndex.put(SMAdapter.TYPE_MK_SMI,  0x209);
            keyTypeToLMKIndex.put(SMAdapter.TYPE_MK_SMC,  0x309);
            keyTypeToLMKIndex.put(SMAdapter.TYPE_MK_DAC,  0x409);
            keyTypeToLMKIndex.put(SMAdapter.TYPE_MK_DN,   0x509);
            keyTypeToLMKIndex.put(SMAdapter.TYPE_MK_CVC3, 0x709);
            keyTypeToLMKIndex.put(SMAdapter.TYPE_ZEK, 0x00A);
            keyTypeToLMKIndex.put(SMAdapter.TYPE_DEK, 0x00B);
            keyTypeToLMKIndex.put(SMAdapter.TYPE_RSA_SK, 0x00C);
            keyTypeToLMKIndex.put(SMAdapter.TYPE_HMAC,   0x10C);
            keyTypeToLMKIndex.put(SMAdapter.TYPE_RSA_PK, 0x00D);
            Provider provider;
            LogEvent evt = new LogEvent(this, "jce-provider");
            try {
                if (jceProviderClassName == null || jceProviderClassName.isEmpty()) {
                    evt.addMessage("No JCE Provider specified. Attempting to load default provider (SunJCE).");
                    jceProviderClassName = "com.sun.crypto.provider.SunJCE";
                }
                provider = (Provider)Class.forName(jceProviderClassName).newInstance();
                Security.addProvider(provider);
                evt.addMessage("name", provider.getName());
            } catch (Exception e) {
                evt.addMessage(e);
                throw  new SMException("Unable to load jce provider whose class name is: "
                        + jceProviderClassName);
            } finally {
                Logger.log(evt);
            }
            jceHandler = new JCEHandler(provider);
            if (lmkRebuild) {
                // Creat new LMK file
                evt = new LogEvent(this, "local-master-keys");
                if (lmk != null)
                    evt.addMessage("Rebuilding new Local Master Keys in file: \"" + lmk.getCanonicalPath() + "\".");
                Logger.log(evt);
                // Generate New random Local Master Keys
                generateLMK();
                // Write the new Local Master Keys to file
                evt = new LogEvent(this, "local-master-keys");
                if (lmk != null) {
                    writeLMK(lmk);
                    evt.addMessage("Local Master Keys built successfully in file: \""
                      + lmk.getCanonicalPath() + "\".");
                } else {
                    evt.addMessage("Local Master Keys built successfully");
                }
                Logger.log(evt);
            }
            if (lmk != null) {
                if (!lmk.exists()) {
                    // LMK File does not exist
                    throw  new SMException("Error loading Local Master Keys, file: \""
                      + lmk.getCanonicalPath() + "\" does not exist."
                      + " Please specify a valid LMK file, or rebuild a new one.");
                }
                else {
                    // Read LMK from file
                    readLMK(lmk);
                    evt = new LogEvent(this, "local-master-keys");
                    evt.addMessage("Loaded successfully from file: \"" + lmk.getCanonicalPath()
                      + "\"");
                    Logger.log(evt);
                }
            }
        } catch (Exception e) {
            if (e instanceof SMException) {
                throw  (SMException)e;
            }
            else {
                throw  new SMException(e);
            }
        }
    }

    private byte[] applySchemeVariant(byte[] lmkdata, int variant){
        byte[] vardata = new byte[lmkdata.length];
        System.arraycopy(lmkdata, 0, vardata, 0, lmkdata.length);
        //XOR first byte of second key with selected variant byte
        vardata[8] ^= variant;
        return vardata;
    }

    private byte[] applyVariant(byte[] lmkdata, int variant){
        byte[] vardata = new byte[lmkdata.length];
        System.arraycopy(lmkdata, 0, vardata, 0, lmkdata.length);
        //XOR first byte of first key with selected variant byte
        vardata[0] ^= variant;
        return vardata;
    }

    private void spreadLMKVariants(byte[] lmkData, int idx) throws SMException {
        int i = 0;
        for (int v :variants){
            int k = 0;
            byte[] variantData = applyVariant(lmkData,v);
            for (int sv :schemeVariants){
                byte[] svData = applySchemeVariant(variantData,sv);
//                System.out.println(String.format("LMK0x%1$d:%2$d:%3$02x=%4$s", k, i, idx
//                        ,ISOUtil.hexString(svData)));
                // make it 3 components to work with sun JCE
                svData = ISOUtil.concat(
                    svData, 0, jceHandler.getBytesLength(SMAdapter.LENGTH_DES3_2KEY),
                    svData, 0, jceHandler.getBytesLength(SMAdapter.LENGTH_DES)
                    );
                int key = idx;
                key += 0x100*i;
                key += 0x1000*k++;
                lmks.put(key, (SecretKey)jceHandler.formDESKey(SMAdapter.LENGTH_DES3_2KEY, svData));
            }
            i++;
        }
    }

    /**
     * Generates new LMK keys
     * @exception SMException
     */
    private void generateLMK () throws SMException {
        lmks.clear();
        try {
            for (int i = 0; i <= LMK_PAIRS_NO; i++){
              SecretKey lmkKey = (SecretKey)jceHandler.generateDESKey(LMK_KEY_LENGTH);
              spreadLMKVariants(lmkKey.getEncoded(), i);
            }
        } catch (JCEHandlerException e) {
            throw  new SMException("Can't generate Local Master Keys", e);
        }
    }

    /**
     * reads (loads) LMK's from lmkFile
     * @param lmkFile
     * @exception SMException
     */
    private void readLMK (File lmkFile) throws SMException {
        lmks.clear();
        try {
            Properties lmkProps = new Properties();
            InputStream in = new BufferedInputStream(new FileInputStream(lmkFile));
            try {
                lmkProps.load(in);
            } finally {
                in.close();
            }
            Enumeration<?> e = lmkProps.propertyNames();
            while (e.hasMoreElements()) {
                String propName = (String) e.nextElement();
                lmkProps.put(propName, Environment.get(lmkProps.getProperty(propName)));
            }
            byte[] lmkData;
            for (int i = 0; i <= LMK_PAIRS_NO; i++) {
                lmkData = ISOUtil.hex2byte(lmkProps.getProperty(
                        String.format("LMK0x%1$02x", i)).substring(0, SMAdapter.LENGTH_DES3_2KEY/4));
                spreadLMKVariants(lmkData, i);
            }
        } catch (Exception e) {
            throw  new SMException("Can't read Local Master Keys from file: " +
                    lmkFile, e);
        }
    }

    /**
     * Writes a newly generated LMK's to lmkFile
     * @param lmkFile
     * @exception SMException
     */
    private void writeLMK (File lmkFile) throws SMException {
        Properties lmkProps = new Properties();
        try {
            for (int i = 0; i <= LMK_PAIRS_NO; i++) {
                lmkProps.setProperty(String.format("LMK0x%1$02x", i),
                        ISOUtil.hexString(lmks.get(i).getEncoded()));
            }
            OutputStream out = new BufferedOutputStream(new FileOutputStream(lmkFile));
            try {
                lmkProps.store(out, "Local Master Keys");
            } finally {
                out.close();
            }
        } catch (Exception e) {
            throw  new SMException("Can't write Local Master Keys to file: " + lmkFile,
                    e);
        }
    }

    /**
     * gets the suitable LMK variant for the key index
     * @param lmkIndex
     * @return the lmks secret key for the givin key index
     * @throws SMException
     */
    private SecretKey getLMK (Integer lmkIndex) throws SMException {
        SecretKey lmk = lmks.get(lmkIndex);
        if (lmk==null)
            throw  new SMException(String.format("Invalid key code: LMK0x%1$04x", lmkIndex));
        return  lmk;
    }
    /**
     * maps a key type to an LMK Index
     */
    private Map<String,Integer> keyTypeToLMKIndex;
    /**
     * The clear Local Master Keys
     */
    private final Map<Integer,SecretKey> lmks = new TreeMap<>();
    /**
     * A index for the LMK used to encrypt the PINs
     */
    private static final Integer PINLMKIndex = 0x004;
    /**
     * The key length (in bits) of the Local Master Keys.
     * JCESecurityModule uses Triple DES Local Master Keys
     */
    private static final short LMK_KEY_LENGTH = LENGTH_DES3_2KEY;
    /**
     * The minimum length of the PIN
     */
    private static final short MIN_PIN_LENGTH = 4;
    /**
     * The maximum length of the PIN
     */
    private static final short MAX_PIN_LENGTH = 12;

    /**
     * a 64-bit block of ones used when calculating pin blocks
     */
    private static final byte[] fPaddingBlock = ISOUtil.hex2byte("FFFFFFFFFFFFFFFF");

    /**
     * a dummy 64-bit block of zeros used when calculating the check value
     */
    private static final byte[] zeroBlock = ISOUtil.hex2byte("0000000000000000");

    protected JCEHandler jceHandler;

    //--------------------------------------------------------------------------------------------------
    // DUKPT
    //--------------------------------------------------------------------------------------------------

    private byte[] encrypt64(byte[] data, byte[] key)
            throws JCEHandlerException
    {
        return jceHandler.encryptData(
                data,
                jceHandler.formDESKey((short) (key.length << 3), key)
        );
    }

    private byte[] decrypt64(byte[] data, byte[] key)
            throws JCEHandlerException
    {
        return jceHandler.decryptData(
                data,
                jceHandler.formDESKey((short) (key.length << 3), key)
        );
    }

    protected byte[] specialEncrypt(byte[] data, byte[] key)
            throws JCEHandlerException
    {
        if (key.length == 8)
        {
            data = ISOUtil.xor(data, key);
            data = encrypt64(data, key);
            return ISOUtil.xor(data, key);
        }
        byte[] keyL = new byte[8];
        byte[] keyR = new byte[8];
        System.arraycopy(key, 0, keyL, 0, 8);
        System.arraycopy(key, 8, keyR, 0, 8);
        data = encrypt64(data, keyL);
        data = decrypt64(data, keyR);
        data = encrypt64(data, keyL);
        return data;
    }

    protected byte[] specialDecrypt(byte[] data, byte[] key)
            throws JCEHandlerException
    {
        if (key.length == 8)
        {
            data = ISOUtil.xor(data, key);
            data = decrypt64(data, key);
            return ISOUtil.xor(data, key);
        }
        byte[] keyL = new byte[8];
        byte[] keyR = new byte[8];
        System.arraycopy(key, 0, keyL, 0, 8);
        System.arraycopy(key, 8, keyR, 0, 8);
        data = decrypt64(data, keyL);
        data = encrypt64(data, keyR);
        data = decrypt64(data, keyL);
        return data;
    }

    private void shr(byte[] b)
    {
        boolean carry = false;
        for (int i = 0; i < b.length; i++)
        {
            byte c = b[i];
            b[i] = (byte) (c >>> 1 & 0x7F);
            if (carry)
            {
                b[i] |= 0x80;
            }
            carry = (c & 0x01) == 1;
        }
    }

    private void or(byte[] b, byte[] mask, int offset)
    {
        int len = Math.min(b.length - offset, mask.length);
        byte[] d = new byte[len];

        for (int i = 0; i < len; i++)
        {
            b[offset++] |= mask[i];
        }
    }

    private byte[] and(byte[] b, byte[] mask, int offset)
    {
        int len = Math.min(b.length - offset, mask.length);
        byte[] d = new byte[b.length];
        System.arraycopy(b, 0, d, 0, b.length);

        for (int i = 0; i < len; i++, offset++)
        {
            d[offset] = (byte) (b[offset] & mask[i]);
        }
        return d;
    }

    private byte[] and(byte[] b, byte[] mask)
    {
        return and(b, mask, 0);
    }

    private boolean notZero(byte[] b)
    {
        int l = b.length;
        for (int i = 0; i < l; i++)
        {
            if (b[i] != 0)
            {
                return true;
            }
        }
        return false;
    }

    private byte[] calculateInitialKey(KeySerialNumber sn, SecureDESKey bdk, boolean tdes)
            throws SMException
    {
        byte[] kl = new byte[8];
        byte[] kr = new byte[8];
        byte[] kk = decryptFromLMK(bdk).getEncoded();

        System.arraycopy(kk, 0, kl, 0, 8);
        System.arraycopy(kk, 8, kr, 0, 8);
        String paddedKsn;
        try
        {
            paddedKsn = ISOUtil.padleft(
                    sn.getBaseKeyID() + sn.getDeviceID() + sn.getTransactionCounter(),
                    20, 'F'
            );
        }
        catch (ISOException e)
        {
            throw new SMException(e);
        }

        byte[] ksn = ISOUtil.hex2byte(paddedKsn.substring(0, 16));
        ksn[7] &= 0xE0;

        byte[] data = encrypt64(ksn, kl);
        data = decrypt64(data, kr);
        data = encrypt64(data, kl);

        // ANS X9.24:2009 3DES keys (@DFLC/@apr 2011) A.6 steps 5 & 6 (p69)
        if (tdes)
        {
            byte[] kl2 = ISOUtil.xor(kl, _VARIANT_RIGHT_HALF);
            byte[] kr2 = ISOUtil.xor(kr, _VARIANT_RIGHT_HALF);
            byte[] data2 = encrypt64(ksn, kl2);
            data2 = decrypt64(data2, kr2);
            data2 = encrypt64(data2, kl2);

            byte[] d = new byte[16];
            System.arraycopy(data, 0, d, 0, 8);
            System.arraycopy(data2, 0, d, 8, 8);
            data = d;
        }
        return data;
    }


    @Override
    public byte[] dataEncrypt (SecureDESKey bdk, byte[] clearText) throws SMException {
        try {
            byte[] ksnB = jceHandler.generateDESKey ((short) 128).getEncoded();
            KeySerialNumber ksn = getKSN (ISOUtil.hexString(ksnB));
            byte[] derivedKey = calculateDerivedKey (ksn, bdk, true, true);
            Key dk = jceHandler.formDESKey ((short) 128, derivedKey);
            byte[] cypherText = jceHandler.encryptData (lpack(clearText), dk);

            ByteBuffer bb = ByteBuffer.allocate (cypherText.length + 32);
            bb.put (ksnB);
            bb.put (cypherText);
            bb.put (hash8 (new byte[][]{ ksnB, cypherText, derivedKey }));
            return bb.array();
        } catch (JCEHandlerException e) {
            throw new SMException (e);
        }
    }

    @Override
    public byte[] dataDecrypt (SecureDESKey bdk, byte[] cypherText) throws SMException {
        try {
            if (cypherText.length < 32) {
                throw new SMException (
                  "Invalid key block '" + ISOUtil.hexString (cypherText) + "'"
                );
            }
            byte[] ksnB          = new byte[24];
            byte[] encryptedData = new byte[cypherText.length - 32];
            byte[] mac           = new byte[8];

            System.arraycopy (cypherText,  0, ksnB, 0, 24);
            System.arraycopy (cypherText, 24, encryptedData, 0, encryptedData.length);
            System.arraycopy (cypherText, cypherText.length-8, mac, 0, 8);

            KeySerialNumber ksn = getKSN (ISOUtil.hexString(ksnB));

            byte[] derivedKey = calculateDerivedKey (ksn, bdk, true, true);
            Key dk = jceHandler.formDESKey ((short) 128, derivedKey);
            byte[] clearText = jceHandler.decryptData (encryptedData, dk);
            byte[] generatedMac = hash8 (
              new byte[][] { ksnB, encryptedData, derivedKey }
            );
            if (!Arrays.equals (mac, generatedMac))
                throw new SMException ("Invalid cyphertext.");
            return lunpack (clearText);
        } catch (JCEHandlerException e) {
            throw new SMException (e);
        }
   }

    protected byte[] calculateDerivedKey(KeySerialNumber ksn, SecureDESKey bdk, boolean tdes, boolean dataEncryption)
            throws SMException
    {
        return tdes?calculateDerivedKeyTDES(ksn,bdk, dataEncryption):calculateDerivedKeySDES(ksn,bdk);
    }

    private byte[] calculateDerivedKeySDES(KeySerialNumber ksn, SecureDESKey bdk)
            throws SMException
    {
        final byte[] _1FFFFF =
                new byte[]{(byte) 0x1F, (byte) 0xFF, (byte) 0xFF};
        final byte[] _100000 =
                new byte[]{(byte) 0x10, (byte) 0x00, (byte) 0x00};
        final byte[] _E00000 =
                new byte[]{(byte) 0xE0, (byte) 0x00, (byte) 0x00};

        byte[] curkey = calculateInitialKey(ksn, bdk, false);
        byte[] smidr = ISOUtil.hex2byte(
                ksn.getBaseKeyID() + ksn.getDeviceID() + ksn.getTransactionCounter()
        );
        byte[] reg3 = ISOUtil.hex2byte(ksn.getTransactionCounter());
        reg3 = and(reg3, _1FFFFF);
        byte[] shiftr = _100000;
        byte[] temp;
        byte[] tksnr;
        smidr = and(smidr, _E00000, 5);
        do
        {
            temp = and(shiftr, reg3);
            if (notZero(temp))
            {
                or(smidr, shiftr, 5);
                tksnr = ISOUtil.xor(smidr, curkey);
                tksnr = encrypt64(tksnr, curkey);
                curkey = ISOUtil.xor(tksnr, curkey);
            }
            shr(shiftr);
        }
        while (notZero(shiftr));
        curkey[7] ^= 0xFF;
        return curkey;
    }
    private byte[] calculateDerivedKeyTDES(KeySerialNumber ksn, SecureDESKey bdk, boolean dataEncryption)
            throws SMException
    {
        final byte[] _1FFFFF =
                new byte[]{(byte) 0x1F, (byte) 0xFF, (byte) 0xFF};
        final byte[] _100000 =
                new byte[]{(byte) 0x10, (byte) 0x00, (byte) 0x00};
        final byte[] _E00000 =
                new byte[]{(byte) 0xE0, (byte) 0x00, (byte) 0x00};

        byte[] curkey = calculateInitialKey(ksn, bdk, true);

        byte[] smidr = ISOUtil.hex2byte(
                ksn.getBaseKeyID() + ksn.getDeviceID() + ksn.getTransactionCounter()
        );
        byte[] reg3 = ISOUtil.hex2byte(ksn.getTransactionCounter());
        reg3 = and(reg3, _1FFFFF);
        byte[] shiftr = _100000;
        byte[] temp;
        byte[] tksnr;
        byte[] r8a;
        byte[] r8b;
        byte[] curkeyL = new byte[8];
        byte[] curkeyR = new byte[8];
        smidr = and(smidr, _E00000, 5);
        do
        {
            temp = and(shiftr, reg3);
            if (notZero(temp))
            {
                System.arraycopy(curkey, 0, curkeyL, 0, 8);
                System.arraycopy(curkey, 8, curkeyR, 0, 8);

                or(smidr, shiftr, 5);
                // smidr == R8

                tksnr = ISOUtil.xor(smidr, curkeyR);
                tksnr = encrypt64(tksnr, curkeyL);
                tksnr = ISOUtil.xor(tksnr, curkeyR);
                // tksnr == R8A
                curkeyL = ISOUtil.xor(curkeyL, _VARIANT_RIGHT_HALF);
                curkeyR = ISOUtil.xor(curkeyR, _VARIANT_RIGHT_HALF);

                r8b = ISOUtil.xor(smidr, curkeyR);
                r8b = encrypt64(r8b, curkeyL);
                r8b = ISOUtil.xor(r8b, curkeyR);

                System.arraycopy(r8b, 0, curkey, 0, 8);
                System.arraycopy(tksnr, 0, curkey, 8, 8);
            }
            shr(shiftr);
        }
        while (notZero(shiftr));

        if (dataEncryption) {
            curkey[5] ^= 0xFF;
            curkey[13] ^= 0xFF;
            System.arraycopy(curkey, 0, curkeyL, 0, 8);
            System.arraycopy(curkey, 8, curkeyR, 0, 8);
            byte[] L = encrypt64(curkeyL, curkeyL);
            L = decrypt64(L, curkeyR);
            L = encrypt64(L, curkeyL);

            byte[] R = encrypt64(curkeyR, curkeyL); // this is the right implementation
            R = decrypt64(R, curkeyR);
            R = encrypt64(R, curkeyL);
            System.arraycopy (L, 0, curkey, 0, 8);
            System.arraycopy (R, 0, curkey, 8, 8);
        } else {
            curkey[7] ^= 0xFF;
            curkey[15] ^= 0xFF;
        }
        return curkey;
    }

    public SecureDESKey importBDK(String clearComponent1HexString,
                                  String clearComponent2HexString,
                                  String clearComponent3HexString) throws SMException
    {
        return formKEYfromThreeClearComponents((short) 128, "BDK",
                clearComponent1HexString,
                clearComponent2HexString,
                clearComponent3HexString);
    }

    private KeySerialNumber getKSN(String s)
    {
        return new KeySerialNumber(
                s.substring(0, 6),
                s.substring(6, 10),
                s.substring(10, Math.min(s.length(), 20))
        );
    }

    protected EncryptedPIN translatePINImpl
            (EncryptedPIN pinUnderDuk, KeySerialNumber ksn,
             SecureDESKey bdk, SecureDESKey kd2, byte destinationPINBlockFormat,boolean tdes)
            throws SMException
    {
        byte[] derivedKey = calculateDerivedKey(ksn, bdk, tdes, false);
        byte[] clearPinblk = specialDecrypt(
                pinUnderDuk.getPINBlock(), derivedKey
        );

        String pan = pinUnderDuk.getAccountNumber();
        String pin = calculatePIN(
                clearPinblk, pinUnderDuk.getPINBlockFormat(), pan
        );
        byte[] translatedPinblk = jceHandler.encryptData(
                calculatePINBlock(pin, destinationPINBlockFormat, pan),
                decryptFromLMK(kd2)
        );
        return new EncryptedPIN(translatedPinblk, destinationPINBlockFormat, pan,false);
    }
    protected EncryptedPIN importPINImpl
            (EncryptedPIN pinUnderDuk, KeySerialNumber ksn, SecureDESKey bdk,boolean tdes)
            throws SMException
    {
        byte[] derivedKey = calculateDerivedKey(ksn, bdk,tdes, false);
        byte[] clearPinblk = specialDecrypt(
                pinUnderDuk.getPINBlock(), derivedKey
        );
        String pan = pinUnderDuk.getAccountNumber();
        String pin = calculatePIN(
                clearPinblk, pinUnderDuk.getPINBlockFormat(), pan
        );

        byte[] pinUnderLmk = jceHandler.encryptData(
                calculatePINBlock(pin, SMAdapter.FORMAT00, pan),
                getLMK(PINLMKIndex)
        );
        return new EncryptedPIN(pinUnderLmk, SMAdapter.FORMAT00, pan,false);
    }

    /**
     * Exports PIN to DUKPT Encryption.
     *
     * @param pinUnderLmk
     * @param ksn
     * @param bdk
     * @param tdes
     * @param destinationPINBlockFormat
     * @return The encrypted pin
     * @throws SMException
     */
    public EncryptedPIN exportPIN
            (EncryptedPIN pinUnderLmk, KeySerialNumber ksn, SecureDESKey bdk, boolean tdes,
             byte destinationPINBlockFormat)
            throws SMException
    {
        String accountNumber = pinUnderLmk.getAccountNumber();
        // process
        // get clear PIN
        byte[] clearPINBlock = jceHandler.decryptData(pinUnderLmk.getPINBlock(),
                                                      getLMK(PINLMKIndex));
        // extract clear pin
        String pin = calculatePIN(clearPINBlock, pinUnderLmk.getPINBlockFormat(),
                                  accountNumber);

        clearPINBlock = calculatePINBlock(pin, destinationPINBlockFormat, accountNumber);

        // encrypt PIN
        byte[] derivedKey = calculateDerivedKey(ksn, bdk, tdes, false);
        byte[] translatedPINBlock = specialEncrypt(clearPINBlock, derivedKey);

        return new EncryptedPIN(translatedPINBlock, destinationPINBlockFormat,
                                accountNumber, false);
    }

    /**
     * places a length indicator in a byte array.
     * @param b the byte array
     * @return a byte array with a two-byte length indicator
     */
    private byte[] lpack (byte[] b) {
        int l = b.length;
        int adjustedLen = ((l+9) >> 3) << 3;
        byte[] d = new byte[adjustedLen];
        System.arraycopy (b, 0, d, 2, l);
        d[0] = (byte) ((l >> 8) & 0xFF);
        d[1] = (byte) (l & 0xFF);
        return d;
    }
    /**
     * Unpacks a byte array packed by lPack
     * into the former byte[]
     * @param b packed byte array
     * @return original (unpacked) byte array
     */
    private byte[] lunpack (byte[] b) {
        int l = ((((int)b[0])&0xFF) << 8) | (((int)b[1])&0xFF);
        byte[] d = new byte[l];
        System.arraycopy (b,2,d,0,l);
        return d;
    }

    private byte[] hash8 (byte[][] bb) throws SMException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            for (byte[] b : bb) {
                md.update(b);
            }
            return Arrays.copyOf(md.digest(), 8);
        } catch (NoSuchAlgorithmException e) {
            throw new SMException (e);
        }
    }
}

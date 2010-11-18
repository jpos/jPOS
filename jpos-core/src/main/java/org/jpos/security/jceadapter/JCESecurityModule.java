/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2010 Alejandro P. Revilla
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

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOUtil;
import org.jpos.security.*;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.jpos.util.SimpleMsg;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.Provider;
import java.security.Security;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;


/**
 * <p>
 * JCESecurityModule is an implementation of a security module in software.
 * It doesn't require any hardware device to work.<br>
 * JCESecurityModule also implements the SMAdapter, so you can view it: either
 * as a self contained security module adapter that doesn't need a security module
 * or a security module that plugs directly to jpos, so doesn't need
 * a separate adapter.<br>
 * It relies on Java(tm) Cryptography Extension (JCE), hence its name.<br>
 * JCESecurityModule relies on the JCEHandler class to do the low level JCE work.
 * </p>
 * <p>
 * WARNING: This version of JCESecurityModule is meant for testing purposes and
 * NOT for life operation, since the Local Master Keys are stored in CLEAR on
 * the system's disk. Comming versions of JCESecurity Module will rely on
 * java.security.KeyStore for a better protection of the Local Master Keys.
 * </p>
 * @author Hani Samuel Kirollos
 * @version $Revision$ $Date$
 */
public class JCESecurityModule extends BaseSMAdapter {

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
        init(null, lmkFile, false);
    }

    public JCESecurityModule (String lmkFile, String jceProviderClassName) throws SMException
    {
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
    public void setConfiguration (Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;
        try {
            init(cfg.get("provider"), cfg.get("lmk"), cfg.getBoolean("rebuildlmk"));
        } catch (SMException e) {
            throw  new ConfigurationException(e);
        }
    }

    public SecureDESKey generateKeyImpl (short keyLength, String keyType) throws SMException {
        SecureDESKey generatedSecureKey = null;
        Key generatedClearKey = jceHandler.generateDESKey(keyLength);
        generatedSecureKey = encryptToLMK(keyLength, keyType, generatedClearKey);
        return  generatedSecureKey;
    }

    public SecureDESKey importKeyImpl (short keyLength, String keyType, byte[] encryptedKey,
            SecureDESKey kek, boolean checkParity) throws SMException {
        SecureDESKey importedKey = null;
        // decrypt encrypted key
        Key clearKEY = jceHandler.decryptDESKey(keyLength, encryptedKey, decryptFromLMK(kek),
                checkParity);
        // Encrypt Key under LMK
        importedKey = encryptToLMK(keyLength, keyType, clearKEY);
        return  importedKey;
    }

    public byte[] exportKeyImpl (SecureDESKey key, SecureDESKey kek) throws SMException {
        byte[] exportedKey = null;
        // get key in clear
        Key clearKey = decryptFromLMK(key);
        // Encrypt key under kek
        exportedKey = jceHandler.encryptDESKey(key.getKeyLength(), clearKey, decryptFromLMK(kek));
        return  exportedKey;
    }

    private int getKeyTypeIndex (short keyLength, String keyType) throws SMException {
        int index = 0;
        if (keyType==null)
            return index;
        StringTokenizer st = new StringTokenizer(keyType,":;");
        if (st.hasMoreTokens()){
           String majorType = st.nextToken();
           Integer idx = keyTypeToLMKIndex.get(majorType);
           if (idx==null)
              throw new SMException("Unsupported key type: " + majorType);
           index = idx;
        }
        if (st.hasMoreTokens())
            try {
                index |= Integer.valueOf(st.nextToken().substring(0,1)) << 8;
            } catch (Exception ex){}
        return index;
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
        StringTokenizer st = new StringTokenizer(keyType,":;");
        if (st.hasMoreTokens())
            st.nextToken();
        if (st.hasMoreTokens())
            try {
                scheme = KeyScheme.valueOf(st.nextToken().substring(1,2));
            } catch (Exception ex){}
        return scheme;
    }

    public EncryptedPIN encryptPINImpl (String pin, String accountNumber) throws SMException {
        EncryptedPIN encryptedPIN = null;
        byte[] clearPINBlock = calculatePINBlock(pin, FORMAT00, accountNumber);
        // Encrypt
        byte[] translatedPINBlock = jceHandler.encryptData(clearPINBlock, getLMK(PINLMKIndex));
        encryptedPIN = new EncryptedPIN(translatedPINBlock, FORMAT00, accountNumber);
        return  encryptedPIN;
    }

    public String decryptPINImpl (EncryptedPIN pinUnderLmk) throws SMException {
        String pin = null;
        byte[] clearPINBlock = jceHandler.decryptData(pinUnderLmk.getPINBlock(),
                getLMK(PINLMKIndex));
        pin = calculatePIN(clearPINBlock, pinUnderLmk.getPINBlockFormat(), pinUnderLmk.getAccountNumber());
        return  pin;
    }

    public EncryptedPIN importPINImpl (EncryptedPIN pinUnderKd1, SecureDESKey kd1) throws SMException {
        EncryptedPIN pinUnderLmk = null;
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
        pinUnderLmk = new EncryptedPIN(translatedPINBlock, destinationPINBlockFormat,
                accountNumber);
        return  pinUnderLmk;
    }

    public EncryptedPIN exportPINImpl (EncryptedPIN pinUnderLmk, SecureDESKey kd2,
            byte destinationPINBlockFormat) throws SMException {
        EncryptedPIN exportedPIN = null;
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
        exportedPIN = new EncryptedPIN(translatedPINBlock, destinationPINBlockFormat,
                accountNumber);
        return  exportedPIN;
    }

    public EncryptedPIN translatePINImpl (EncryptedPIN pinUnderKd1, SecureDESKey kd1,
            SecureDESKey kd2, byte destinationPINBlockFormat) throws SMException {
        EncryptedPIN translatedPIN = null;
        String accountNumber = pinUnderKd1.getAccountNumber();
        // get clear PIN
        byte[] clearPINBlock = jceHandler.decryptData(pinUnderKd1.getPINBlock(),
                decryptFromLMK(kd1));
        String pin = calculatePIN(clearPINBlock, pinUnderKd1.getPINBlockFormat(),
                accountNumber);
        // Reformat PIN Block
        clearPINBlock = calculatePINBlock(pin, destinationPINBlockFormat, accountNumber);
        // encrypt PIN
        byte[] translatedPINBlock = jceHandler.encryptData(clearPINBlock, decryptFromLMK(kd2));
        translatedPIN = new EncryptedPIN(translatedPINBlock, destinationPINBlockFormat,
                accountNumber);
        return  translatedPIN;
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
     * Generates a random clear key component.<br>
     * Used by Console, that's why it is package protected.
     * @param keyLength
     * @return clear key componenet
     * @throws SMException
     */
    String generateClearKeyComponent (short keyLength) throws SMException {
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
    protected byte[] generateKeyCheckValueImpl (SecureDESKey secureDESKey) throws SMException {
        return  calculateKeyCheckValue(decryptFromLMK(secureDESKey));
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
    SecureDESKey formKEYfromThreeClearComponents (short keyLength, String keyType,
            String clearComponent1HexString, String clearComponent2HexString, String clearComponent3HexString) throws SMException {
        SecureDESKey secureDESKey;
        LogEvent evt = new LogEvent(this, "s-m-operation");
        
        try {
            byte[] clearComponent1 = ISOUtil.hex2byte(clearComponent1HexString);
            byte[] clearComponent2 = ISOUtil.hex2byte(clearComponent2HexString);
            byte[] clearComponent3 = ISOUtil.hex2byte(clearComponent3HexString);
            byte[] clearKeyBytes = ISOUtil.xor(ISOUtil.xor(clearComponent1, clearComponent2),
                    clearComponent3);
            Key clearKey = null;
            clearKey = jceHandler.formDESKey(keyLength, clearKeyBytes);
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

    /**
     * Calculates a key check value over a clear key
     * @param key
     * @return the key check value
     * @exception SMException
     */
    byte[] calculateKeyCheckValue (Key key) throws SMException {
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
    private SecureDESKey encryptToLMK (short keyLength, String keyType, Key clearDESKey) throws SMException {
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
    private Key decryptFromLMK (SecureDESKey secureDESKey) throws SMException {
        Key key = null;
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
        if (true && !Util.isDESParityAdjusted(clearKeyBytes))
            throw new JCEHandlerException("Parity not adjusted");
        key = jceHandler.formDESKey(secureDESKey.getKeyLength(), clearKeyBytes);
        return key;
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
    private byte[] calculatePINBlock (String pin, byte pinBlockFormat, String accountNumber) throws SMException {
        byte[] pinBlock = null;
        if (pin.length() > MAX_PIN_LENGTH)
            throw  new SMException("Invalid PIN length: " + pin.length());
        if (accountNumber.length() != 12)
            throw  new SMException("Invalid Account Number: " + accountNumber + ". The length of the account number must be 12 (the 12 right-most digits of the account number excluding the check digit)");
        switch (pinBlockFormat) {
            case FORMAT00: // same as FORMAT01
            case FORMAT01:
                {
                    // Block 1
                    String block1 = null;
                    byte[] block1ByteArray;
                    switch (pin.length()) {
                        // pin length then pad with 'F'
                        case 4:
                            block1 = "04" + pin + "FFFFFFFFFF";
                            break;
                        case 5:
                            block1 = "05" + pin + "FFFFFFFFF";
                            break;
                        case 6:
                            block1 = "06" + pin + "FFFFFFFF";
                            break;
                        case 7:
                            block1 = "07" + pin + "FFFFFFF";
                            break;
                        case 8:
                            block1 = "08" + pin + "FFFFFF";
                            break;
                        default:
                            throw  new SMException("Unsupported PIN Length: " +
                                    pin.length());
                    }
                    block1ByteArray = ISOUtil.hex2byte(block1);
                    // Block 2
                    String block2;
                    byte[] block2ByteArray = null;
                    block2 = "0000" + accountNumber;
                    block2ByteArray = ISOUtil.hex2byte(block2);
                    // pinBlock
                    pinBlock = ISOUtil.xor(block1ByteArray, block2ByteArray);
                }
                break;
            case FORMAT03: 
                {
                    if(pin.length() < 4 || pin.length() > 12) 
                        throw new SMException("Unsupported PIN Length: " + 
                                pin.length());
                    pinBlock = ISOUtil.hex2byte (
                        pin + "FFFFFFFFFFFFFFFF".substring(pin.length(),16)
                    );
                }
                break;
            default:
                throw  new SMException("Unsupported PIN format: " + pinBlockFormat);
        }
        return  pinBlock;
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
    private String calculatePIN (byte[] pinBlock, byte pinBlockFormat, String accountNumber) throws SMException {
        String pin = null;
        int pinLength;
        if (accountNumber.length() != 12)
            throw  new SMException("Invalid Account Number: " + accountNumber + ". The length of the account number must be 12 (the 12 right-most digits of the account number excluding the check digit)");
        switch (pinBlockFormat) {
            case FORMAT00: // same as format 01
            case FORMAT01:
                {
                    // Block 2
                    String block2;
                    block2 = "0000" + accountNumber;
                    byte[] block2ByteArray = ISOUtil.hex2byte(block2);
                    // get Block1
                    byte[] block1ByteArray = ISOUtil.xor(pinBlock, block2ByteArray);
                    pinLength = Math.abs (block1ByteArray[0]);
                    if (pinLength > MAX_PIN_LENGTH)
                        throw  new SMException("PIN Block Error");
                    // get pin
                    String pinBlockHexString = ISOUtil.hexString(block1ByteArray);
                    pin = pinBlockHexString.substring(2, pinLength
                            + 2);
                    String pad = pinBlockHexString.substring(pinLength + 2);
                    pad = pad.toUpperCase();
                    int i = pad.length();
                    while (--i >= 0)
                        if (pad.charAt(i) != 'F')
                            throw new SMException("PIN Block Error");
                }
                break;
            case FORMAT03: 
                {
                    String block1 = ISOUtil.hexString(pinBlock);
                    int len = block1.indexOf('F');
                    if(len == -1) len = 12;
                    int i = block1.length();
                    pin = block1.substring(0, len);

                    while(--i >= len) 
                        if(block1.charAt(i) != 'F') 
                            throw new SMException("PIN Block Error");
                    while(--i >= 0) 
                        if(pin.charAt(i) >= 'A') 
                            throw new SMException("PIN Block Error");

                    if(pin.length() < 4 || pin.length() > 12) 
                        throw new SMException("Unsupported PIN Length: " + 
                                pin.length());
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
        File lmk = new File(lmkFile);
        try {
            keyTypeToLMKIndex = new TreeMap<String,Integer>();
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
            Provider provider = null;
            LogEvent evt = new LogEvent(this, "jce-provider");
            try {
                if ((jceProviderClassName == null) || (jceProviderClassName.compareTo("")
                        == 0)) {
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
                evt.addMessage("Rebuilding new Local Master Keys in file: \"" +
                        lmk.getCanonicalPath() + "\".");
                Logger.log(evt);
                // Generate New random Local Master Keys
                generateLMK();
                // Write the new Local Master Keys to file
                writeLMK(lmk);
                evt = new LogEvent(this, "local-master-keys");
                evt.addMessage("Local Master Keys built successfully in file: \""
                        + lmk.getCanonicalPath() + "\".");
                Logger.log(evt);
            }
            if (!lmk.exists()) {
                // LMK File does not exist
                throw  new SMException("Error loading Local Master Keys, file: \""
                        + lmk.getCanonicalPath() + "\" does not exist." + " Please specify a valid LMK file, or rebuild a new one.");
            }
            else {
                // Read LMK from file
                readLMK(lmk);
                evt = new LogEvent(this, "local-master-keys");
                evt.addMessage("Loaded successfully from file: \"" + lmk.getCanonicalPath()
                        + "\"");
                Logger.log(evt);
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
            FileInputStream in = new FileInputStream(lmkFile);
            lmkProps.load(in);
            in.close();
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
            FileOutputStream out = new FileOutputStream(lmkFile);
            lmkProps.store(out, "Local Master Keys");
            out.close();
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
    private Map<Integer,SecretKey> lmks = new TreeMap<Integer,SecretKey>();
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
     * The maximum length of the PIN
     */
    private static final short MAX_PIN_LENGTH = 12;
    /**
     * a dummy 64-bit block of zeros used when calculating the check value
     */
    private static final byte[] zeroBlock =  {
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, (byte)0x00
    };
    private JCEHandler jceHandler;
}




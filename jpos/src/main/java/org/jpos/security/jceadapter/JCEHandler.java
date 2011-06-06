/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2011 Alejandro P. Revilla
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

import org.jpos.iso.ISOUtil;
import org.jpos.security.SMAdapter;
import org.jpos.security.Util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Map;
import java.util.HashMap;


/**
 * <p>
 * Provides some higher level methods that are needed by the JCE
 * Security Module, yet they are generic and can be used elsewhere.
 * </p>
 * <p>
 * It depends on the Java<font size=-1><sup>TM</sup></font> Cryptography Extension (JCE).
 * </p>
 * @author Hani S. Kirollos
 * @version $Revision$ $Date$
 */
public class JCEHandler {
    static final String ALG_DES = "DES";
    static final String ALG_TRIPLE_DES = "DESede";

    /**
     * Registers the JCE provider whose name is providerName and sets it to be the
     * only provider to be used in this instance of the JCEHandler class.
     * @param jceProviderClassName Name of the JCE provider
     * (e.g. "com.sun.crypto.provider.SunJCE" for Sun's implementation,
     * or "org.bouncycastle.jce.provider.BouncyCastleProvider" for bouncycastle.org
     * implementation)
     * @throws JCEHandlerException
     */
    public JCEHandler (String jceProviderClassName) throws JCEHandlerException
    {
        try {
            provider = (Provider)Class.forName(jceProviderClassName).newInstance();
            Security.addProvider(provider);
        } catch (Exception e) {
            throw  new JCEHandlerException(e);
        }
    }

    /**
     * Uses the JCE provider specified
     * @param provider
     */
    public JCEHandler (Provider provider) {
        this.provider = provider;
    }

    /**
     * Generates a clear DES (DESede) key
     * @param keyLength the bit length (key size) of the generated key (LENGTH_DES, LENGTH_DES3_2KEY or LENGTH_DES3_3KEY)
     * @return generated clear DES (or DESede) key
     * @exception JCEHandlerException
     */
    public Key generateDESKey (short keyLength) throws JCEHandlerException {
        Key generatedClearKey = null;
        try {
            KeyGenerator k1 = null;
            if (keyLength > SMAdapter.LENGTH_DES) {
                k1 = KeyGenerator.getInstance(ALG_TRIPLE_DES, provider.getName());
            }
            else {
                k1 = KeyGenerator.getInstance(ALG_DES, provider.getName());
            }
            generatedClearKey = k1.generateKey();
            /* These 3 steps not only enforce correct parity, but also enforces
             that when keyLength=128, the third key of the triple DES key is equal
             to the first key. This is needed because, JCE doesn't differenciate
             between Triple DES with 2 keys and Triple DES with 3 keys
             */
            byte[] clearKeyBytes = extractDESKeyMaterial(keyLength, generatedClearKey);
            Util.adjustDESParity(clearKeyBytes);
            generatedClearKey = formDESKey(keyLength, clearKeyBytes);
        } catch (Exception e) {
            if (e instanceof JCEHandlerException)
                throw  (JCEHandlerException)e;
            else
                throw  new JCEHandlerException(e);
        }
        return  generatedClearKey;
    }

    /**
     * Encrypts (wraps) a clear DES Key, it also sets odd parity before encryption
     * @param keyLength bit length (key size) of the clear DES key (LENGTH_DES, LENGTH_DES3_2KEY or LENGTH_DES3_3KEY)
     * @param clearDESKey DES/Triple-DES key whose format is "RAW"
     * (for a DESede with 2 Keys, keyLength = 128 bits, while DESede key with 3 keys keyLength = 192 bits)
     * @param encryptingKey can be a key of any type (RSA, DES, DESede...)
     * @return encrypted DES key
     * @throws JCEHandlerException
     */
    public byte[] encryptDESKey (short keyLength, Key clearDESKey, Key encryptingKey) throws JCEHandlerException {
        byte[] encryptedDESKey = null;
        byte[] clearKeyBytes = extractDESKeyMaterial(keyLength, clearDESKey);
        // enforce correct (odd) parity before encrypting the key
        Util.adjustDESParity(clearKeyBytes);
        encryptedDESKey = doCryptStuff(clearKeyBytes, encryptingKey, Cipher.ENCRYPT_MODE);
        return  encryptedDESKey;
    }

    /**
     * Extracts the DES/DESede key material
     * @param keyLength bit length (key size) of the DES key. (LENGTH_DES, LENGTH_DES3_2KEY or LENGTH_DES3_3KEY)
     * @param clearDESKey DES/Triple-DES key whose format is "RAW"
     * @return encoded key material
     * @throws JCEHandlerException
     */
    protected byte[] extractDESKeyMaterial (short keyLength, Key clearDESKey) throws JCEHandlerException {
        byte[] clearKeyBytes = null;
        String keyAlg = clearDESKey.getAlgorithm();
        String keyFormat = clearDESKey.getFormat();
        if (keyFormat.compareTo("RAW") != 0) {
            throw  new JCEHandlerException("Unsupported DES key encoding format: "
                    + keyFormat);
        }
        if (!keyAlg.startsWith(ALG_DES)) {
            throw  new JCEHandlerException("Unsupported key algorithm: " + keyAlg);
        }
        clearKeyBytes = clearDESKey.getEncoded();
        clearKeyBytes = ISOUtil.trim(clearKeyBytes, getBytesLength(keyLength));
        return  clearKeyBytes;
    }

    /**
     * Decrypts an encrypted DES/Triple-DES key
     * @param keyLength bit length (key size) of the DES key to be decrypted. (LENGTH_DES, LENGTH_DES3_2KEY or LENGTH_DES3_3KEY)
     * @param encryptedDESKey the byte[] representing the encrypted key
     * @param encryptingKey can be of any algorithm (RSA, DES, DESede...)
     * @param checkParity if true, the parity of the key is checked
     * @return clear DES (DESede) Key
     * @throws JCEHandlerException if checkParity==true and the key does not have correct parity
     */
    public Key decryptDESKey (short keyLength, byte[] encryptedDESKey, Key encryptingKey,
            boolean checkParity) throws JCEHandlerException {
        Key key = null;
        byte[] clearKeyBytes = doCryptStuff(encryptedDESKey, encryptingKey, Cipher.DECRYPT_MODE);
        if (checkParity) {
            if (!Util.isDESParityAdjusted(clearKeyBytes)) {
                throw new JCEHandlerException("Parity not adjusted");
            }
        }
        key = formDESKey(keyLength, clearKeyBytes);
        return  key;
    }

    /**
     * Forms the clear DES key given its "RAW" encoded bytes
     * Does the inverse of extractDESKeyMaterial
     * @param keyLength bit length (key size) of the DES key. (LENGTH_DES, LENGTH_DES3_2KEY or LENGTH_DES3_3KEY)
     * @param clearKeyBytes the RAW DES/Triple-DES key
     * @return clear key
     * @throws JCEHandlerException
     */
    protected Key formDESKey (short keyLength, byte[] clearKeyBytes) throws JCEHandlerException {
        Key key = null;
        switch (keyLength) {
            case SMAdapter.LENGTH_DES:
                {
                    key = new SecretKeySpec(clearKeyBytes, ALG_DES);
                }
                break;
            case SMAdapter.LENGTH_DES3_2KEY:
                {
                    // make it 3 components to work with JCE
                    clearKeyBytes = ISOUtil.concat(
                        clearKeyBytes, 0, getBytesLength(SMAdapter.LENGTH_DES3_2KEY),
                        clearKeyBytes, 0, getBytesLength(SMAdapter.LENGTH_DES)
                        );
                }
            case SMAdapter.LENGTH_DES3_3KEY:
                {
                    key = new SecretKeySpec(clearKeyBytes, ALG_TRIPLE_DES);
                }
        }
        if (key == null)
            throw  new JCEHandlerException("Unsupported DES key length: " + keyLength
                    + " bits");
        return  key;
    }

    /**
     * Encrypts data
     * @param data
     * @param key
     * @return encrypted data
     * @exception JCEHandlerException
     */
    public byte[] encryptData (byte[] data, Key key) throws JCEHandlerException {
        byte[] encryptedData;
        encryptedData = doCryptStuff(data, key, Cipher.ENCRYPT_MODE);
        return  encryptedData;
    }

    /**
     * Decrypts data
     * @param encryptedData
     * @param key
     * @return clear data
     * @exception JCEHandlerException
     */
    public byte[] decryptData (byte[] encryptedData, Key key) throws JCEHandlerException {
        byte[] clearData;
        clearData = doCryptStuff(encryptedData, key, Cipher.DECRYPT_MODE);
        return  clearData;
    }

    /**
     * performs cryptographic operations (encryption/decryption) using JCE Cipher
     * @param data
     * @param key
     * @param CipherMode Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE
     * @return result of the cryptographic operations
     * @throws JCEHandlerException
     */
    byte[] doCryptStuff (byte[] data, Key key, int CipherMode) throws JCEHandlerException {
        byte[] result;
        String transformation;
        if (key.getAlgorithm().startsWith(ALG_DES)) {
            transformation = key.getAlgorithm() + "/" + desMode + "/" + desPadding;
        }
        else {
            transformation = key.getAlgorithm();
        }
        try {
            Cipher c1 = Cipher.getInstance(transformation, provider.getName());
            c1.init(CipherMode, key);
            result = c1.doFinal(data);
        } catch (Exception e) {
            throw  new JCEHandlerException(e);
        }
        return  result;
    }

    /**
     * Calculates the length of key in bytes
     * @param keyLength bit length (key size) of the DES key. (LENGTH_DES, LENGTH_DES3_2KEY or LENGTH_DES3_3KEY)
     * @return keyLength/8
     * @throws JCEHandlerException if unknown key length
     */
    int getBytesLength(short keyLength) throws JCEHandlerException{
        int bytesLength = 0;
        switch (keyLength) {
            case SMAdapter.LENGTH_DES: bytesLength = 8;break;
            case SMAdapter.LENGTH_DES3_2KEY: bytesLength = 16;break;
            case SMAdapter.LENGTH_DES3_3KEY: bytesLength = 24; break;
            default: throw new JCEHandlerException("Unsupported key length: " + keyLength + " bits");
        }
        return bytesLength;
    }

    /**
     * Helper method used for create or retrieve MAC algorithm from cache
     * @param engine object identyifing MAC algorithm
     * @return Initialized MAC algotithm
     * @throws org.jpos.security.jceadapter.JCEHandlerException
     */
    Mac assignMACEngine(MacEngineKey engine) throws JCEHandlerException {
        macEngines = macEngines==null?new HashMap():macEngines;
        if (macEngines.containsKey(engine)) {
          return (Mac)macEngines.get(engine);
        }
        
        //Initalize new MAC engine and store them in macEngines cache
        Mac mac = null;
        try{
          mac = Mac.getInstance(engine.getMacAlgorithm(), provider);
          mac.init(engine.getMacKey());
        } catch (NoSuchAlgorithmException e) {
          throw new JCEHandlerException(e);
        } catch (InvalidKeyException e) {
          throw new JCEHandlerException(e);
        }
        macEngines.put(engine, mac);
        return mac;
    }

    /**
     * Generates MAC (Message Message Authentication Code)
     * for some data.
     * @param data the data to be MACed
     * @param kd the key used for MACing
     * @param macAlgorithm MAC algorithm name suitable for {@link Mac#getInstance}
     * @return the MAC
     * @throws org.jpos.security.jceadapter.JCEHandlerException
     */
    public byte[] generateMAC(byte[] data, Key kd, String macAlgorithm) throws JCEHandlerException {
        Mac mac = assignMACEngine(new MacEngineKey(macAlgorithm,kd));
        synchronized (mac){
          mac.reset();
          return mac.doFinal(data);
        }
    }

    /**
     * The JCE provider
     */
    Provider provider = null;
    Map macEngines = null;
    String desMode = "ECB";
    String desPadding = "NoPadding";
    
    /**
     * Class used for indexing MAC algorithms in cache
     */
    protected class MacEngineKey{
      private String     macAlgorithm;
      private Key macKey;

      protected MacEngineKey(String macAlgorithm, Key macKey) {
        this.macAlgorithm = macAlgorithm;
        this.macKey = macKey;
      }

      public String getMacAlgorithm() {
        return macAlgorithm;
      }

      public Key getMacKey() {
        return macKey;
      }

      public boolean equals(Object obj) {
        if (obj == null) {
          return false;
        }
        if (getClass() != obj.getClass()) {
          return false;
        }
        final MacEngineKey other = (MacEngineKey) obj;
        if (this.macAlgorithm != other.macAlgorithm && (this.macAlgorithm == null || !this.macAlgorithm.equals(other.macAlgorithm))) {
          return false;
        }
        return !(this.macKey != other.macKey && (this.macKey == null || !this.macKey.equals(other.macKey)));
      }

      public int hashCode() {
        int hash = 5;
        hash = 67 * hash + (this.macAlgorithm != null ? this.macAlgorithm.hashCode() : 0);
        hash = 67 * hash + (this.macKey != null ? this.macKey.hashCode() : 0);
        return hash;
      }
    }
}




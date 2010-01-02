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

package  org.jpos.security;


/**
 * A class that implements the SecurityModuleAdapter interface would act as an
 * adapter to the real security module device (by communicating with it using
 * its proprietary protocol).
 *
 * But application programmers will be communicating
 * with the security module using this simple interface.
 *
 * @todo support for PIN Verification API's and RSA
 * @author Hani S. Kirollos
 * @version $Revision$ $Date$
 */
public interface SMAdapter {
    /**
     * DES Key Length <code>LENGTH_DES</code> = 64.
     */
    public static final short LENGTH_DES = 64;
    /**
     * Triple DES (2 keys) <code>LENGTH_DES3_2KEY</code> = 128.
     */
    public static final short LENGTH_DES3_2KEY = 128;
    /**
     * Triple DES (3 keys) <code>LENGTH_DES3_3KEY</code> = 192.
     */
    public static final short LENGTH_DES3_3KEY = 192;
    /**
     * ZMK: Zone Master Key is a DES (or Triple-DES) key-encryption key which is distributed
     * manually in order that further keys can be exchanged automatically.
     */
    public static final String TYPE_ZMK = "ZMK";

    /**
     * ZPK: Zone PIN Key.
     *
     * is a DES (or Triple-DES) data-encrypting key which is distributed
     * automatically and is used to encrypt PINs for transfer between
     * communicating parties (e.g. between acquirers and issuers).
     */
    public static final String TYPE_ZPK = "ZPK";

    /**
     * TMK: Terminal Master Key.
     *
     * is a  DES (or Triple-DES) key-encrypting key which is distributed
     * manually, or automatically under a previously installed TMK. It is
     * used to distribute data-encrypting keys, whithin a local network,
     * to an ATM or POS terminal or similar.
     */
    public static final String TYPE_TMK = "TMK";

    /**
     * TPK: Terminal PIN Key.
     *
     * is a  DES (or Triple-DES) data-encrypting key which is used
     * to encrypt PINs for transmission, within a local network,
     * between the terminal and the terminal data acquirer.
     */
    public static final String TYPE_TPK = "TPK";

    /**
     * TAK: Terminal Authentication Key.
     *
     * is a  DES (or Triple-DES) data-encrypting key which is used to
     * generate and verify a Message Authentication Code (MAC) when data
     * is transmitted, within a local network, between the terminal and
     * the terminal data acquirer.
     */
    public static final String TYPE_TAK = "TAK";

    /**
     * PVK: PIN Verification Key.
     * is a  DES (or Triple-DES) data-encrypting key which is used to
     * generate and verify PIN verification data and thus verify the
     * authenticity of a PIN.
     */
    public static final String TYPE_PVK = "PVK";

    /**
     * CVK: Card Verification Key.
     *
     * is similar for PVK but for card information instead of PIN
     */
    public static final String TYPE_CVK = "CVK";

    /**
     * BDK: Base Derivation Key.
     * is a  Triple-DES key-encryption key used to derive transaction
     * keys in DUKPT (see ANSI X9.24)
     */
    public static final String TYPE_BDK = "BDK";

    /**
     * ZAK: Zone Authentication Key.
     *
     * a  DES (or Triple-DES) data-encrypting key that is distributed
     * automatically, and is used to generate and verify a Message
     * Authentication Code (MAC) when data is transmitted between
     * communicating parties (e.g. between acquirers and issuers)
     */
    public static final String TYPE_ZAK = "ZAK";

    /**
     * PIN Block Format adopted by ANSI (ANSI X9.8) and is one of
     * two formats supported by the ISO (ISO 95641 - format 0).
     */
    public static final byte FORMAT01 = (byte)01;

    /**
     * PIN Block Format 02 supports Douctel ATMs.
     */
    public static final byte FORMAT02 = (byte)02;

    /**
         * PIN Block Format 03 is the Diabold Pin Block format.
         */
    public static final byte FORMAT03 = (byte)03;

    /**
     * PIN Block Format 04 is the PIN block format adopted
     * by the PLUS network.
     */
    public static final byte FORMAT04 = (byte)04;

    /**
     * PIN Block Format 05 is the ISO 9564-1 Format 1 PIN Block.
     */
    public static final byte FORMAT05 = (byte)05;
    /**
     * Proprietary PIN Block format.
     *
     * Most Security Modules use a proprietary PIN Block format
     * when encrypting the PIN under the LMK of the Security Module
     * hence this format (FORMAT00).
     *
     * <p>
     * This is not a standard format, every Security Module would
     * interpret FORMAT00 differently.
     *
     * So, no interchange would accept PIN Blocks from other interchanges
     * using this format. It is useful only when working with PIN's inside
     * your own interchange.
     * </p>
     */
    public static final byte FORMAT00 = (byte)00;

    /**
     * Generates a random DES Key.
     *
     * @param keyType type of the key to be generated (TYPE_ZMK, TYPE_TMK...etc)
     * @param keyLength bit length of the key to be generated (LENGTH_DES, LENGTH_DES3_2KEY...)
     * @return the random key secured by the security module<BR>
     * @throws SMException
     */
    public SecureDESKey generateKey (short keyLength, String keyType) throws SMException;



    /**
     * Generates key check value.<br>
     * @param key SecureDESKey with untrusted or fake Key Check Value
     * @return key check value bytes
     * @throws SMException
     */
    public byte[] generateKeyCheckValue (SecureDESKey kd) throws SMException;



    /**
     * Imports a key from encryption under a KEK (Key-Encrypting Key)
     * to protection under the security module.
     *
     * @param keyLength bit length of the key to be imported (LENGTH_DES, LENGTH_DES3_2KEY...etc)
     * @param keyType type of the key to be imported (TYPE_ZMK, TYPE_TMK...etc)
     * @param encryptedKey key to be imported encrypted under KEK
     * @param kek the key-encrypting key
     * @param checkParity if true, the key is not imported unless it has adjusted parity
     * @return imported key secured by the security module
     * @throws SMException if the parity of the imported key is not adjusted AND checkParity = true
     */
    public SecureDESKey importKey (short keyLength, String keyType, byte[] encryptedKey,
            SecureDESKey kek, boolean checkParity) throws SMException;



    /**
     * Exports secure key to encryption under a KEK (Key-Encrypting Key).
     * @param key the secure key to be exported
     * @param kek the key-encrypting key
     * @return the exported key (key encrypted under kek)
     * @throws SMException
     */
    public byte[] exportKey (SecureDESKey key, SecureDESKey kek) throws SMException;

    /**
     * Encrypts a clear pin under LMK.
     *
     * CAUTION: The use of clear pin presents a significant security risk
     * @param pin clear pin as entered by card holder
     * @param accountNumber The 12 right-most digits of the account number excluding the check digit. Should also function correctly if the complete account number, including the check digit is passed.
     * @return PIN under LMK
     * @throws SMException
     */
    public EncryptedPIN encryptPIN (String pin, String accountNumber) throws SMException;

    /**
     * Decrypts an Encrypted PIN (under LMK).
     * CAUTION: The use of clear pin presents a significant security risk
     * @param pinUnderLmk
     * @return clear pin as entered by card holder
     * @throws SMException
     */
    public String decryptPIN (EncryptedPIN pinUnderLmk) throws SMException;

    /**
     * Imports a PIN from encryption under KD (Data Key)
     * to encryption under LMK.
     *
     * @param pinUnderKd1 the encrypted PIN
     * @param kd1 Data Key under which the pin is encrypted
     * @return pin encrypted under LMK
     * @throws SMException
     */
    public EncryptedPIN importPIN (EncryptedPIN pinUnderKd1, SecureDESKey kd1) throws SMException;



    /**
     * Translates a PIN from encrytion under KD1 to encryption under KD2.
     *
     * @param pinUnderKd1 pin encrypted under KD1
     * @param kd1 Data Key (also called session key) under which the pin is encrypted
     * @param kd2 the destination Data Key 2 under which the pin will be encrypted
     * @param destinationPINBlockFormat the PIN Block Format of the exported encrypted PIN
     * @return pin encrypted under KD2
     * @throws SMException
     */
    public EncryptedPIN translatePIN (EncryptedPIN pinUnderKd1, SecureDESKey kd1,
            SecureDESKey kd2, byte destinationPINBlockFormat) throws SMException;



    /**
     * Imports a PIN from encryption under a transaction key to encryption
     * under LMK.
     *
     * The transaction key is derived from the Key Serial Number and the Base Derivation Key using DUKPT (Derived Unique Key per Transaction). See ANSI X9.24 for more information.
     * @param pinUnderDuk pin encrypted under a transaction key
     * @param ksn Key Serial Number (also called Key Name, in ANSI X9.24) needed to derive the transaction key
     * @param bdk Base Derivation Key, used to derive the transaction key underwhich the pin is encrypted
     * @return pin encrypted under LMK
     * @throws SMException
     */
    public EncryptedPIN importPIN (EncryptedPIN pinUnderDuk, KeySerialNumber ksn,
            SecureDESKey bdk) throws SMException;



    /**
     * Translates a PIN from encryption under a transaction key to
     * encryption under a KD (Data Key).
     *
     * The transaction key is derived from the Key Serial Number and the Base Derivation Key using DUKPT (Derived Unique Key per Transaction). See ANSI X9.24 for more information.
     * @param pinUnderDuk pin encrypted under a DUKPT transaction key
     * @param ksn Key Serial Number (also called Key Name, in ANSI X9.24) needed to derive the transaction key
     * @param bdk Base Derivation Key, used to derive the transaction key underwhich the pin is encrypted
     * @param kd2 the destination Data Key (also called session key) under which the pin will be encrypted
     * @param destinationPINBlockFormat the PIN Block Format of the translated encrypted PIN
     * @return pin encrypted under kd2
     * @throws SMException
     */
    public EncryptedPIN translatePIN (EncryptedPIN pinUnderDuk, KeySerialNumber ksn,
            SecureDESKey bdk, SecureDESKey kd2, byte destinationPINBlockFormat) throws SMException;



    /**
     * Exports a PIN from encryption under LMK to encryption under a KD
     * (Data Key).
     *
     * @param pinUnderLmk pin encrypted under LMK
     * @param kd2 the destination data key (also called session key) under which the pin will be encrypted
     * @param destinationPINBlockFormat the PIN Block Format of the exported encrypted PIN
     * @return pin encrypted under kd2
     * @throws SMException
     */
    public EncryptedPIN exportPIN (EncryptedPIN pinUnderLmk, SecureDESKey kd2, byte destinationPINBlockFormat) throws SMException;



    /**
     * Generates CBC-MAC (Cipher Block Chaining Message Authentication Code)
     * for some data.
     *
     * @param data the data to be MACed
     * @param kd the key used for MACing
     * @return the MAC
     * @throws SMException
     */
    public byte[] generateCBC_MAC (byte[] data, SecureDESKey kd) throws SMException;

    /**
     * Generates EDE-MAC (Encrypt Decrypt Encrypt Message Message Authentication Code)
     * for some data.
     *
     * @param data the data to be MACed
     * @param kd the key used for MACing
     * @return the MAC
     * @throws SMException
     */
    public byte[] generateEDE_MAC (byte[] data, SecureDESKey kd) throws SMException;
    
    /**
     * Translate key from encryption under the LMK held in “key change storage”
     * to encryption under a new LMK.
     *
     * @param kd the key encrypted under old LMK
     * @return key encrypted under the new LMK
     * @throws SMException if the parity of the imported key is not adjusted AND checkParity = true
     */
    public SecureDESKey translateKeyFromOldLMK (SecureDESKey kd) throws SMException;

    /**
     * Erase the key change storage area of memory
     *
     * It is recommended that this command is used after keys stored
     * by the Host have been translated from old to new LMKs.
     *
     * @throws SMException
     */
    public void eraseOldLMK () throws SMException;
}




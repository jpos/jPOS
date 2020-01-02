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

package  org.jpos.security;

import org.javatuples.Pair;

import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * A class that implements the SMAdapter interface would act as an
 * adapter to the real security module device (by communicating with it using
 * its proprietary protocol).
 *
 * But application programmers will be communicating
 * with the security module using this simple interface.
 *
 * TODO: support for EMV card/issuer RSA generation API's
 *
 * @author Hani S. Kirollos
 * @author Robert Demski
 * @version $Revision$ $Date$
 */
public interface SMAdapter<T> {
    /**
     * DES Key Length <code>LENGTH_DES</code> = 64.
     */
    short LENGTH_DES = 64;
    /**
     * Triple DES (2 keys) <code>LENGTH_DES3_2KEY</code> = 128.
     */
    short LENGTH_DES3_2KEY = 128;
    /**
     * Triple DES (3 keys) <code>LENGTH_DES3_3KEY</code> = 192.
     */
    short LENGTH_DES3_3KEY = 192;
    /**
     * ZMK: Zone Master Key is a DES (or Triple-DES) key-encryption key which is distributed
     * manually in order that further keys can be exchanged automatically.
     */
    String TYPE_ZMK = "ZMK";

    /**
     * ZPK: Zone PIN Key.
     *
     * is a DES (or Triple-DES) data-encrypting key which is distributed
     * automatically and is used to encrypt PINs for transfer between
     * communicating parties (e.g. between acquirers and issuers).
     */
    String TYPE_ZPK = "ZPK";

    /**
     * TMK: Terminal Master Key.
     *
     * is a  DES (or Triple-DES) key-encrypting key which is distributed
     * manually, or automatically under a previously installed TMK. It is
     * used to distribute data-encrypting keys, whithin a local network,
     * to an ATM or POS terminal or similar.
     */
    String TYPE_TMK = "TMK";

    /**
     * TPK: Terminal PIN Key.
     *
     * is a  DES (or Triple-DES) data-encrypting key which is used
     * to encrypt PINs for transmission, within a local network,
     * between the terminal and the terminal data acquirer.
     */
    String TYPE_TPK = "TPK";

    /**
     * TAK: Terminal Authentication Key.
     *
     * is a  DES (or Triple-DES) data-encrypting key which is used to
     * generate and verify a Message Authentication Code (MAC) when data
     * is transmitted, within a local network, between the terminal and
     * the terminal data acquirer.
     */
    String TYPE_TAK = "TAK";

    /**
     * PVK: PIN Verification Key.
     * is a  DES (or Triple-DES) data-encrypting key which is used to
     * generate and verify PIN verification data and thus verify the
     * authenticity of a PIN.
     */
    String TYPE_PVK = "PVK";

    /**
     * CVK: Card Verification Key.
     *
     * is similar for PVK but for card information instead of PIN
     */
    String TYPE_CVK = "CVK";

    /**
     * BDK: Base Derivation Key.
     * is a  Triple-DES key-encryption key used to derive transaction
     * keys in DUKPT (see ANSI X9.24)
     */
    String TYPE_BDK = "BDK";

    /**
     * ZAK: Zone Authentication Key.
     *
     * a  DES (or Triple-DES) data-encrypting key that is distributed
     * automatically, and is used to generate and verify a Message
     * Authentication Code (MAC) when data is transmitted between
     * communicating parties (e.g. between acquirers and issuers)
     */
    String TYPE_ZAK = "ZAK";

    /**
     * MK-AC: Issuer Master Key for generating and verifying
     * Application Cryptograms.
     */
    String TYPE_MK_AC = "MK-AC";

    /**
     * MK-SMI: Issuer Master Key for Secure Messaging Integrity.
     *
     * is a Triple-DES key which is used to generating Message
     * Authrntication Codes (MAC) for scripts send to EMV chip cards.
     */
    String TYPE_MK_SMI = "MK-SMI";

    /**
     * MK-SMC: Issuer Master Key for Secure Messaging Confidentiality.
     *
     * is a Triple-DES data-encrypting key which is used to encrypt
     * data (e.g. PIN block) in scripts send to EMV chip cards.
     */
    String TYPE_MK_SMC = "MK-SMC";

    /**
     * MK-CVC3: Issuer Master Key for generating and verifying
     * Card Verification Code 3 (CVC3).
     */
    String TYPE_MK_CVC3 = "MK-CVC3";

    /**
     * MK-DAC Issuer Master Key for generating and verifying
     * Data Authentication Codes.
     */
    String TYPE_MK_DAC = "MK-DAC";

    /**
     * MK-DN: Issuer Master Key for generating and verifying
     * Dynamic Numbers.
     */
    String TYPE_MK_DN = "MK-DN";

    /**
     * ZEK: Zone Encryption Key.
     */
    String TYPE_ZEK = "ZEK";

    /**
     * DEK: Data Encryption Key.
     */
    String TYPE_DEK = "DEK";

    /**
     * RSA: Private Key.
     */
    String TYPE_RSA_SK = "RSA_SK";

    /**
     * HMAC: Hash Message Authentication Code <i>(with key usage)</i>.
     */
    String TYPE_HMAC   = "HMAC";

    /**
     * RSA: Public Key.
     */
    String TYPE_RSA_PK = "RSA_PK";

    /**
     * PIN Block Format adopted by ANSI (ANSI X9.8) and is one of
     * two formats supported by the ISO (ISO 95641 - format 0).
     */
    byte FORMAT01 = (byte)01;

    /**
     * PIN Block Format 02 supports Douctel ATMs.
     */
    byte FORMAT02 = (byte)02;

    /**
         * PIN Block Format 03 is the Diabold Pin Block format.
         */
    byte FORMAT03 = (byte)03;

    /**
     * PIN Block Format 04 is the PIN block format adopted
     * by the PLUS network.
     */
    byte FORMAT04 = (byte)04;

    /**
     * PIN Block Format 05 is the ISO 9564-1 Format 1 PIN Block.
     */
    byte FORMAT05 = (byte)05;

    /**
     * PIN Block Format 34 is the standard EMV PIN block format.
     * Is only avaliable as output of EMV PIN change commands.
     */
    byte FORMAT34 = (byte)34;

    /**
     * PIN Block Format 35 is the required by Europay/MasterCard
     * for their Pay Now & Pay Later products.
     */
    byte FORMAT35 = (byte)35;

    /**
     * PIN Block Format 41 is the Visa format for PIN change
     * without using the current PIN.
     */
    byte FORMAT41 = (byte)41;

    /**
     * PIN Block Format 42 is the Visa format for PIN change
     * using the current (old) PIN.
     */
    byte FORMAT42 = (byte)42;

    /**
     * Proprietary PIN Block format.
     * <p>
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
    byte FORMAT00 = (byte)00;

    /**
     * Generates a random DES Key.
     *
     * @param keyType type of the key to be generated (TYPE_ZMK, TYPE_TMK...etc)
     * @param keyLength bit length of the key to be generated (LENGTH_DES, LENGTH_DES3_2KEY...)
     * @return the random key secured by the security module<BR>
     * @throws SMException
     */
    SecureDESKey generateKey(short keyLength, String keyType) throws SMException;

    /**
     * Generates a random Key.
     *
     * @param keySpec the specification of the key to be generated
     *               (length, type, usage, algorithm, etc)
     * @return the random key secured by the security module
     * @throws SMException
     * @see SecureKeySpec
     */
    SecureKey generateKey(SecureKeySpec keySpec) throws SMException;


    /**
     * Generates key check value.
     *
     * @param kd the key with untrusted or fake Key Check Value
     * @return key check value bytes
     * @throws SMException
     */
    byte[] generateKeyCheckValue(T kd) throws SMException;



    /**
     * Translate Key Scheme to more secure encription.
     * <p>
     * Converts an DES key encrypted using X9.17 methods to a more secure
     * key using the variant method.
     *
     * @param key key to be translated to {@code destKeyScheme} scheme
     * @param keyScheme destination key scheme
     * @return translated key with {@code destKeyScheme} scheme
     * @throws SMException
     */
    SecureDESKey translateKeyScheme(SecureDESKey key, KeyScheme keyScheme)
      throws SMException;



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
    SecureDESKey importKey(short keyLength, String keyType, byte[] encryptedKey,
                           SecureDESKey kek, boolean checkParity) throws SMException;

    /**
     * Imports a key from encryption under a KEK (Key-Encrypting Key)
     * to protection under the security module.
     *
     * @param kek the key-encrypting key
     * @param key key to be imported and encrypted under KEK
     * @param keySpec the specification of the key to be imported. It allows
     *        passing or change key block attributes.
     * @param checkParity if {@code true}, the key is not imported unless it has
     *        adjusted parity
     * @return imported key secured by the security module
     * @throws SMException e.g: if the parity of the imported key is not adjusted
     *         and {@code checkParity} is {@code true}
     */
    SecureKey importKey(SecureKey kek, SecureKey key, SecureKeySpec keySpec, boolean checkParity)
            throws SMException;


    /**
     * Exports secure key to encryption under a KEK (Key-Encrypting Key).
     * @param key the secure key to be exported
     * @param kek the key-encrypting key
     * @return the exported key (key encrypted under kek)
     * @throws SMException
     */
    byte[] exportKey(SecureDESKey key, SecureDESKey kek) throws SMException;

    /**
     * Exports secure key to encryption under a KEK (Key-Encrypting Key).
     *
     * @param kek the key-encrypting key
     * @param key the secure key to be exported
     * @param keySpec the specification of the key to be exported. It allows
     *        passing or change key block attributes.
     * @return the exported key (key encrypted under kek)
     * @throws SMException
     */
    SecureKey exportKey(SecureKey kek, SecureKey key, SecureKeySpec keySpec)
            throws SMException;

    /**
     * Encrypts a clear pin under LMK.
     *
     * <p>CAUTION: The use of clear pin presents a significant security risk
     * @param pin clear pin as entered by card holder
     * @param accountNumber account number, including BIN and the check digit
     * @return PIN under LMK
     * @throws SMException
     */
    EncryptedPIN encryptPIN(String pin, String accountNumber) throws SMException;

    /**
     * Encrypts a clear pin under LMK.
     *
     * <p>CAUTION: The use of clear pin presents a significant security risk
     * @param pin clear pin as entered by card holder
     * @param accountNumber if <code>extract</code> is false then account number, including BIN and the check digit
     *        or if parameter <code>extract</code> is true then 12 right-most digits of the account number, excluding the check digit
     * @param extract true to extract 12 right-most digits off the account number
     * @return PIN under LMK
     * @throws SMException
     */
    EncryptedPIN encryptPIN(String pin, String accountNumber, boolean extract) throws SMException;

    /**
     * Decrypts an Encrypted PIN (under LMK).
     * <p>CAUTION: The use of clear pin presents a significant security risk
     * @param pinUnderLmk
     * @return clear pin as entered by card holder
     * @throws SMException
     */
    String decryptPIN(EncryptedPIN pinUnderLmk) throws SMException;

    /**
     * Imports a PIN from encryption under KD (Data Key)
     * to encryption under LMK.
     *
     * @param pinUnderKd1 the encrypted PIN
     * @param kd1 Data Key under which the pin is encrypted
     * @return pin encrypted under LMK
     * @throws SMException
     */
    EncryptedPIN importPIN(EncryptedPIN pinUnderKd1, T kd1) throws SMException;



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
    EncryptedPIN translatePIN(EncryptedPIN pinUnderKd1, T kd1,
                              T kd2, byte destinationPINBlockFormat) throws SMException;



    /**
     * Imports a PIN from encryption under a transaction key to encryption
     * under LMK.
     *
     * <p>The transaction key is derived from the Key Serial Number and the Base Derivation Key using DUKPT (Derived Unique Key per Transaction). See ANSI X9.24 for more information.
     * @deprecated Use signature that specifies tdes flag.
     * @param pinUnderDuk pin encrypted under a transaction key
     * @param ksn Key Serial Number (also called Key Name, in ANSI X9.24) needed to derive the transaction key
     * @param bdk Base Derivation Key, used to derive the transaction key underwhich the pin is encrypted
     * @return pin encrypted under LMK
     * @throws SMException
     */
    EncryptedPIN importPIN(EncryptedPIN pinUnderDuk, KeySerialNumber ksn, T bdk)
            throws SMException;

    /**
     * Imports a PIN from encryption under a transaction key to encryption
     * under LMK.
     *
     * <p>The transaction key is derived from the Key Serial Number and the Base Derivation Key using DUKPT (Derived Unique Key per Transaction). See ANSI X9.24 for more information.
     * @param pinUnderDuk pin encrypted under a transaction key
     * @param ksn Key Serial Number (also called Key Name, in ANSI X9.24) needed to derive the transaction key
     * @param bdk Base Derivation Key, used to derive the transaction key underwhich the pin is encrypted
     * @param tdes Use Triple DES to calculate derived transaction key.
     * @return pin encrypted under LMK
     * @throws SMException
     */
    EncryptedPIN importPIN(EncryptedPIN pinUnderDuk, KeySerialNumber ksn,
                           T bdk, boolean tdes) throws SMException;



    /**
     * Translates a PIN from encryption under a transaction key to
     * encryption under a KD (Data Key).
     *
     * <p>The transaction key is derived from the Key Serial Number and the Base Derivation Key using DUKPT (Derived Unique Key per Transaction). See ANSI X9.24 for more information.
     * @deprecated Use signature that specifies tdes flag.
     * @param pinUnderDuk pin encrypted under a DUKPT transaction key
     * @param ksn Key Serial Number (also called Key Name, in ANSI X9.24) needed to derive the transaction key
     * @param bdk Base Derivation Key, used to derive the transaction key underwhich the pin is encrypted
     * @param kd2 the destination Data Key (also called session key) under which the pin will be encrypted
     * @param destinationPINBlockFormat the PIN Block Format of the translated encrypted PIN
     * @return pin encrypted under kd2
     * @throws SMException
     */
    EncryptedPIN translatePIN(EncryptedPIN pinUnderDuk, KeySerialNumber ksn,
                              T bdk, T kd2, byte destinationPINBlockFormat) throws SMException;


    /**
     * Translates a PIN from encryption under a transaction key to
     * encryption under a KD (Data Key).
     *
     * <p>The transaction key is derived from the Key Serial Number and the Base Derivation Key using DUKPT (Derived Unique Key per Transaction). See ANSI X9.24 for more information.
     * @param pinUnderDuk pin encrypted under a DUKPT transaction key
     * @param ksn Key Serial Number (also called Key Name, in ANSI X9.24) needed to derive the transaction key
     * @param bdk Base Derivation Key, used to derive the transaction key underwhich the pin is encrypted
     * @param kd2 the destination Data Key (also called session key) under which the pin will be encrypted
     * @param destinationPINBlockFormat the PIN Block Format of the translated encrypted PIN
     * @param tdes Use Triple DES to calculate derived transaction key.
     * @return pin encrypted under kd2
     * @throws SMException
     */
    EncryptedPIN translatePIN(EncryptedPIN pinUnderDuk, KeySerialNumber ksn,
                              T bdk, T kd2, byte destinationPINBlockFormat, boolean tdes) throws SMException;



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
    EncryptedPIN exportPIN(EncryptedPIN pinUnderLmk, T kd2, byte destinationPINBlockFormat) throws SMException;



    /**
     * Generate random pin under LMK
     *
     * @param accountNumber The 12 right-most digits of the account number excluding the check digit
     * @param pinLen length of the pin, usually in range 4-12.
     *               Value 0 means that default length is assumed by HSM (usually 4)
     * @return generated PIN under LMK
     * @throws SMException
     */
    EncryptedPIN generatePIN(String accountNumber, int pinLen)
             throws SMException;



    /**
     * Generate random pin under LMK with exclude list
     *
     * @param accountNumber The 12 right-most digits of the account number excluding the check digit
     * @param pinLen length of the pin, usually in range 4-12.
     *               Value 0 means that default length is assumed by HSM (usually 4)
     * @param excludes list of pins which won't be generated.
     *               Each pin has to be <code>pinLen</code> length
     * @return generated PIN under LMK
     * @throws SMException
     */
    EncryptedPIN generatePIN(String accountNumber, int pinLen, List<String> excludes)
            throws SMException;

    /**
     * Print PIN or PIN and solicitation data to the HSM configured printer.
     * <p>If {@code kd1} includes an encrypted PIN block then is first imported,
     * Also template is updated if needed in HSM storage. Then the PIN and
     * solicitation data are included into the template and result are
     * printed to the HSM attached printer.
     *
     * @param accountNo The 12 right-most digits of the account number excluding the check digit.
     * @param pinUnderKd1 pin block under Key Data 1
     * @param kd1 Data Key 1 ZPK, TPK may be null if {@code pinUnderKd1} contains PIN under LMK
     * @param template template text (PCL, PostScript or other) for PIN Mailer printer.
     *                 Its format depends on used HSM. This template should
     *                 includes placeholders tags (e.g. in format ${tag})
     *                 indicationg place where coresponding value or PIN should
     *                 be inserted. Tags values are passed in {@code fields}
     *                 map argument except PIN which is passed in argument {@code pinUnderKd1}.
     * @param fields map of tags values representing solicitation data to include
     *               in template. null if no solicitation data are passed
     * @throws SMException
     */
    void printPIN(String accountNo, EncryptedPIN pinUnderKd1, T kd1
      , String template, Map<String, String> fields) throws SMException;


    /**
     * Calculate PVV (VISA PIN Verification Value of PIN under LMK)
     * with exclude list
     *
     * <p>NOTE: {@code pvkA} and {@code pvkB} should be single length keys
     * but at least one of them may be double length key
     *
     * @param pinUnderLmk PIN under LMK
     * @param pvkA first key PVK in PVK pair
     * @param pvkB second key PVK in PVK pair
     * @param pvkIdx index of the PVK, in range 0-6, if not present 0 is assumed
     * @return PVV (VISA PIN Verification Value)
     * @throws SMException if PIN is on exclude list {@link WeakPINException} is thrown
     */
    String calculatePVV(EncryptedPIN pinUnderLmk, T pvkA, T pvkB, int pvkIdx)
            throws SMException;


    /**
     * Calculate PVV (VISA PIN Verification Value of PIN under LMK)
     *
     * <p>NOTE: {@code pvkA} and {@code pvkB} should be single length keys
     * but at least one of them may be double length key
     *
     * @param pinUnderLmk PIN under LMK
     * @param pvkA first key PVK in PVK pair
     * @param pvkB second key PVK in PVK pair
     * @param pvkIdx index of the PVK, in range 0-6, if not present 0 is assumed
     * @param excludes list of pins which won't be generated.
     *               Each pin has to be <code>pinLen</code> length
     * @return PVV (VISA PIN Verification Value)
     * @throws SMException
     */
    String calculatePVV(EncryptedPIN pinUnderLmk, T pvkA, T pvkB, int pvkIdx,
                        List<String> excludes) throws SMException;


    /**
     * Calculate PVV (VISA PIN Verification Value of customer selected PIN)
     *
     * <p>NOTE: {@code pvkA} and {@code pvkB} should be single length keys
     * but at least one of them may be double length key
     *
     * @param pinUnderKd1 the encrypted PIN
     * @param kd1 Data Key under which the pin is encrypted
     * @param pvkA first key PVK in PVK pair
     * @param pvkB second key PVK in PVK pair
     * @param pvkIdx index of the PVK, in range 0-6, if not present 0 is assumed
     * @return PVV (VISA PIN Verification Value)
     * @throws SMException
     */
    String calculatePVV(EncryptedPIN pinUnderKd1, T kd1, T pvkA, T pvkB, int pvkIdx)
            throws SMException;


    /**
     * Calculate PVV (VISA PIN Verification Value of customer selected PIN)
     *
     * <p>NOTE: {@code pvkA} and {@code pvkB} should be single length keys
     * but at least one of them may be double length key
     *
     * @param pinUnderKd1 the encrypted PIN
     * @param kd1 Data Key under which the pin is encrypted
     * @param pvkA first key PVK in PVK pair
     * @param pvkB second key PVK in PVK pair
     * @param pvkIdx index of the PVK, in range 0-6, if not present 0 is assumed
     * @param excludes list of pins which won't be generated.
     *               Each pin has to be <code>pinLen</code> length
     * @return PVV (VISA PIN Verification Value)
     * @throws WeakPINException if passed PIN is on {@code excludes} list
     * @throws SMException
     */
    String calculatePVV(EncryptedPIN pinUnderKd1, T kd1, T pvkA, T pvkB, int pvkIdx,
                        List<String> excludes) throws SMException;


    /**
     * Verify PVV (VISA PIN Verification Value of an LMK encrypted PIN)
     *
     * <p>NOTE: {@code pvkA} and {@code pvkB} should be single
     * length keys but at least one of them may be double length key
     *
     * @param pinUnderKd1 pin block under {@code kd1}
     * @param kd1 Data Key (also called session key) under which the pin is encrypted (ZPK or TPK)
     * @param pvkA first PVK in PVK pair
     * @param pvkB second PVK in PVK pair
     * @param pvki index of the PVK, in range 0-6, if not present 0 is assumed
     * @param pvv (VISA PIN Verification Value)
     * @return true if pin is valid false if not
     * @throws SMException
     */
    boolean verifyPVV(EncryptedPIN pinUnderKd1, T kd1, T pvkA,
                      T pvkB, int pvki, String pvv) throws SMException;


    /**
     * Calculate an PIN Offset using the IBM 3624 method
     *
     * <p>Using that method is not recomendated. PVV method is prefrred,
     * but it may be need in some legacy systms
     * @param pinUnderLmk PIN under LMK
     * @param pvk        accepts single, double, triple size key length.
     *                   Single key length is recomendated
     * @param decTab     decimalisation table. Accepts plain text and encrypted
     *                   decimalisation table depending to HSM configuration
     * @param pinValData pin validation data. User-defined data consisting of hexadecimal
     *                   characters and the character N, which indicates to the HSM where
     *                   to insert the last 5 digits of the account number. Usualy it consists
     *                   the first digits of the card number
     * @param minPinLen  pin minimal length
     * @return IBM PIN Offset
     * @throws SMException
     */
    String calculateIBMPINOffset(EncryptedPIN pinUnderLmk, T pvk,
                                 String decTab, String pinValData,
                                 int minPinLen) throws SMException;



    /**
     * Calculate an PIN Offset using the IBM 3624 method
     *
     * <p>Using that method is not recomendated. PVV method is prefrred,
     * but it may be need in some legacy systms
     * @param pinUnderLmk PIN under LMK
     * @param pvk        accepts single, double, triple size key length.
     *                   Single key length is recomendated
     * @param decTab     decimalisation table. Accepts plain text and encrypted
     *                   decimalisation table depending to HSM configuration
     * @param pinValData pin validation data. User-defined data consisting of hexadecimal
     *                   characters and the character N, which indicates to the HSM where
     *                   to insert the last 5 digits of the account number. Usualy it consists
     *                   the first digits of the card number
     * @param minPinLen  pin minimal length
     * @param excludes list of pins which won't be generated.
     *               Each pin has to be <code>pinLen</code> length
     * @return IBM PIN Offset
     * @throws WeakPINException if passed PIN is on {@code excludes} list
     * @throws SMException
     */
    String calculateIBMPINOffset(EncryptedPIN pinUnderLmk, T pvk,
                                 String decTab, String pinValData, int minPinLen,
                                 List<String> excludes) throws SMException;



    /**
     * Calculate an PIN Offset using the IBM 3624 method of customer selected PIN
     *
     * <p>Using that method is not recomendated. PVV method is prefrred,
     * but it may be need in some legacy systms
     * @param pinUnderKd1 the encrypted PIN
     * @param kd1 Data Key under which the pin is encrypted
     * @param pvk        accepts single, double, triple size key length.
     *                   Single key length is recomendated
     * @param decTab     decimalisation table. Accepts plain text and encrypted
     *                   decimalisation table depending to HSM configuration
     * @param pinValData pin validation data. User-defined data consisting of hexadecimal
     *                   characters and the character N, which indicates to the HSM where
     *                   to insert the last 5 digits of the account number. Usualy it consists
     *                   the first digits of the card number
     * @param minPinLen  pin minimal length
     * @return IBM PIN Offset
     * @throws SMException
     */
    String calculateIBMPINOffset(EncryptedPIN pinUnderKd1, T kd1,
                                 T pvk, String decTab, String pinValData,
                                 int minPinLen) throws SMException;



    /**
     * Calculate an PIN Offset using the IBM 3624 method of customer selected PIN
     *
     * <p>Using that method is not recomendated. PVV method is prefrred,
     * but it may be need in some legacy systms
     * @param pinUnderKd1 the encrypted PIN
     * @param kd1 Data Key under which the pin is encrypted
     * @param pvk        accepts single, double, triple size key length.
     *                   Single key length is recomendated
     * @param decTab     decimalisation table. Accepts plain text and encrypted
     *                   decimalisation table depending to HSM configuration
     * @param pinValData pin validation data. User-defined data consisting of hexadecimal
     *                   characters and the character N, which indicates to the HSM where
     *                   to insert the last 5 digits of the account number. Usualy it consists
     *                   the first digits of the card number
     * @param minPinLen  pin minimal length
     * @param excludes list of pins which won't be generated.
     *               Each pin has to be <code>pinLen</code> length
     * @return IBM PIN Offset
     * @throws WeakPINException if passed PIN is on {@code excludes} list
     * @throws SMException
     */
    String calculateIBMPINOffset(EncryptedPIN pinUnderKd1, T kd1,
                                 T pvk, String decTab, String pinValData,
                                 int minPinLen, List<String> excludes) throws SMException;



    /**
     * Verify an PIN Offset using the IBM 3624 method
     *
     * @param pinUnderKd1 pin block under {@code kd1}
     * @param kd1        Data Key (also called session key) under which the pin is encrypted (ZPK or TPK)
     * @param pvk        accepts single, double, triple size key length.
     *                   Single key length is recomendated
     * @param offset     IBM PIN Offset
     * @param decTab     decimalisation table. Accepts plain text and encrypted
     *                   decimalisation table depending to HSM configuration
     * @param pinValData pin validation data. User-defined data consisting of hexadecimal
     *                   characters and the character N, which indicates to the HSM where
     *                   to insert the last 5 digits of the account number. Usualy it consists
     *                   the first digits of the card number
     * @param minPinLen  min pin length
     * @return true if pin offset is valid false if not
     * @throws SMException
     */
    boolean verifyIBMPINOffset(EncryptedPIN pinUnderKd1, T kd1, T pvk,
                               String offset, String decTab, String pinValData,
                               int minPinLen) throws SMException;



    /**
     * Derive a PIN Using the IBM 3624 method
     *
     * <p>That method derive pin from pin offset (not exacly that same but working).
     * Therefore that metod is not recomendated. It is similar to obtain pin
     * from encrypted pinblock, but require (encrypted) decimalisation table
     * handling is more complicated and returned pin may differ from pin what user has selected 
     * It may be uable e.g. in migration from pin offset method to PVV method
     * @param accountNo  the 12 right-most digits of the account number excluding the check digit
     * @param pvk        accepts single, double, triple size key length.
     *                   Single key length is recomendated
     * @param decTab     decimalisation table. Accepts plain text and encrypted
     *                   decimalisation table depending to HSM configuration
     * @param pinValData pin validation data. User-defined data consisting of hexadecimal
     *                   characters and the character N, which indicates to the HSM where
     *                   to insert the last 5 digits of the account number. Usualy it consists
     *                   the first digits of the card number
     * @param minPinLen  min pin length
     * @param offset     IBM PIN Offset
     * @return           PIN under LMK
     * @throws SMException
     */
    EncryptedPIN deriveIBMPIN(String accountNo, T pvk
      , String decTab, String pinValData, int minPinLen
      , String offset) throws SMException;



    /**
     * Calaculate a Card Verification Code/Value.
     *
     * <p>NOTE: {@code cvkA} and {@code cvkB} should be single
     * length keys but at least one of them may be double length key
     *
     * @param accountNo The account number including BIN and the check digit
     * @param cvkA        the first CVK in CVK pair
     * @param cvkB        the second CVK in CVK pair
     * @param expDate     the card expiration date
     * @param serviceCode the card service code
     *        Service code should be:
     *        <ul>
     *          <li>the value which will be placed onto card's magnetic stripe for encoding CVV1/CVC1</li>
     *          <li>"000" for printing CVV2/CVC2 on card's signature stripe</li>
     *          <li>"999" for inclusion iCVV/Chip CVC on EMV chip card</li>
     *        </ul>
     * @return Card Verification Code/Value
     * @throws SMException
     * @deprecated Issuers do not always follow the recommended 'yyMM' format.
     * Using the {@code java.util.Date} prevents from format manipulating to
     * solve problem. Use {@link #calculateCVD} with string version of {@code expDate}
     */
    @Deprecated
    String calculateCVV(String accountNo, T cvkA, T cvkB,
                        Date expDate, String serviceCode) throws SMException;



    /**
     * Calaculate a Card Verification Digit (Code/Value).
     *
     * <p>NOTE: {@code cvkA} and {@code cvkB} should be single
     * length keys but at least one of them may be double length key
     *
     * @param accountNo The account number including BIN and the check digit
     * @param cvkA        the first CVK in CVK pair
     * @param cvkB        the second CVK in CVK pair
     * @param expDate     the card expiration date
     * @param serviceCode the card service code
     *        Service code should be:
     *        <ul>
     *          <li>the value which will be placed onto card's magnetic stripe for encoding CVV1/CVC1</li>
     *          <li>"000" for printing CVV2/CVC2 on card's signature stripe</li>
     *          <li>"999" for inclusion iCVV/Chip CVC on EMV chip card</li>
     *        </ul>
     * @return Card Verification Digit (Code/Value)
     * @throws SMException
     */
    String calculateCVD(String accountNo, T cvkA, T cvkB,
                        String expDate, String serviceCode) throws SMException;



    /**
     * Calaculate a 3-D Secure CAVV/AAV.
     *
     * <ul>
     * <li>Visa uses CAVV (Cardholder Authentication Verification Value)</li>
     * <li>MasterCard uses AAV (Accountholder Authentication Value)</li>
     * </ul>
     * <p>NOTE: Algorithm used to calculation CAVV/AAV is same as for CVV/CVC
     *          calculation. Only has been changed meaning of parameters
     *          {@code expDate} and {@code serviceCode}.
     *
     * @param accountNo   the account number including BIN and the check digit.
     * @param cvk         the key used to CVV/CVC generation
     * @param upn         the unpredictable number. Calculated value based
     *                    on Transaction Identifier (xid) from PAReq.
     *                    A 4 decimal digits value must be supplied.
     * @param authrc      the Authentication Results Code. A value based on
     *                    the Transaction Status (status) that will be used in
     *                    PARes. A 1 decimal digit value must be supplied.
     * @param sfarc       the Second Factor Authentication Results Code.
     *                    A value based on the result of second factor authentication.
     *                    A 2 decimal digits value must be suppiled.
     * @return Cardholder Authentication Verification Value/Accountholder
     *         Authentication Value
     * @throws SMException
     */
    String calculateCAVV(String accountNo, T cvk, String upn,
                         String authrc, String sfarc) throws SMException;

    /**
     * Verify a Card Verification Code/Value.
     *
     * <p>NOTE: {@code cvkA} and {@code cvkB} should be single
     * length keys but at least one of them may be double length key
     *
     * @param accountNo The account number including BIN and the check digit
     * @param cvkA the first CVK in CVK pair
     * @param cvkB the second CVK in CVK pair
     * @param cvv Card Verification Code/Value
     * @param expDate the card expiration date
     * @param serviceCode the card service code
     *        Service code should be:
     *        <ul>
     *         <li>taken from card's magnetic stripe for verifing CVV1/CVC1</li>
     *         <li>"000" for verifing CVV2/CVC2 printed on card's signature stripe</li>
     *         <li>"999" for verifing iCVV/Chip CVC included on EMV chip card</li>
     *        </ul>
     * @return true if CVV/CVC is valid or false if not
     * @throws SMException
     * @deprecated Issuers do not always follow the recommended 'yyMM' format.
     * Using the {@code java.util.Date} prevents from format manipulating to
     * solve problem. Use {@link #verifyCVD} with string version of {@code expDate}
     */
    @Deprecated
    boolean verifyCVV(String accountNo, T cvkA, T cvkB,
                      String cvv, Date expDate, String serviceCode) throws SMException;


    /**
     * Verify a Card Verification Digit (Code/Value).
     *
     * <p>NOTE: {@code cvkA} and {@code cvkB} should be single
     * length keys but at least one of them may be double length key
     *
     * @param accountNo The account number including BIN and the check digit
     * @param cvkA the first CVK in CVK pair
     * @param cvkB the second CVK in CVK pair
     * @param cvv Card Verification Code/Value
     * @param expDate the card expiration date
     * @param serviceCode the card service code
     *        Service code should be:
     *        <ul>
     *         <li>taken from card's magnetic stripe for verifing CVV1/CVC1</li>
     *         <li>"000" for verifing CVV2/CVC2 printed on card's signature stripe</li>
     *         <li>"999" for verifing iCVV/Chip CVC included on EMV chip card</li>
     *        </ul>
     * @return {@code true} if CVV/CVC is valid or {@code false} otherwise
     * @throws SMException
     */
    boolean verifyCVD(String accountNo, T cvkA, T cvkB,
                      String cvv, String expDate, String serviceCode) throws SMException;


    /**
     * Verify a 3-D Secure CAVV/AAV.
     *
     * <ul>
     * <li>Visa uses CAVV (Cardholder Authentication Verification Value)</li>
     * <li>MasterCard uses AAV (Accountholder Authentication Value)</li>
     * </ul>
     * <p>NOTE: Algorithm used to verification CAVV/AAV is same as for CVV/CVC
     *          verification. Only has been changed meaning of parameters
     *          {@code expDate} and {@code serviceCode}.
     *
     * @param accountNo   the account number including BIN and the check digit.
     * @param cvk         the key used to CVV/CVC generation
     * @param cavv        the Cardholder Authentication Verification Value
     *                    or Accountholder Authentication Value.
     * @param upn         the unpredictable number. Calculated value based
     *                    on Transaction Identifier (xid) from PAReq.
     *                    A 4 decimal digits value must be supplied.
     * @param authrc      the Authentication Results Code. A value based on
     *                    the Transaction Status (status) that will be used in
     *                    PARes. A 1 decimal digit value must be supplied.
     * @param sfarc       the Second Factor Authentication Results Code.
     *                    A value based on the result of second factor authentication.
     *                    A 2 decimal digits value must be suppiled.
     * @return true if CAVV/AAV is valid or false if not
     * @throws SMException
     */
    boolean verifyCAVV(String accountNo, T cvk, String cavv,
                       String upn, String authrc, String sfarc) throws SMException;


    /**
     * Verify a Dynamic Card Verification Value (dCVV).
     *
     * <p>The EMV "Track 2 Equivalent Data", provided in the authorisation
     * message and originating from the contactless smart card, is the source
     * for the following data elements used in this function:
     * <li> {@code accountNo}
     * <li> {@code expDate}
     * <li> {@code serviceCode}
     * <li> {@code atc}
     * <li> {@code dCVV}
     *
     * @param accountNo The account number including BIN and the check digit
     * @param imkac the issuer master key for generating and verifying Application Cryptograms
     * @param dcvv dynamic Card Verification Value
     * @param expDate the card expiration date
     * @param serviceCode the card service code
     * @param atc application transactin counter. This is used for ICC Master
     *        Key derivation. A 2 byte value must be supplied.
     * @param mkdm ICC Master Key Derivation Method. If {@code null} specified
     *        is assumed.
     * @return {@code true} if {@code dcvv} is valid, or {@code false} if not
     * @throws SMException
     * @deprecated Issuers do not always follow the recommended 'yyMM' format.
     * Using the {@code java.util.Date} prevents from format manipulating to
     * solve problem. Use {@link #verifydCVV} with string version of {@code expDate}
     */
    @Deprecated
    boolean verifydCVV(String accountNo, T imkac, String dcvv,
                       Date expDate, String serviceCode, byte[] atc, MKDMethod mkdm)
                     throws SMException;

    /**
     * Verify a Dynamic Card Verification Value (dCVV).
     * <p>
     * The EMV "Track 2 Equivalent Data", provided in the authorisation
     * message and originating from the contactless smart card, is the source
     * for the following data elements used in this function:
     * <ul>
     *   <li> {@code accountNo}
     *   <li> {@code expDate}
     *   <li> {@code serviceCode}
     *   <li> {@code atc}
     *   <li> {@code dCVV}
     * </ul>
     *
     * @param accountNo The account number including BIN and the check digit
     * @param imkac the issuer master key for generating and verifying Application Cryptograms
     * @param dcvv dynamic Card Verification Value
     * @param expDate the card expiration date
     * @param serviceCode the card service code
     * @param atc application transactin counter. This is used for ICC Master
     *        Key derivation. A 2 byte value must be supplied.
     * @param mkdm ICC Master Key Derivation Method. If {@code null} specified
     *        is assumed.
     * @return {@code true} if {@code dcvv} is valid, or {@code false} if not
     * @throws SMException
     */
    boolean verifydCVV(String accountNo, T imkac, String dcvv,
                       String expDate, String serviceCode, byte[] atc, MKDMethod mkdm)
                     throws SMException;


    /**
     * Verify a Dynamic Card Verification Code 3 (CVC3)
     * <p>
     * The EMV "Track 2 Equivalent Data", provided in the authorisation
     * message and originating from the contactless smart card, is the source
     * for the following data elements used in this function:
     * <ul>
     *   <li> {@code accountNo}
     *   <li> {@code expDate}
     *   <li> {@code serviceCode}
     *   <li> {@code atc}
     *   <li> {@code unpredictable number}
     *   <li> {@code cvc3}
     * </ul>
     *
     * @param imkcvc3 the issuer master key for generating and verifying CVC3
     * @param accountNo The account number including BIN and the check digit
     * @param acctSeqNo account sequence number, 2 decimal digits
     * @param atc application transactin counter. This is used for CVC3
     *        calculation. A 2 byte value must be supplied.
     * @param upn  unpredictable number. This is used for CVC3 calculation
     *        A 4 byte value must be supplied.
     * @param data Static Track Data or when this data length is less or equal 2 IVCVC3
     *        <ul>
     *        <li>Static Track 1 or 2 Data. From the the issuer is dependent on
     *            how to obtain it from the EMV "Track 2 Equivalent Data",
     *            provided in the authorisation message and originating from
     *            the contactless smart card. Usually variable part of
     *            Discreditionary Data are replased by some static value.
     *        <li>precomputed Initial Vector for <tt>CVC3</tt> calculation
     *            <tt>(IVCVC3)</tt> which is a <tt>MAC</tt> calculated over
     *            the static part of Track1 or Track2 data using the key derived
     *            from <tt>MK-CVC3</tt>.
     *        </ul>
     * @param mkdm ICC Master Key Derivation Method. If {@code null} specified
     *        is assumed.
     * @param cvc3 dynamic Card Verification Code 3. Should contain 5 decimal
     *        digits. Max value is {@code "65535"} (decimal representation
     *        of 2 byte value). Is possible to pass shorter cvc3 value e.g.
     *        {@code "789"} matches with calcuated CVC3 {@code "04789"}
     * @return true if cvc3 is valid false if not
     * @throws SMException
     */
    boolean verifyCVC3(T imkcvc3, String accountNo, String acctSeqNo,
                       byte[] atc, byte[] upn, byte[] data, MKDMethod mkdm, String cvc3)
                     throws SMException;



    /**
     * Verify Application Cryptogram (ARQC or TC/AAC)
     * <ul>
     * <li>Authorization Request Cryptogram (ARQC) - Online authorization
     * <li>Transaction certificate (TC) - Offline approval
     * <li>Application Authentication Cryptogram (AAC) - Offline decline
     * </ul>
     *
     * @param mkdm ICC Master Key Derivation Method. For {@code skdm} equals
     *        {@link SKDMethod#VSDC} and {@link SKDMethod#MCHIP} this parameter
     *        is ignored and {@link MKDMethod#OPTION_A} is always used.
     * @param skdm Session Key Derivation Method
     * @param imkac the issuer master key for generating and verifying Application Cryptograms
     * @param accountNo account number including BIN and check digit
     * @param acctSeqNo account sequence number, 2 decimal digits
     * @param arqc ARQC/TC/AAC. A 8 byte value must be supplied.
     * @param atc application transactin counter. This is used for Session
     *        Key Generation. A 2 byte value must be supplied.
     *        For {@code skdm} equals {@link SKDMethod#VSDC} is not used.
     * @param upn unpredictable number. This is used for Session Key Generation
     *        A 4 byte value must be supplied. For {@code skdm} equals
     *        {@link SKDMethod#VSDC} is not used.
     * @param txnData transaction data. Transaction data elements and them
     *        order is dependend to proper cryptogram version. If the data
     *        supplied is a multiple of 8 bytes, no extra padding is added.
     *        If it is not a multiple of 8 bytes, additional zero padding is added.
     *        <b>If alternative padding methods are required, it have to be
     *        applied before</b>.
     * @return true if ARQC/TC/AAC is passed or false if not
     * @throws SMException
     */
    boolean verifyARQC(MKDMethod mkdm, SKDMethod skdm, T imkac
      , String accountNo, String acctSeqNo, byte[] arqc, byte[] atc
      , byte[] upn, byte[] txnData) throws SMException;



    /**
     * Genarate Authorisation Response Cryptogram (ARPC)
     *
     * @param mkdm ICC Master Key Derivation Method. For {@code skdm} equals
     *        {@link SKDMethod#VSDC} and {@link SKDMethod#MCHIP} this parameter
     *        is ignored and {@link MKDMethod#OPTION_A} is always used.
     * @param skdm Session Key Derivation Method
     * @param imkac the issuer master key for generating and verifying Application Cryptograms
     * @param accoutNo account number including BIN and check digit
     * @param acctSeqNo account sequence number, 2 decimal digits
     * @param arqc ARQC/TC/AAC. A 8 byte value must be supplied.
     * @param atc application transactin counter. This is used for Session
     *        Key Generation. A 2 byte value must be supplied.
     *        For {@code skdm} equals {@link SKDMethod#VSDC} is not used.
     * @param upn unpredictable number. This is used for Session Key Generation
     *        A 4 byte value must be supplied. For {@code skdm} equals
     *        {@link SKDMethod#VSDC} is not used.
     * @param arpcMethod ARPC calculating method. For {@code skdm} equals
     *        {@link SKDMethod#VSDC}, {@link SKDMethod#MCHIP},
     *        {@link SKDMethod#AEPIS_V40} only {@link ARPCMethod#METHOD_1} is valid
     * @param arc the Authorisation Response Code. A 2 byte value must be supplied.
     *        For {@code arpcMethod} equals {@link ARPCMethod#METHOD_2} it is
     *        csu - Card Status Update. Then a 4 byte value must be supplied.
     * @param propAuthData Proprietary Authentication Data. Up to 8 bytes.
     *        Contains optional issuer data for transmission to the card in
     *        the Issuer Authentication Data of an online transaction.
     *        It may by used only for {@code arpcMethod} equals
     *        {@link ARPCMethod#METHOD_2} in other case is ignored.
     * @return calculated 8 bytes ARPC or if {@code arpcMethod} equals
     *        {@link ARPCMethod#METHOD_2} 4 bytes ARPC
     * @throws SMException
     */
    byte[] generateARPC(MKDMethod mkdm, SKDMethod skdm, T imkac
      , String accoutNo, String acctSeqNo, byte[] arqc, byte[] atc, byte[] upn
      , ARPCMethod arpcMethod, byte[] arc, byte[] propAuthData)
            throws SMException;



    /**
     * Verify Application Cryptogram (ARQC or TC/AAC) and Genarate
     * Authorisation Response Cryptogram (ARPC)
     * <ul>
     * <li>Authorization Request Cryptogram (ARQC) - Online authorization
     * <li>Transaction certificate (TC) - Offline approval
     * <li>Application Authentication Cryptogram (AAC) - Offline decline
     * </ul>
     * @param mkdm ICC Master Key Derivation Method. For {@code skdm} equals
     *        {@link SKDMethod#VSDC} and {@link SKDMethod#MCHIP} this parameter
     *        is ignored and {@link MKDMethod#OPTION_A} is always used.
     * @param skdm Session Key Derivation Method
     * @param imkac the issuer master key for generating and verifying Application Cryptograms
     * @param accountNo account number including BIN and check digit
     * @param acctSeqNo account sequence number, 2 decimal digits
     * @param arqc ARQC/TC/AAC. A 8 byte value must be supplied.
     * @param atc application transactin counter. This is used for Session
     *        Key Generation. A 2 byte value must be supplied.
     *        For {@code skdm} equals {@link SKDMethod#VSDC} is not used.
     * @param upn unpredictable number. This is used for Session Key Generation
     *        A 4 byte value must be supplied. For {@code skdm} equals
     *        {@link SKDMethod#VSDC} is not used.
     * @param txnData transaction data. Transaction data elements and them
     *        order is dependend to proper cryptogram version. If the data
     *        supplied is a multiple of 8 bytes, no extra padding is added.
     *        If it is not a multiple of 8 bytes, additional zero padding is added.
     *        <b>If alternative padding methods are required, it have to be
     *        applied before</b>.
     * @param arpcMethod ARPC calculating method. For {@code skdm} equals
     *        {@link SKDMethod#VSDC}, {@link SKDMethod#MCHIP},
     *        {@link SKDMethod#AEPIS_V40} only {@link ARPCMethod#METHOD_1} is valid
     * @param arc the Authorisation Response Code. A 2 byte value must be supplied.
     *        For {@code arpcMethod} equals {@link ARPCMethod#METHOD_2} it is
     *        csu - Card Status Update. Then a 4 byte value must be supplied.
     * @param propAuthData Proprietary Authentication Data. Up to 8 bytes.
     *        Contains optional issuer data for transmission to the card in
     *        the Issuer Authentication Data of an online transaction.
     *        It may by used only for {@code arpcMethod} equals
     *        {@link ARPCMethod#METHOD_2} in other case is ignored.
     * @return if ARQC/TC/AAC verification passed then calculated 8 bytes ARPC
     *         or for {@code arpcMethod} equals {@link ARPCMethod#METHOD_2}
     *         4 bytes ARPC, null in other case
     * @throws SMException
     */
    byte[] verifyARQCGenerateARPC(MKDMethod mkdm, SKDMethod skdm, T imkac
      , String accountNo, String acctSeqNo, byte[] arqc, byte[] atc, byte[] upn
      , byte[] txnData, ARPCMethod arpcMethod, byte[] arc, byte[] propAuthData)
            throws SMException;


    /**
     * Generate Secure Message MAC over suppiled message data
     * <p>
     * This method is used by issuer to generate MAC over message data send
     * from the issuer back to the card
     *
     * @param mkdm ICC Master Key Derivation Method. For {@code skdm} equals
     *        {@link SKDMethod#VSDC} and {@link SKDMethod#MCHIP} this parameter
     *        is ignored and {@link MKDMethod#OPTION_A} is always used.
     * @param skdm Session Key Derivation Method
     * @param imksmi the issuer master key for Secure Messaging Integrity
     * @param accountNo account number including BIN and check digit
     * @param acctSeqNo account sequence number, 2 decimal digits
     * @param atc application transactin counter. This is used for Session
     *        Key Generation. A 2 byte value must be supplied.
     *        For {@code skdm} equals {@link SKDMethod#VSDC} is not used.
     *        Second usage is as part of data which will be macked
     * @param arqc ARQC/TC/AAC. A 8 byte value must be supplied.
     *        For {@code skdm} equals {@link SKDMethod#MCHIP} RAND should
     *        be suppiled. RAND is ARQC incremeted by 1 (with overflow) after
     *        each script command for that same ATC value
     * @param data for which MAC will be generated. Should contain
     *        APDU command e.g. PIN Unblock, Application block/unblock
     *        with some additional application dependent data
     * @return generated 8 bytes MAC
     * @throws SMException
     */
    byte[] generateSM_MAC(MKDMethod mkdm, SKDMethod skdm
      , T imksmi, String accountNo, String acctSeqNo
      , byte[] atc, byte[] arqc, byte[] data) throws SMException;



    /**
     * Translate PIN and generate MAC over suppiled message data
     * <p>
     * This method is used by issuer to:
     * <ul>
     * <li>translate standard ATM PIN block format encrypted under zone
     * or terminal key {@code kd1} to an application specific PIN block
     * format, encrypted under a confidentiality session key, derived from
     * {@code imksmc}
     * <li>generate MAC over suppiled message {@code data} and translated
     * PIN block
     * </ul>
     * @param mkdm ICC Master Key Derivation Method. For {@code skdm} equals
     *        {@link SKDMethod#VSDC} and {@link SKDMethod#MCHIP} this parameter
     *        is ignored and {@link MKDMethod#OPTION_A} is always used.
     * @param skdm Session Key Derivation Method
     * @param padm padding method. If null {@code padm} is derived as follow:
     *    <blockquote>
     *    <table>
     *      <thead>
     *        <tr><th>{@code skdm} value</th><th>derived {@code padm} value</th></tr>
     *      </thead>
     *      <tbody>
     *        <tr><td>{@link SKDMethod#VSDC}</td><td>{@link PaddingMethod#VSDC}</td></tr>
     *        <tr><td>{@link SKDMethod#MCHIP}</td><td>{@link PaddingMethod#MCHIP}</td></tr>
     *        <tr><td>{@link SKDMethod#EMV_CSKD}</td><td>{@link PaddingMethod#CCD}</td></tr>
     *      </tbody>
     *    </table>
     *    Other variations require to explicite pass {@code padm} value
     *    </blockquote>
     * @param imksmi the issuer master key for Secure Messaging Integrity
     * @param accountNo account number including BIN and check digit
     * @param acctSeqNo account sequence number, 2 decimal digits
     * @param atc application transactin counter. This is used for Session
     *        Key Generation. A 2 byte value must be supplied.
     *        For {@code skdm} equals {@link SKDMethod#VSDC} is not used.
     *        Second usage is as part of data which will be macked
     * @param arqc ARQC/TC/AAC. A 8 byte value must be supplied.
     *        For {@code skdm} equals {@link SKDMethod#MCHIP} RAND should
     *        be suppiled. RAND is ARQC incremeted by 1 (with overflow) after
     *        each script command for that same ATC value
     * @param data for which MAC will be generated. Should contain APDU
     *        command PIN Change with some additional application dependent data
     * @param currentPIN encrypted under {@code kd1} current PIN. Used when
     *        {@code destinationPINBlockFormat} equals {@link SMAdapter#FORMAT42}
     * @param newPIN encrypted under {@code kd1} new PIN.
     * @param kd1 Data Key (also called transport key) under which the source pin is encrypted
     * @param imksmc the issuer master key for Secure Messaging Confidentiality
     * @param imkac the issuer master key for generating and verifying
     *        Application Cryptograms. Used when {@code destinationPINBlockFormat} equals
     *        {@link SMAdapter#FORMAT41} or {@link SMAdapter#FORMAT42} in other cases is ignored
     * @param destinationPINBlockFormat the PIN Block Format of the translated encrypted PIN
     *        <dl>
     *          <dt><b>Allowed values:</b>
     *          <dd>{@link SMAdapter#FORMAT34} Standard EMV PIN Block
     *          <dd>{@link SMAdapter#FORMAT35} Europay/Mastercard
     *          <dd>{@link SMAdapter#FORMAT41} Visa/Amex format without using Current PIN
     *          <dd>{@link SMAdapter#FORMAT42} Visa/Amex format using Current PIN
     *        </dl>
     * @return Pair of values, encrypted PIN and 8 bytes MAC
     * @throws SMException
     */
    Pair<EncryptedPIN,byte[]> translatePINGenerateSM_MAC(MKDMethod mkdm
      , SKDMethod skdm, PaddingMethod padm, T imksmi
      , String accountNo, String acctSeqNo, byte[] atc, byte[] arqc
      , byte[] data, EncryptedPIN currentPIN, EncryptedPIN newPIN
      , T kd1, T imksmc, T imkac
      , byte destinationPINBlockFormat) throws SMException;



    /**
     * Encrypt Data Block.
     *
     * @param cipherMode block cipher mode.
     * @param kd DEK or ZEK key used to encrypt data.
     * @param data data to be encrypted. If the data is not a multiple of
     *        8 bytes, padding have to be applied before.
     * @param iv initial vector. Its length must be equal to the length
     *        of cipher block (8 bytes for DES, 3DES ciphers). After operation
     *        will contain new iv value. Not used for {@link CipherMode#ECB}.
     * @return encrypted data. In {@code iv} array refference new value of
     *        initial vector value will be placed.
     * @throws SMException
     */
    byte[] encryptData(CipherMode cipherMode, SecureDESKey kd
      , byte[] data, byte[] iv) throws SMException;



    /**
     * Decrypt Data Block.
     *
     * @param cipherMode block cipher mode.
     * @param kd DEK or ZEK key used to decrypt data.
     * @param data data to be decrypted.
     * @param iv initial vector. Its length must be equal to the length
     *        of cipher block (8 bytes for DES, 3DES ciphers). After operation
     *        will contain new iv value. Not used for {@link CipherMode#ECB}.
     * @return decrypted data. In {@code iv} array refference new value of
     *        initial vector value will be placed.
     * @throws SMException
     */
    byte[] decryptData(CipherMode cipherMode, SecureDESKey kd
      , byte[] data, byte[] iv) throws SMException;



    /**
     * Generates CBC-MAC (Cipher Block Chaining Message Authentication Code)
     * for some data.
     *
     * @param data the data to be MACed
     * @param kd the key used for MACing
     * @return the MAC
     * @throws SMException
     */
    byte[] generateCBC_MAC(byte[] data, T kd) throws SMException;

    /**
     * Generates EDE-MAC (Encrypt Decrypt Encrypt Message Message Authentication Code)
     * for some data.
     *
     * @param data the data to be MACed
     * @param kd the key used for MACing
     * @return the MAC
     * @throws SMException
     */
    byte[] generateEDE_MAC(byte[] data, T kd) throws SMException;

    /**
     * Translate key from encryption under the LMK held in key change storage
     * to encryption under a new LMK.
     *
     * @param kd the key encrypted under old LMK
     * @return key encrypted under the new LMK
     * @throws SMException
     */
    SecureDESKey translateKeyFromOldLMK(SecureDESKey kd) throws SMException;


    /**
     * Translate key from encryption under the LMK held in key change storage
     * to encryption under a new LMK.
     *
     * @param key the key encrypted under old LMK
     * @param keySpec the specification of the key to be translated. It allows
     *        passing new key block attributes.
     * @return key encrypted under the new LMK
     * @throws SMException
     */
    SecureKey translateKeyFromOldLMK(SecureKey key, SecureKeySpec keySpec) throws SMException;


    /**
     * Generate a public/private key pair.
     *
     * @param spec algorithm specific parameters, e.g. algorithm, key size,
     *        public key exponent.
     * @return key pair generated according to passed parameters
     * @throws SMException
     */
    Pair<PublicKey, SecurePrivateKey> generateKeyPair(AlgorithmParameterSpec spec)
      throws SMException;


    /**
     * Generate a public/private key pair.
     *
     * @param keySpec the specification of the key to be generated. It allows
     *        passing key algorithm type, size and key block attributes.
     *        NOTE: For pass an extra key usage of the RSA key, possible is use
     *        e.g. {@code keySpec.setVariant()} or {@code keySpec.setReserved()}
     * @return key pair generated according to passed parameters
     * @throws SMException
     */
    Pair<PublicKey, SecureKey> generateKeyPair(SecureKeySpec keySpec) throws SMException;


    /**
     * Calculate signature of Data Block.
     *
     * @param hash identifier of the hash algorithm used to hash passed data.
     * @param privateKey private key used to compute data signature.
     * @param data data to be signed.
     * @return signature of passed data.
     * @throws SMException
     */
    byte[] calculateSignature(MessageDigest hash, SecureKey privateKey
            ,byte[] data) throws SMException;


    /**
     * Encrypts clear Data Block with specified cipher.
     * <p>
     * NOTE: This is a more general version of the
     * {@link #encryptData(CipherMode, SecureDESKey, byte[], byte[])}
     *
     * @param encKey the data encryption key e.g:
     *        <ul>
     *          <li>when RSA public key encapsulated in {@code SecurePrivateKey}
     *          <li>when DES/TDES DEK {@code SecureDESKey}
     *        </ul>
     * @param data clear data block to encrypt
     * @param algspec algorithm specification or {@code null} if not required.
     *        Used to pass additional algorithm parameters e.g:
     *        {@code OAEPParameterSpec} or custom extension of
     *        {@code AlgorithmParameterSpec} to pass symetric cipher mode ECB, CBC
     * @param iv the inital vector or {@code null} if not used <i>(e.g: RSA
     *        cipher or ECB mode)</i>. If used, after operation will contain new
     *        {@code iv} value.
     * @return encrypted data block
     * @throws SMException
     */
    byte[] encryptData(SecureKey encKey, byte[] data
            , AlgorithmParameterSpec algspec, byte[] iv) throws SMException;


    /**
     * Decrypts encrypted Data Block with specified cipher.
     * <p>
     * NOTE: This is a more general version of the
     * {@link #decryptData(CipherMode, SecureDESKey, byte[], byte[])}
     *
     * @param decKey the data decryption key e.g:
     *        <ul>
     *          <li>when RSA private key encapsulated in {@code SecurePrivateKey}
     *          <li>when DES/TDES DEK {@code SecureDESKey}
     *        </ul>
     * @param data encrypted data block to decrypt
     * @param algspec algorithm specification or {@code null} if not required.
     *        Used to pass additional algorithm parameters e.g:
     *        {@code OAEPParameterSpec} or custom extension of
     *        {@code AlgorithmParameterSpec} to pass symetric cipher mode ECB, CBC
     * @param iv the inital vector or {@code null} if not used <i>(e.g: RSA
     *        cipher or ECB mode)</i>. If used, after operation will contain new
     *        {@code iv} value.
     * @return decrypted data block
     * @throws SMException
     */
    byte[] decryptData(SecureKey decKey, byte[] data
            , AlgorithmParameterSpec algspec, byte[] iv) throws SMException;


    /**
     * Erase the key change storage area of memory
     *
     * It is recommended that this command is used after keys stored
     * by the Host have been translated from old to new LMKs.
     *
     * @throws SMException
     */
    void eraseOldLMK() throws SMException;

    /**
     * Encrypt Data
     * @param bdk base derivation key
     * @param clearText clear Text
     * @return cyphertext
     */
    byte[] dataEncrypt(T bdk, byte[] clearText) throws SMException;

    /**
     * Decrypt Data
     * @param bdk base derivation key
     * @param cypherText clear Text
     * @return cleartext
     */
    byte[] dataDecrypt(T bdk, byte[] cypherText) throws SMException;

    /**
     * Forms a key from 3 clear components and returns it encrypted under its corresponding LMK
     * The corresponding LMK is determined from the keyType
     * @param keyLength e.g. LENGTH_DES, LENGTH_DES3_2, LENGTH_DES3_3, ..
     * @param keyType possible values are those defined in the SecurityModule inteface. e.g., ZMK, TMK,...
     * @param clearComponent up to three HexStrings containing key components
     * @return forms an SecureDESKey from two clear components
     * @throws SMException
     */
    SecureDESKey formKEYfromClearComponents(short keyLength, String keyType, String... clearComponent) throws SMException;

    /**
     * Generates a random clear key component.
     * @param keyLength
     * @return clear key componenet
     * @throws SMException
     */
    default String generateClearKeyComponent(short keyLength) throws SMException {
        throw new SMException("Operation not supported in: " + this.getClass().getName());
    }

}

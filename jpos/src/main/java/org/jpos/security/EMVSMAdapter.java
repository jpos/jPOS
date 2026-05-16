/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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

package org.jpos.security;

/**
 * Extension of {@link SMAdapter} that adds public APIs for EMV cryptographic
 * primitives whose computations already exist inside jPOS but are not yet
 * exposed for direct generation use.
 * <p>
 * Methods declared here are <b>generation counterparts</b> of existing
 * verification methods on {@link SMAdapter}. The values returned must satisfy
 * the corresponding verifier; that is, for any valid set of inputs:
 * <ul>
 *   <li>{@link #generateCVC3} must produce a CVC3 accepted by
 *       {@link SMAdapter#verifyCVC3};
 *   <li>{@link #generatedCVV} must produce a dCVV accepted by
 *       {@link SMAdapter#verifydCVV};
 *   <li>{@link #generateApplicationCryptogram} must produce an
 *       ARQC/TC/AAC accepted by {@link SMAdapter#verifyARQC}.
 * </ul>
 * <p>
 * Some methods also return key-derivation results via
 * {@link EMVDerivedKey}; see e.g. {@link #deriveICCMasterKey}.
 * <p>
 * Implementations that cannot perform a given operation should leave the
 * default {@link BaseSMAdapter} implementation in place, which throws
 * {@link SMException}.
 *
 * @param <T> the SecureKey implementation type
 */
public interface EMVSMAdapter<T> extends SMAdapter<T> {

    /**
     * Generate a Dynamic Card Verification Code 3 (CVC3).
     * <p>
     * This is the generation counterpart of
     * {@link #verifyCVC3(Object, String, String, byte[], byte[], byte[], MKDMethod, String)}.
     * Inputs have the same semantics; the result is the full 5-digit CVC3
     * string that the verifier would compute internally.
     *
     * @param imkcvc3 issuer master key for CVC3, encrypted under the LMK
     * @param accountNo account number including BIN and check digit
     * @param acctSeqNo account sequence number, 2 decimal digits, or {@code null}
     * @param atc application transaction counter (2 bytes)
     * @param upn unpredictable number (4 bytes)
     * @param data static track data, or a pre-computed 2-byte IVCVC3
     * @param mkdm ICC master-key derivation method; if {@code null},
     *        {@link MKDMethod#OPTION_A} is used
     * @return the 5-digit CVC3 (zero-padded on the left if numerically short)
     * @throws SMException on security module error
     */
    String generateCVC3(T imkcvc3, String accountNo, String acctSeqNo,
                        byte[] atc, byte[] upn, byte[] data, MKDMethod mkdm)
            throws SMException;

    /**
     * Generate a Dynamic Card Verification Value (dCVV).
     * <p>
     * This is the generation counterpart of
     * {@link #verifydCVV(String, Object, String, String, String, byte[], MKDMethod)}.
     * Inputs have the same semantics; the result is the dCVV string that the
     * verifier would compute internally.
     *
     * @param accountNo account number including BIN and check digit
     * @param imkac issuer master key for application cryptograms, encrypted
     *        under the LMK
     * @param expDate card expiration date as {@code yyMM}
     * @param serviceCode 3-digit service code
     * @param atc application transaction counter (2 bytes)
     * @param mkdm ICC master-key derivation method; if {@code null},
     *        {@link MKDMethod#OPTION_A} is used
     * @return the dCVV string
     * @throws SMException on security module error
     */
    String generatedCVV(String accountNo, T imkac, String expDate,
                        String serviceCode, byte[] atc, MKDMethod mkdm)
            throws SMException;

    /**
     * Generate an Application Cryptogram (ARQC / TC / AAC).
     * <p>
     * The three cryptogram types share the same MAC computation; the type the
     * card emits is determined by the {@code Cryptogram Information Data} byte
     * supplied as part of {@code txnData}. This method is the generation
     * counterpart of
     * {@link SMAdapter#verifyARQC(MKDMethod, SKDMethod, Object, String, String, byte[], byte[], byte[], byte[])}
     * and is expected to return bytes that the verifier would accept.
     *
     * @param mkdm ICC Master Key Derivation Method. For {@code skdm} equals
     *        {@link SKDMethod#VSDC} and {@link SKDMethod#MCHIP} this parameter
     *        is ignored and {@link MKDMethod#OPTION_A} is always used.
     * @param skdm Session Key Derivation Method
     * @param imkac the issuer master key for generating and verifying
     *        Application Cryptograms, encrypted under the LMK
     * @param accountNo account number including BIN and check digit
     * @param acctSeqNo account sequence number, 2 decimal digits
     * @param atc application transaction counter. This is used for Session
     *        Key Generation. A 2 byte value must be supplied.
     *        For {@code skdm} equals {@link SKDMethod#VSDC} is not used.
     * @param upn unpredictable number. This is used for Session Key Generation.
     *        A 4 byte value must be supplied. For {@code skdm} equals
     *        {@link SKDMethod#VSDC} is not used.
     * @param txnData transaction data. Transaction data elements and their
     *        order is dependent to proper cryptogram version. If the data
     *        supplied is a multiple of 8 bytes, no extra padding is added.
     *        If it is not a multiple of 8 bytes, additional zero padding is added.
     *        <b>If alternative padding methods are required, it has to be
     *        applied before</b>.
     * @return 8-byte computed Application Cryptogram
     * @throws SMException on security module error
     */
    byte[] generateApplicationCryptogram(MKDMethod mkdm, SKDMethod skdm, T imkac,
                        String accountNo, String acctSeqNo, byte[] atc,
                        byte[] upn, byte[] txnData)
            throws SMException;

    /**
     * Derive an ICC Master Key from an Issuer Master Key (IMK) and the
     * cardholder's PAN / PAN Sequence Number.
     * <p>
     * The result wraps the derived ICC Master Key in whatever representation
     * the underlying security module uses for {@link SecureKey} (the JCE
     * adapter wraps it as a {@link SecureDESKey} under the LMK; a future
     * ANSI X9.143 / TR-31 HSM adapter would wrap it as a key block). The
     * derived key shares the IMK's usage family — the JCE adapter preserves
     * the IMK's {@code keyType} and {@code keyLength} on the derived
     * {@link SecureDESKey}.
     * <p>
     * The derivation follows EMV v4.2 Book 2, Annex A1.4. For {@code mkdm}
     * equals {@link MKDMethod#OPTION_B} on a PAN longer than 16 digits, the
     * Option B (SHA-1 decimalised) path is used; otherwise Option A is used.
     * This matches the formatting used internally by
     * {@link SMAdapter#verifyARQC}.
     *
     * @param mkdm ICC Master Key Derivation Method. If {@code null},
     *        {@link MKDMethod#OPTION_A} is used.
     * @param imk the issuer master key, wrapped per the adapter's convention
     * @param pan account number including BIN and check digit
     * @param psn PAN Sequence Number, 2 decimal digits; if {@code null} or
     *        empty, treated as {@code "00"}
     * @return the derived ICC Master Key paired with its 3-byte Key Check Value
     * @throws SMException on security module error
     */
    EMVDerivedKey<T> deriveICCMasterKey(MKDMethod mkdm, T imk,
                                        String pan, String psn)
            throws SMException;

    /**
     * Derive an EMV Application Cryptogram session key from an ICC Master
     * Key and the per-transaction diversifiers.
     * <p>
     * The result wraps the session key in whatever representation the
     * underlying security module uses for {@link SecureKey}. The derived
     * key inherits the ICC Master Key's {@code keyType} and {@code keyLength}.
     * <p>
     * Per-{@code skdm} input usage:
     * <ul>
     *   <li>{@link SKDMethod#VSDC} — VSDC has no session-key derivation; the
     *       application cryptogram is MAC'd with the ICC Master Key directly.
     *       For API uniformity this method returns the ICC Master Key
     *       rewrapped (same bytes, same KCV). {@code atc} and {@code upn}
     *       are ignored.
     *   <li>{@link SKDMethod#MCHIP} — uses both {@code atc} (2 bytes) and
     *       {@code upn} (4 bytes) as diversifiers; both must be supplied.
     *   <li>{@link SKDMethod#EMV_CSKD} — uses {@code atc} (2 bytes) only;
     *       {@code upn} may be {@code null}.
     * </ul>
     *
     * @param skdm Session Key Derivation Method
     * @param iccMk the ICC Master Key, typically the output of
     *        {@link #deriveICCMasterKey}
     * @param atc Application Transaction Counter (2 bytes). Ignored for
     *        {@link SKDMethod#VSDC}.
     * @param upn Unpredictable Number (4 bytes). Used only for
     *        {@link SKDMethod#MCHIP}; ignored otherwise and may be {@code null}.
     * @return the derived session key paired with its 3-byte Key Check Value
     * @throws SMException on security module error or if {@code skdm} is not
     *         supported (e.g. {@link SKDMethod#AEPIS_V40} or
     *         {@link SKDMethod#EMV2000_SKM})
     */
    EMVDerivedKey<T> deriveEMVSessionKey(SKDMethod skdm, T iccMk,
                                         byte[] atc, byte[] upn)
            throws SMException;

    /**
     * Generate an Authorisation Response Cryptogram (ARPC) directly from a
     * supplied key.
     * <p>
     * This is an overload of
     * {@link SMAdapter#generateARPC(MKDMethod, SKDMethod, Object, String, String, byte[], byte[], byte[], ARPCMethod, byte[], byte[])}
     * that skips the IMK + PAN/PSN derivation. The caller passes in the key
     * under which the ARPC is to be computed.
     * <p>
     * <b>Which key to pass</b> depends on the SKDMethod the card uses:
     * <ul>
     *   <li>{@link SKDMethod#VSDC} and {@link SKDMethod#MCHIP} — no
     *       ARPC-session-key derivation in EMV; pass the <i>ICC Master Key</i>
     *       directly (the output of
     *       {@link #deriveICCMasterKey(MKDMethod, Object, String, String)}).
     *   <li>{@link SKDMethod#EMV_CSKD} — pass the AC session key (the output of
     *       {@link #deriveEMVSessionKey(SKDMethod, Object, byte[], byte[])}
     *       with {@code EMV_CSKD}).
     * </ul>
     * Example for EMV_CSKD:
     * <pre>
     *   EMVDerivedKey&lt;T&gt; iccMk   = deriveICCMasterKey(mkdm, imk, pan, psn);
     *   EMVDerivedKey&lt;T&gt; session = deriveEMVSessionKey(SKDMethod.EMV_CSKD,
     *                                                   iccMk.key(), atc, null);
     *   byte[]           arpc    = generateARPC(session.key(), arqc,
     *                                           arpcMethod, arc, propAuthData);
     * </pre>
     * <p>
     * <b>Constraint relaxation:</b> unlike the master-key-based overload, this
     * direct method does <i>not</i> enforce SKDMethod/ARPCMethod compatibility
     * (which the master-key variant enforces via {@code constraintARPCM},
     * rejecting METHOD_2 for VSDC and MCHIP). The caller takes responsibility
     * for choosing combinations the card will accept.
     *
     * @param key the key under which the ARPC is computed (see "Which key
     *        to pass" above), wrapped per the adapter's convention
     * @param arqc the Application Request Cryptogram, 8 bytes
     * @param arpcMethod ARPC calculation method
     * @param arc the Authorisation Response Code, 2 bytes for
     *        {@link ARPCMethod#METHOD_1}; for {@link ARPCMethod#METHOD_2}
     *        this is the 4-byte Card Status Update (CSU)
     * @param propAuthData Proprietary Authentication Data, up to 8 bytes.
     *        Used only for {@link ARPCMethod#METHOD_2}; ignored otherwise
     *        and may be {@code null}.
     * @return 8-byte ARPC for {@link ARPCMethod#METHOD_1}, 4-byte ARPC for
     *         {@link ARPCMethod#METHOD_2}
     * @throws SMException on security module error
     */
    byte[] generateARPC(T key, byte[] arqc, ARPCMethod arpcMethod,
                        byte[] arc, byte[] propAuthData)
            throws SMException;

    /**
     * Derive an EMV Secure Messaging session key from a Secure Messaging
     * Issuer Master Key (IMK-SMI or IMK-SMC), the cardholder PAN/PSN, and
     * the per-session diversifier.
     * <p>
     * The same algorithm computes both the integrity key (SK-SMI) and the
     * confidentiality key (SK-SMC); only the input IMK differs. Pass
     * {@code imk-smi} to get SK-SMI for MACing issuer scripts, or
     * {@code imk-smc} to get SK-SMC for PIN-block encryption. The derived
     * key inherits the IMK's {@code keyType} and {@code keyLength}, so the
     * usage family carries through to the returned {@link SecureKey}.
     * <p>
     * Per-{@code skdm} input usage:
     * <ul>
     *   <li>{@link SKDMethod#VSDC} — uses {@code atc} as a 2-byte
     *       diversifier; {@code arqc} is ignored and may be {@code null}.
     *   <li>{@link SKDMethod#MCHIP} and {@link SKDMethod#EMV_CSKD} — use
     *       {@code arqc} as an 8-byte diversifier (typically the ARQC
     *       itself for the first script, then {@code ARQC + n} for
     *       subsequent scripts); {@code atc} is ignored and may be
     *       {@code null}.
     * </ul>
     * <p>
     * <b>Note on dispatch:</b> SM session-key derivation is <i>not</i> the
     * same algorithm as AC session-key derivation. In particular, MCHIP
     * uses {@code deriveSK_MK(atc, upn)} for AC (see
     * {@link #deriveEMVSessionKey}) but {@code deriveCommonSK_SM(arqc)}
     * for SM. Use the right method for the right purpose.
     *
     * @param mkdm ICC Master Key Derivation Method. If {@code null},
     *        {@link MKDMethod#OPTION_A} is used.
     * @param skdm Session Key Derivation Method
     * @param imk the SM Issuer Master Key — pass IMK-SMI for SK-SMI or
     *        IMK-SMC for SK-SMC
     * @param pan account number including BIN and check digit
     * @param psn PAN Sequence Number, 2 decimal digits; if {@code null} or
     *        empty, treated as {@code "00"}
     * @param atc Application Transaction Counter (2 bytes). Used only for
     *        {@link SKDMethod#VSDC}.
     * @param arqc the per-session diversifier (8 bytes). Used only for
     *        {@link SKDMethod#MCHIP} and {@link SKDMethod#EMV_CSKD}.
     * @return the derived SM session key paired with its 3-byte KCV
     * @throws SMException on security module error or if {@code skdm} is
     *         not supported
     * @see SMAdapter#generateSM_MAC
     * @see SMAdapter#translatePINGenerateSM_MAC
     */
    EMVDerivedKey<T> deriveSecureMessagingSessionKey(MKDMethod mkdm, SKDMethod skdm,
                                                    T imk, String pan, String psn,
                                                    byte[] atc, byte[] arqc)
            throws SMException;

    /**
     * Generate a Secure Messaging MAC directly from a supplied session key.
     * <p>
     * This is an overload of
     * {@link SMAdapter#generateSM_MAC(MKDMethod, SKDMethod, Object, String, String, byte[], byte[], byte[])}
     * that skips the IMK + PAN/PSN derivation. The caller passes the
     * SK-SMI directly — typically the output of
     * {@link #deriveSecureMessagingSessionKey(MKDMethod, SKDMethod, Object, String, String, byte[], byte[])}
     * with an IMK-SMI — closing the loop on the SM-MAC derivation chain:
     * <pre>
     *   EMVDerivedKey&lt;T&gt; skSmi = deriveSecureMessagingSessionKey(
     *           mkdm, skdm, imkSmi, pan, psn, atc, arqc);
     *   byte[]           mac   = generateSM_MAC(skSmi.key(), data);
     * </pre>
     * <p>
     * The implementation applies ISO/IEC 9797-1 padding method 2 (append
     * {@code 0x80} then {@code 0x00} pad to the next 8-byte boundary) and
     * computes the ISO/IEC 9797-1 MAC algorithm 3 (Triple-DES retail MAC).
     * Both match the padding+MAC used internally by the master-key-based
     * {@code generateSM_MAC} for VSDC, MCHIP, and EMV_CSKD, so the returned
     * 8 bytes are bit-identical to what the integrated method would produce
     * given the same inputs and derivation.
     * <p>
     * Callers requiring alternative padding (e.g. Visa, CCD, or M/Chip
     * SM-MAC padding variants) should pre-pad the input themselves; this
     * method does not provide a padding-method selector.
     *
     * @param sessionKey the SK-SMI, wrapped per the adapter's convention
     * @param data the data to MAC (pre-padding)
     * @return the 8-byte Secure Messaging MAC
     * @throws SMException on security module error
     */
    byte[] generateSM_MAC(T sessionKey, byte[] data) throws SMException;

    /**
     * Encrypt a PIN block for Secure Messaging directly under a supplied
     * SM session encryption key.
     * <p>
     * This is the SM-PIN-encrypt half of the integrated
     * {@link SMAdapter#translatePINGenerateSM_MAC translatePINGenerateSM_MAC},
     * exposed as a stand-alone primitive. The caller supplies the SK-SMC
     * directly — typically the output of
     * {@link #deriveSecureMessagingSessionKey(MKDMethod, SKDMethod, Object, String, String, byte[], byte[])}
     * with an IMK-SMC — and the new PIN already encrypted under an existing
     * key {@code kd1}. The method translates the PIN into
     * {@code destinationPINBlockFormat} and re-encrypts it under the
     * SK-SMC for transmission to the card in an issuer script.
     * <p>
     * Composability:
     * <pre>
     *   EMVDerivedKey&lt;T&gt; udkAc  = deriveICCMasterKey(mkdm, imkAc,  pan, psn);  // FORMAT41/42 only
     *   EMVDerivedKey&lt;T&gt; udkSmc = deriveICCMasterKey(mkdm, imkSmc, pan, psn);  // not strictly needed; PR 7 below does its own derivation
     *   EMVDerivedKey&lt;T&gt; skSmc  = deriveSecureMessagingSessionKey(
     *           mkdm, skdm, imkSmc, pan, psn, atc, arqc);
     *   EncryptedPIN     enc    = encryptSecureMessagingPIN(
     *           skSmc.key(), newPIN, kd1, destinationPINBlockFormat,
     *           paddingMethod, currentPIN, udkAc.key());
     * </pre>
     * <p>
     * Per-format input requirements:
     * <ul>
     *   <li>{@code FORMAT34}, {@code FORMAT35} (EMV / Mastercard): only
     *       {@code sessionKey}, {@code newPIN}, {@code kd1},
     *       {@code destinationPINBlockFormat}, {@code paddingMethod} are
     *       used. {@code currentPIN} and {@code udkAc} may be {@code null}.
     *   <li>{@code FORMAT41} (Visa, new PIN only): {@code udkAc} is
     *       required (used as the PAN-like diversifier in the PIN block).
     *       {@code currentPIN} may be {@code null}.
     *   <li>{@code FORMAT42} (Visa, PIN change with current PIN): both
     *       {@code currentPIN} and {@code udkAc} are required.
     * </ul>
     * <p>
     * If {@code paddingMethod} is {@code null}, {@link PaddingMethod#MCHIP}
     * is used (no SM-specific padding added). The integrated
     * {@code translatePINGenerateSM_MAC} auto-derives padding from the
     * SKDMethod; this direct method has no SKDMethod context, so the
     * caller picks.
     *
     * @param sessionKey the SM session encryption key (SK-SMC), wrapped
     *        per the adapter's convention
     * @param newPIN the PIN currently wrapped under {@code kd1}
     * @param kd1 the key currently wrapping {@code newPIN}
     * @param destinationPINBlockFormat the target PIN block format
     *        (e.g. {@link SMAdapter#FORMAT34}, {@link SMAdapter#FORMAT35},
     *        {@link SMAdapter#FORMAT41}, {@link SMAdapter#FORMAT42})
     * @param paddingMethod the SM padding method; {@code null} defaults to
     *        {@link PaddingMethod#MCHIP}
     * @param currentPIN the current PIN wrapped under {@code kd1};
     *        required only for {@link SMAdapter#FORMAT42}, otherwise
     *        may be {@code null}
     * @param udkAc the ICC AC Master Key (typically the output of
     *        {@link #deriveICCMasterKey} with an IMK-AC), wrapped per the
     *        adapter's convention; required only for
     *        {@link SMAdapter#FORMAT41} and {@link SMAdapter#FORMAT42},
     *        otherwise may be {@code null}
     * @return the PIN re-encrypted under the SK-SMC in
     *         {@code destinationPINBlockFormat}
     * @throws SMException on security module error
     * @see SMAdapter#translatePINGenerateSM_MAC
     * @see SMAdapter#translatePIN
     */
    EncryptedPIN encryptSecureMessagingPIN(T sessionKey, EncryptedPIN newPIN,
            T kd1, byte destinationPINBlockFormat, PaddingMethod paddingMethod,
            EncryptedPIN currentPIN, T udkAc) throws SMException;

    /**
     * Recover an Issuer Public Key from an EMV Issuer Public Key Certificate
     * signed by a Certification Authority, per EMV 4.4 Book 2 §6.
     * <p>
     * The certificate, remainder, and exponent inputs correspond to EMV
     * tags {@code 0x90} (Issuer Public Key Certificate), {@code 0x92}
     * (Issuer Public Key Remainder, optional) and {@code 0x9F32} (Issuer
     * Public Key Exponent) respectively, as read from the card.
     * <p>
     * The implementation performs RSA recovery {@code cert^e mod n} under
     * the supplied CA public key and validates every field EMV mandates:
     * recovered data header {@code 0x6A}, certificate format {@code 0x02},
     * trailer {@code 0xBC}, hash algorithm indicator, public key algorithm
     * indicator, length consistency, SHA-1 hash over the cert payload +
     * remainder + exponent, certificate expiration date against the
     * current system clock, and (when {@code pan} is non-null) issuer
     * identifier match with the leftmost PAN digits.
     * <p>
     * <b>Every validation is a hard failure</b>: any inconsistency throws
     * {@link SMException} with a message naming the failed step, and no
     * partial result is returned. Callers who need lenient behavior
     * (e.g. recovering keys from expired certs for forensics) should
     * pre-process inputs rather than catching exceptions.
     * <p>
     * <b>Algorithm support</b>: the JCE adapter currently implements only
     * hash algorithm indicator {@code 0x01} (SHA-1) and public key
     * algorithm indicator {@code 0x01} (RSA). Other indicators raise
     * {@link SMException}; support can be extended when EMV consumers
     * need SHA-256 variants.
     *
     * @param caPublicKey the CA public key, identified per scheme by its
     *        RID + index
     * @param issuerPublicKeyCertificate EMV tag {@code 0x90} contents;
     *        length must equal the CA modulus length in bytes
     * @param issuerPublicKeyRemainder EMV tag {@code 0x92} contents, or
     *        {@code null} / empty when the issuer modulus fits entirely
     *        inside the certificate
     * @param issuerPublicKeyExponent EMV tag {@code 0x9F32} contents
     * @param pan the cardholder PAN, used to verify the issuer identifier
     *        embedded in the certificate; if {@code null} or empty, the
     *        issuer-identifier check is skipped (useful for diagnostic
     *        recovery without a card context)
     * @return the recovered Issuer Public Key
     * @throws SMException on RSA recovery failure or any EMV validation
     *         failure (header, format, trailer, lengths, hash, expiration,
     *         issuer identifier, unsupported algorithm indicator)
     */
    EMVIssuerPublicKey recoverIssuerPublicKey(
            EMVCAPublicKey caPublicKey,
            byte[] issuerPublicKeyCertificate,
            byte[] issuerPublicKeyRemainder,
            byte[] issuerPublicKeyExponent,
            String pan) throws SMException;

    /**
     * Recover an ICC Public Key from an EMV ICC Public Key Certificate
     * signed by the issuer, per EMV 4.4 Book 2 §6.4.
     * <p>
     * The certificate, remainder, exponent, and static application data
     * inputs correspond to EMV tags {@code 0x9F46} (ICC Public Key
     * Certificate), {@code 0x9F48} (ICC Public Key Remainder, optional),
     * {@code 0x9F47} (ICC Public Key Exponent), and the Static Application
     * Data assembled by the terminal per EMV Book 3 §10.3.
     * <p>
     * The natural caller flow is to chain this with
     * {@link #recoverIssuerPublicKey(EMVCAPublicKey, byte[], byte[], byte[], String)}:
     * <pre>
     *   EMVIssuerPublicKey issuer = recoverIssuerPublicKey(ca, issuerCert,
     *           issuerRemainder, issuerExp, pan);
     *   EMVICCPublicKey    icc    = recoverICCPublicKey(issuer, iccCert,
     *           iccRemainder, iccExp, staticApplicationData, pan);
     * </pre>
     * <p>
     * The implementation performs RSA recovery {@code cert^e mod n} under
     * the supplied <i>issuer</i> public key and validates every field EMV
     * mandates: recovered data header {@code 0x6A}, certificate format
     * {@code 0x04} (note: differs from the issuer cert format {@code 0x02}),
     * trailer {@code 0xBC}, hash algorithm indicator, public key algorithm
     * indicator, length consistency, SHA-1 hash over the cert payload +
     * remainder + exponent <b>+ Static Application Data</b>, certificate
     * expiration date against the current system clock, and (when {@code pan}
     * is non-null) Application PAN match.
     * <p>
     * The SAD inclusion in the hash input is the key semantic difference
     * vs. issuer certificate recovery — it's how the issuer cryptographically
     * commits the ICC public key to a specific set of card records. Passing
     * a different SAD at recovery time than was used at certificate creation
     * causes the hash validation step to fail.
     * <p>
     * Every validation is a hard failure (see
     * {@link #recoverIssuerPublicKey} for the same model). The JCE adapter
     * currently supports hash algorithm indicator {@code 0x01} (SHA-1) and
     * public key algorithm indicator {@code 0x01} (RSA) only.
     *
     * @param issuerPublicKey the recovered Issuer Public Key, typically
     *        the output of {@link #recoverIssuerPublicKey}
     * @param iccPublicKeyCertificate EMV tag {@code 0x9F46} contents;
     *        length must equal the issuer modulus length in bytes
     * @param iccPublicKeyRemainder EMV tag {@code 0x9F48} contents, or
     *        {@code null} / empty when the ICC modulus fits entirely
     *        inside the certificate
     * @param iccPublicKeyExponent EMV tag {@code 0x9F47} contents
     * @param staticApplicationData the data assembled per EMV Book 3
     *        §10.3 (records read from the AFL, optionally followed by the
     *        Static Data Authentication Tag List); included in the SHA-1
     *        hash validation
     * @param pan the cardholder PAN; if {@code null} or empty, the
     *        Application PAN check is skipped
     * @return the recovered ICC Public Key
     * @throws SMException on RSA recovery failure or any EMV validation
     *         failure
     */
    EMVICCPublicKey recoverICCPublicKey(
            EMVIssuerPublicKey issuerPublicKey,
            byte[] iccPublicKeyCertificate,
            byte[] iccPublicKeyRemainder,
            byte[] iccPublicKeyExponent,
            byte[] staticApplicationData,
            String pan) throws SMException;

    /**
     * Verify EMV Signed Static Application Data (SSAD, tag {@code 0x93})
     * per EMV 4.4 Book 2 §5.4 and return the Data Authentication Code
     * (DAC) the issuer embedded in the signed payload.
     * <p>
     * The natural caller flow chains this with PR 9's
     * {@link #recoverIssuerPublicKey(EMVCAPublicKey, byte[], byte[], byte[], String)}:
     * <pre>
     *   EMVIssuerPublicKey issuer = recoverIssuerPublicKey(ca, issuerCert,
     *           issuerRemainder, issuerExp, pan);
     *   byte[]             dac    = verifySDA(issuer, ssad, staticApplicationData);
     *   // store dac in EMV tag 0x9F45 for subsequent transaction data
     * </pre>
     * <p>
     * The implementation performs RSA recovery {@code ssad^e mod n}
     * under the supplied issuer public key and validates the recovered
     * structure: header {@code 0x6A}, signed-data-format {@code 0x03}
     * (distinct from {@code 0x02} for issuer certificates and
     * {@code 0x04} for ICC certificates), trailer {@code 0xBC}, hash
     * algorithm indicator, mandatory {@code 0xBB} pad pattern, and a
     * SHA-1 hash over (signed-data-format + hash-alg + DAC + pad-pattern)
     * concatenated with the supplied Static Application Data.
     * <p>
     * <b>Pad pattern check is mandatory.</b> EMV §5.4 lists this under
     * structural verification guidance; this adapter treats any
     * deviation from {@code 0xBB} as a hard failure rather than hiding a
     * card conformance defect. Callers needing lenient behavior should
     * pre-process the SSAD.
     * <p>
     * <b>Caller assembles SAD.</b> Per EMV Book 3 §10.3, terminals
     * concatenate the records identified by the AFL plus (optionally)
     * the Static Data Authentication Tag List. That assembly is
     * application policy, outside this method's scope — the verify
     * method consumes whatever bytes the caller passes.
     *
     * @param issuerPublicKey the recovered Issuer Public Key (typically
     *        the output of {@link #recoverIssuerPublicKey})
     * @param signedStaticApplicationData EMV tag {@code 0x93} contents;
     *        length must equal the issuer modulus length
     * @param staticApplicationData the SAD bytes assembled per EMV
     *        Book 3 §10.3 — same bytes the issuer hashed when signing
     * @return the 2-byte Data Authentication Code (to be stored in EMV
     *         tag {@code 0x9F45})
     * @throws SMException on RSA recovery failure or any EMV
     *         validation failure
     */
    byte[] verifySDA(
            EMVIssuerPublicKey issuerPublicKey,
            byte[] signedStaticApplicationData,
            byte[] staticApplicationData) throws SMException;

    /**
     * Verify EMV Signed Dynamic Application Data (SDAD, tag {@code 0x9F4B})
     * returned by the card from INTERNAL AUTHENTICATE, per EMV 4.4 Book 2
     * §6.5, and return the ICC Dynamic Number the card signed inside it.
     * <p>
     * DDA proves the card holds the ICC private key — a stronger signal
     * than SDA (which only proves the issuer signed static records). The
     * natural caller flow chains all three offline-auth recovery steps:
     * <pre>
     *   EMVIssuerPublicKey issuer = recoverIssuerPublicKey(ca, issuerCert,
     *           issuerRemainder, issuerExp, pan);
     *   EMVICCPublicKey    icc    = recoverICCPublicKey(issuer, iccCert,
     *           iccRemainder, iccExp, staticApplicationData, pan);
     *   byte[]             dyn    = verifyDDA(icc, sdad, ddolData);
     * </pre>
     * <p>
     * The implementation performs RSA recovery {@code sdad^e mod n} under
     * the ICC public key and validates the recovered structure:
     * header {@code 0x6A}, signed-data-format {@code 0x05} (distinct from
     * {@code 0x03} for SSAD), trailer {@code 0xBC}, hash algorithm
     * indicator, LDD and LDN sanity (the latter must fall in
     * {@code [2, 8]} per EMV §6.5.2), mandatory {@code 0xBB} pad pattern,
     * and a SHA-1 hash over the recovered payload (minus header / hash /
     * trailer) concatenated with the supplied DDOL bytes.
     * <p>
     * <b>Pad pattern check is mandatory</b> — same rationale as
     * {@link #verifySDA}.
     * <p>
     * <b>Caller is responsible for DDOL assembly.</b> Per EMV §10.4, the
     * terminal builds DDOL by substituting its data elements into the
     * card-supplied DDOL template (tag {@code 0x9F49}). That assembly is
     * application policy, outside this method's scope — the verify
     * method consumes whatever bytes the caller passes.
     *
     * @param iccPublicKey the recovered ICC Public Key (typically the
     *        output of {@link #recoverICCPublicKey})
     * @param signedDynamicApplicationData EMV tag {@code 0x9F4B} contents;
     *        length must equal the ICC modulus length
     * @param ddolData the DDOL bytes the terminal sent to INTERNAL
     *        AUTHENTICATE — same bytes the card hashed when signing
     * @return the ICC Dynamic Number (variable length, 2 to 8 bytes)
     * @throws SMException on RSA recovery failure or any EMV validation
     *         failure
     */
    byte[] verifyDDA(
            EMVICCPublicKey iccPublicKey,
            byte[] signedDynamicApplicationData,
            byte[] ddolData) throws SMException;
}

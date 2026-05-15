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
}

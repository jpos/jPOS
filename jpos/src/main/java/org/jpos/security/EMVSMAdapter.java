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
}

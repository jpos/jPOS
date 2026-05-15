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
 *       {@link SMAdapter#verifydCVV}.
 * </ul>
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
}

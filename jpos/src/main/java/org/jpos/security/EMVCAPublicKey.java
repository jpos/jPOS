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

import java.util.Arrays;

import org.jpos.iso.ISOUtil;

/**
 * EMV Certification Authority (CA) Public Key, as used to verify Issuer
 * Public Key Certificates during offline data authentication.
 * <p>
 * CA public keys are published per scheme (Visa, Mastercard, JCB, etc.)
 * and identified by the pair ({@link #rid}, {@link #index}). They are
 * clear public values — there is no LMK wrapping involved — so this
 * type holds the modulus and exponent as raw {@code byte[]}.
 * <p>
 * Used as input to
 * {@link EMVSMAdapter#recoverIssuerPublicKey(EMVCAPublicKey, byte[], byte[], byte[], String)}.
 *
 * @param rid 5-byte Registered Application Provider Identifier
 *        (scheme-specific; e.g. Visa = {@code A000000003})
 * @param index 1-byte CA public key index within the RID
 * @param modulus big-endian RSA modulus, variable length per spec
 *        (EMV currently allows 1024 to 1984 bits)
 * @param exponent big-endian RSA public exponent, typically
 *        {@code {0x03}} or {@code {0x01, 0x00, 0x01}}
 * @param hashAlgorithmIndicator hash algorithm identifier as defined by
 *        EMV (0x01 = SHA-1; other values currently unsupported by the
 *        JCE adapter)
 * @param publicKeyAlgorithmIndicator public key algorithm identifier
 *        (0x01 = RSA)
 */
public record EMVCAPublicKey(byte[] rid, byte index, byte[] modulus,
                             byte[] exponent, byte hashAlgorithmIndicator,
                             byte publicKeyAlgorithmIndicator) {

    /**
     * Creates an EMV CA public key and defensively copies array components.
     */
    public EMVCAPublicKey {
        rid = copy(rid);
        modulus = copy(modulus);
        exponent = copy(exponent);
    }

    /**
     * Returns a defensive copy of the RID.
     *
     * @return the Registered Application Provider Identifier
     */
    @Override
    public byte[] rid() {
        return copy(rid);
    }

    /**
     * Returns a defensive copy of the RSA modulus.
     *
     * @return the RSA modulus
     */
    @Override
    public byte[] modulus() {
        return copy(modulus);
    }

    /**
     * Returns a defensive copy of the RSA public exponent.
     *
     * @return the RSA public exponent
     */
    @Override
    public byte[] exponent() {
        return copy(exponent);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EMVCAPublicKey other)) return false;
        return index == other.index
                && hashAlgorithmIndicator == other.hashAlgorithmIndicator
                && publicKeyAlgorithmIndicator == other.publicKeyAlgorithmIndicator
                && Arrays.equals(rid, other.rid)
                && Arrays.equals(modulus, other.modulus)
                && Arrays.equals(exponent, other.exponent);
    }

    @Override
    public int hashCode() {
        int h = Arrays.hashCode(rid);
        h = 31 * h + index;
        h = 31 * h + Arrays.hashCode(modulus);
        h = 31 * h + Arrays.hashCode(exponent);
        h = 31 * h + hashAlgorithmIndicator;
        h = 31 * h + publicKeyAlgorithmIndicator;
        return h;
    }

    @Override
    public String toString() {
        return "EMVCAPublicKey[rid=" + (rid == null ? "" : ISOUtil.hexString(rid))
                + ", index=" + String.format("%02X", index & 0xff)
                + ", modulus=" + (modulus == null ? "" : ISOUtil.hexString(modulus))
                + ", exponent=" + (exponent == null ? "" : ISOUtil.hexString(exponent))
                + ", hashAlg=" + String.format("%02X", hashAlgorithmIndicator & 0xff)
                + ", pkAlg=" + String.format("%02X", publicKeyAlgorithmIndicator & 0xff)
                + "]";
    }

    private static byte[] copy(byte[] bytes) {
        return bytes == null ? null : bytes.clone();
    }
}

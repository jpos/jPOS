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
 * EMV Issuer Public Key recovered from an Issuer Public Key Certificate.
 * <p>
 * Returned by
 * {@link EMVSMAdapter#recoverIssuerPublicKey(EMVCAPublicKey, byte[], byte[], byte[], String)}
 * after successful certificate recovery and validation per EMV 4.4
 * Book 2 §6.
 * <p>
 * The issuer public key reconstructed in {@link #modulus} is the
 * concatenation of the leftmost portion present in the certificate and
 * the remainder supplied separately (EMV tag {@code 0x92}), if any. The
 * exponent passes through unchanged from the caller-supplied tag
 * {@code 0x9F32}.
 *
 * @param issuerIdentifier 4 BCD bytes — the leftmost digits of the PAN
 *        right-padded with {@code 0xF} nibbles (e.g. PAN {@code 12345…}
 *        → {@code 12 34 5F FF})
 * @param expirationDate 2 BCD bytes — {@code MM YY} (per EMV
 *        Book 2 §6.3)
 * @param serialNumber 3 bytes — assigned by the CA
 * @param modulus big-endian RSA modulus of the issuer public key, full
 *        length (leftmost-in-cert + remainder)
 * @param exponent big-endian RSA public exponent of the issuer public
 *        key, as supplied by the caller
 * @param hashAlgorithmIndicator hash algorithm identifier from the
 *        certificate (0x01 = SHA-1)
 * @param publicKeyAlgorithmIndicator public key algorithm identifier
 *        from the certificate (0x01 = RSA)
 */
public record EMVIssuerPublicKey(byte[] issuerIdentifier, byte[] expirationDate,
                                 byte[] serialNumber, byte[] modulus,
                                 byte[] exponent, byte hashAlgorithmIndicator,
                                 byte publicKeyAlgorithmIndicator) {

    public EMVIssuerPublicKey {
        issuerIdentifier = copy(issuerIdentifier);
        expirationDate = copy(expirationDate);
        serialNumber = copy(serialNumber);
        modulus = copy(modulus);
        exponent = copy(exponent);
    }

    @Override
    public byte[] issuerIdentifier() {
        return copy(issuerIdentifier);
    }

    @Override
    public byte[] expirationDate() {
        return copy(expirationDate);
    }

    @Override
    public byte[] serialNumber() {
        return copy(serialNumber);
    }

    @Override
    public byte[] modulus() {
        return copy(modulus);
    }

    @Override
    public byte[] exponent() {
        return copy(exponent);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EMVIssuerPublicKey other)) return false;
        return hashAlgorithmIndicator == other.hashAlgorithmIndicator
                && publicKeyAlgorithmIndicator == other.publicKeyAlgorithmIndicator
                && Arrays.equals(issuerIdentifier, other.issuerIdentifier)
                && Arrays.equals(expirationDate, other.expirationDate)
                && Arrays.equals(serialNumber, other.serialNumber)
                && Arrays.equals(modulus, other.modulus)
                && Arrays.equals(exponent, other.exponent);
    }

    @Override
    public int hashCode() {
        int h = Arrays.hashCode(issuerIdentifier);
        h = 31 * h + Arrays.hashCode(expirationDate);
        h = 31 * h + Arrays.hashCode(serialNumber);
        h = 31 * h + Arrays.hashCode(modulus);
        h = 31 * h + Arrays.hashCode(exponent);
        h = 31 * h + hashAlgorithmIndicator;
        h = 31 * h + publicKeyAlgorithmIndicator;
        return h;
    }

    @Override
    public String toString() {
        return "EMVIssuerPublicKey[issuerIdentifier="
                + (issuerIdentifier == null ? "" : ISOUtil.hexString(issuerIdentifier))
                + ", expirationDate=" + (expirationDate == null ? "" : ISOUtil.hexString(expirationDate))
                + ", serialNumber=" + (serialNumber == null ? "" : ISOUtil.hexString(serialNumber))
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

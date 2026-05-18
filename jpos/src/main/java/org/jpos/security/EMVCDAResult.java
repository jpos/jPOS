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
 * Outcome of a successful EMV CDA (Combined DDA + AC) verification, per
 * EMV 4.4 Book 2 §6.6.
 * <p>
 * Returned by
 * {@link EMVSMAdapter#verifyCDA(EMVICCPublicKey, byte[], byte[], byte[], byte[])}
 * after all twelve validation steps pass. The terminal uses
 * {@link #cid} to route the transaction (bits 7-6 of CID select
 * ARQC / TC / AAC) and may use {@link #iccDynamicNumber} for
 * downstream diagnostics.
 *
 * @param iccDynamicNumber the per-transaction nonce the card included
 *        in its signed dynamic data (variable length, 2 to 8 bytes
 *        per EMV §6.5.2)
 * @param cid Cryptogram Information Data byte the card committed to
 *        inside the signature (also returned in tag {@code 0x9F27}
 *        out-of-band)
 */
public record EMVCDAResult(byte[] iccDynamicNumber, byte cid) {

    /**
     * Creates a CDA result and defensively copies array components.
     */
    public EMVCDAResult {
        iccDynamicNumber = copy(iccDynamicNumber);
    }

    /**
     * Returns a defensive copy of the ICC dynamic number.
     *
     * @return the ICC dynamic number
     */
    @Override
    public byte[] iccDynamicNumber() {
        return copy(iccDynamicNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EMVCDAResult other)) return false;
        return cid == other.cid
                && Arrays.equals(iccDynamicNumber, other.iccDynamicNumber);
    }

    @Override
    public int hashCode() {
        int h = Arrays.hashCode(iccDynamicNumber);
        h = 31 * h + cid;
        return h;
    }

    @Override
    public String toString() {
        return "EMVCDAResult[iccDynamicNumber="
                + (iccDynamicNumber == null ? "" : ISOUtil.hexString(iccDynamicNumber))
                + ", cid=" + String.format("%02X", cid & 0xff)
                + "]";
    }

    private static byte[] copy(byte[] bytes) {
        return bytes == null ? null : bytes.clone();
    }
}

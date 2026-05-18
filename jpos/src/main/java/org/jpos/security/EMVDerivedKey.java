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
import java.util.Objects;

import org.jpos.iso.ISOUtil;

/**
 * Result of an EMV key-derivation operation: the derived key paired with its
 * Key Check Value.
 * <p>
 * The {@code key} component is parametric on {@link SecureKey} so that the
 * type carries through whatever wrapping the underlying security module
 * uses. The software JCE adapter populates it with a {@link SecureDESKey}
 * (wrapped under the LMK); a future HSM adapter that speaks ANSI X9.143 /
 * TR-31 key blocks would populate it with its own key-block type.
 * <p>
 * The KCV is the standard 3-byte short check value (the high-order bytes of
 * encrypting an all-zero block with the clear derived key). It is held as a
 * {@code byte[]} to match {@link SecureKey#getKeyCheckValue()}; the
 * {@link #equals(Object)}, {@link #hashCode()} and {@link #toString()}
 * overrides below compare and render it by content rather than by array
 * identity.
 *
 * @param <T> the {@link SecureKey} implementation type carried by the
 *            underlying security module. By convention {@code T} is always
 *            a {@code SecureKey} subtype (matching {@code SMAdapter}'s
 *            usage), though the bound is left unconstrained to stay
 *            compatible with the rest of the {@code org.jpos.security}
 *            public API surface.
 * @param key the derived key, wrapped per the adapter's convention
 * @param kcv the Key Check Value, normally 3 bytes
 */
public record EMVDerivedKey<T>(T key, byte[] kcv) {

    /**
     * Creates a derived-key result and defensively copies array components.
     */
    public EMVDerivedKey {
        kcv = copy(kcv);
    }

    /**
     * Returns a defensive copy of the Key Check Value.
     *
     * @return the Key Check Value
     */
    @Override
    public byte[] kcv() {
        return copy(kcv);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EMVDerivedKey<?> other)) return false;
        return Objects.equals(key, other.key) && Arrays.equals(kcv, other.kcv);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hashCode(key) + Arrays.hashCode(kcv);
    }

    @Override
    public String toString() {
        return "EMVDerivedKey[key=" + key
                + ", kcv=" + (kcv == null ? "" : ISOUtil.hexString(kcv))
                + "]";
    }

    private static byte[] copy(byte[] bytes) {
        return bytes == null ? null : bytes.clone();
    }
}

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

package org.jpos.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Defines the cryptographic algorithm with which the key contained in the key
 * block will be used.
 * <p>
 * Each value repesents byte 7 of the Keyblok Header.
 */
public enum Algorithm {

    /**
     * AES - Advanced Encryption Standard.
     */
    AES         ('A', "AES - Advanced Encryption Standard"),

    /**
     * DES - Data Encryption Standard.
     */
    DES         ('D', "DES - Data Encryption Standard"),

    /**
     * Elliptic curve.
     */
    EC          ('E', "Elliptic curve"),

    /**
     * HMAC - Hash Message Authentication Code.
     */
    HMAC        ('H', "HMAC - Hash Message Authentication Code"),

    /**
     * RSA - Rivest–Shamir–Adleman.
     */
    RSA         ('R', "RSA - Rivest Shamir Adleman"),

    /**
     * DSA - Digital Signature Algorithm.
     */
    DSA         ('S', "DSA - Digital Signature Algorithm"),

    /**
     * TDES - Triple Data Encryption Standard.
     * <p>
     * Also known as TDSA <i>(official Triple Data Encryption Algorithm)</i>.
     */
    TDES        ('T', "Triple DES - Triple Data Encryption Standard");


    private static final Map<Character, Algorithm> MAP = new HashMap<>();

    static {
        for (Algorithm alg : Algorithm.values())
            MAP.put(alg.getCode(), alg);
    }

    private final char code;
    private final String name;

    Algorithm(char code, String name) {
        Objects.requireNonNull(name, "The name of algorithm is required");
        this.code = code;
        this.name = name;
    }

    /**
     * Get algorithm code.
     *
     * @return character algorithm code
     */
    public char getCode() {
        return code;
    }

    /**
     * Get algorithm name.
     *
     * @return the algorithm name
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("Algorithm[code: %s, name: %s]", code, name);
    }

    /**
     * Returns the enum constant of this type with the specified {@code code}.
     *
     * @param code the string must match exactly with identifier specified by
     *        <i>ISO 8583-1:2003(E) Table A.22 — Transaction type codes</i>
     * @return the enum constant with the specified processing code or
     *         {@code null} if unknown.
     */
    public static Algorithm valueOfByCode(char code) {
        return MAP.get(code);
    }

}

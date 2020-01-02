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
 * Defines the operation that the key contained in the key block can perform.
 * <p>
 * Each value represents byte 8 of the Keyblok Header.
 */
public enum ModeOfUse {

    /**
     * The key may be used to perform both encrypt and decrypt operations.
     */
    ENCDEC          ('B', "Encryption and Decryption"),

    /**
     * The key may be used to perform MAC calculation <i>(both generate &
     * verify)</i> operations.
     */
    GENVER          ('C', "Verification and Generation of MAC, CVD"),

    /**
     * The key may only be used to perform decrypt operations.
     */
    DECONLY         ('D', "Data Decryption"),

    /**
     * The key may only be used to perform encrypt operations.
     */
    ENCONLY         ('E', "Data Encryption"),

    /**
     * The key may only be used to perform MAC generate operations.
     */
    GENONLY         ('G', "Generaction of MAC, CVD"),

    /**
     * No special restrictions apply.
     */
    ANY             ('N', "Without restrictions"),

    /**
     * The key may only be used to perform digital signature generation
     * operations.
     */
    GENSIGN         ('S', "Digital Signature Generation"),

    /**
     * The key may be used to perform both digital signature generation and
     * verification operations.
     */
    SIGNVER         ('T', "Digital Signature Generation and Verification"),

    /**
     * The key may only be used to perform digital signature verification
     * operations.
     */
    VERONLY         ('V', "Digital Signature Verification"),

    /**
     * The key may only be used to derive other keys.
     */
    DERIVE          ('X', "Derive Keys"),

    /**
     * The key may be used to create key variants.
     */
    KEYVAR          ('Y', "Key used to create key variants");


    private static final Map<Character, ModeOfUse> MAP = new HashMap<>();

    static {
        for (ModeOfUse tr : ModeOfUse.values())
            MAP.put(tr.getCode(), tr);
    }

    private final char code;
    private final String name;

    ModeOfUse(char code, String name) {
        Objects.requireNonNull(name, "The name of key use mode is required");
        this.code = code;
        this.name = name;
    }

    /**
     * Get code of key use mode.
     *
     * @return the character which represents code of key use mode
     */
    public char getCode() {
        return code;
    }

    /**
     * Get name of key use mode.
     *
     * @return the name of key use mode.
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("ModeOfUse[code: %s, name: %s]", code, name);
    }

    /**
     * Returns the enum constant of this type with the specified {@code code}.
     *
     * @param code the string must match exactly with identifier specified by
     *        <i>ISO 8583-1:2003(E) Table A.22 — Transaction type codes</i>
     * @return the enum constant with the specified processing code or
     *         {@code null} if unknown.
     */
    public static ModeOfUse valueOfByCode(char code) {
        return MAP.get(code);
    }

}

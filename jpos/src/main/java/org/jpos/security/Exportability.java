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
 * Defines the conditions under which the key contained in the key block can be
 * exported outside the cryptographic domain in which the key is found.
 * <p>
 * Each value repesents byte 11 of the Keyblok Header.
 * <p>
 */
public enum Exportability {

    /**
     * May only be exported in a trusted key block, provided the wrapping key
     * itself is in a trusted format.
     */
    ANY         ('E', "Exportable only in a trusted key block"),

    /**
     * No export permitted.
     */
    NONE        ('N', "No export permitted"),

    /**
     * May only be exported in a trusted key block, provided the wrapping key
     * itself is in a trusted format <b>only if allowed</b>.
     * <p>
     * Sensitive; all other export possibilities are permitted, provided such
     * export has been enabled <i>(existing Authorized State requirements
     * remain)</i>.
     */
    TRUSTED     ('S', "Exportable only in a trusted key block if allowed");


    private static final Map<Character, Exportability> MAP = new HashMap<>();

    static {
        for (Exportability exp : Exportability.values())
            MAP.put(exp.getCode(), exp);
    }

    private final char code;
    private final String name;

    Exportability(char code, String name) {
        Objects.requireNonNull(name, "The name of key exportability is required");
        this.code = code;
        this.name = name;
    }

    /**
     * Get exportability code.
     *
     * @return the character exportability code
     */
    public char getCode() {
        return code;
    }

    /**
     * Get exportability name.
     *
     * @return the exportability name
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("Exportability[code: %s, name: %s]", code, name);
    }

    /**
     * Returns the enum constant of this type with the specified {@code code}.
     *
     * @param code the string must match exactly with identifier specified by
     *        <i>ISO 8583-1:2003(E) Table A.22 — Transaction type codes</i>
     * @return the enum constant with the specified processing code or
     *         {@code null} if unknown.
     */
    public static Exportability valueOfByCode(char code) {
        return MAP.get(code);
    }

}

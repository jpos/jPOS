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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.jpos.iso.ISOUtil;

/**
 * Defines the primary usage of the key contained in the key block.
 * <p>
 * Each value repesents bytes 5-6 of the Keyblok Header.
 */
public class KeyUsage {

    protected static final Map<String, KeyUsage> TR31MAP =  new LinkedHashMap<>();

    /**
     * TR-31 BDK Base Derivation Key.
     */
    public final static KeyUsage BDK       = create("B0", "BDK - Base Derivation Key");

    /**
     * TR-31 DUKPT Initial Key (IKEY aka IPEK).
     */
    public final static KeyUsage IKEY      = create("B1", "IKEY - DUKPT Initial Key");

    /**
     * TR-31 CVK Card Verification Key.
     */
    public final static KeyUsage CVK       = create("C0", "CVK - Card Verification Key");

    /**
     * TR-31 Data Encryption Key.
     */
    public final static KeyUsage ENC       = create("D0", "Data Encryption Key");

    /**
     * TR-31 Initialization Value.
     * <p>
     * Used for protect eg. Initalization Vector or Decimalization Table.
     */
    public final static KeyUsage INIT      = create("I0", "Initialization Value");

    /**
     * TR-31 Generic Key Encryption / Wrapping Key.
     */
    public final static KeyUsage KEK       = create("K0", "Key Encryption / Wrapping Key");

    /**
     * TR-31 Key Block Protection Key.
     */
    public final static KeyUsage KEKWRAP   = create("K1", "Key Block Protection Key");

    /**
     * TR-31 ISO 16609 MAC algorithm 1 Key <i>(using 3-DES)</i>.
     */
    public final static KeyUsage ISOMAC0   = create("M0", "ISO 16609 MAC algorithm 1 Key");

    /**
     * TR-31 ISO 9797-1 MAC algorithm 1 Key.
     */
    public final static KeyUsage ISOMAC1   = create("M1", "ISO 9797-1 MAC algorithm 1 Key");

    /**
     * TR-31 ISO 9797-1 MAC algorithm 2 Key.
     */
    public final static KeyUsage ISOMAC2   = create("M2", "ISO 9797-1 MAC algorithm 2 Key");

    /**
     * TR-31 ISO 9797-1 MAC algorithm 3 Key.
     */
    public final static KeyUsage ISOMAC3   = create("M3", "ISO 9797-1 MAC algorithm 3 Key");

    /**
     * TR-31 ISO 9797-1 MAC algorithm 4 Key.
     */
    public final static KeyUsage ISOMAC4   = create("M4", "ISO 9797-1 MAC algorithm 4 Key");

    /**
     * TR-31 ISO 9797-1 MAC algorithm 5 Key.
     */
    public final static KeyUsage ISOMAC5   = create("M5", "ISO 9797-1 MAC algorithm 5 Key");

    /**
     * TR-31 Generic PIN Encription Key.
     */
    public final static KeyUsage PINENC    = create("P0", "PIN encryption key");

    /**
     * TR-31 Generic PIN Verification Key.
     */
    public final static KeyUsage PINVER    = create("V0", "PIN verification key or other algorithm");

    /**
     * TR-31 PIN Verification Key (IBM 3624 algorithm).
     */
    public final static KeyUsage PINV3624  = create("V1", "PIN verification key, IBM 3624 algorithm");

    /**
     * TR-31 PIN Verification Key (Visa PVV algorithm).
     */
    public final static KeyUsage VISAPVV   = create("V2", "PIN verification key, VISA PVV algorithm");

    /**
     * TR-31 Application Cryptograms Key.
     */
    public final static KeyUsage EMVACMK   = create("E0", "EMV/Chip card Master Key, MKAC - Application Cryptogram");

    /**
     * TR-31 Secure Messaging for Confidentiality Key.
     */
    public final static KeyUsage EMVSCMK   = create("E1", "EMV/Chip card Master Key, MKSMC - Secure Messaging for Confidentiality");

    /**
     * TR-31 Secure Messaging for Integrity.
     */
    public final static KeyUsage EMVSIMK   = create("E2", "EMV/Chip card Master Key, MKSMI - Secure Messaging for Integrity");

    /**
     * TR-31 Data Authentication Code Key.
     */
    public final static KeyUsage EMVDAMK   = create("E3", "EMV/Chip card Master Key, MKDAC - Data Authentication Code");

    /**
     * TR-31 Dynamic Numbers Key.
     */
    public final static KeyUsage EMVDNMK   = create("E4", "EMV/Chip card Master Key, MKDN - Dynamic Numbers");

    /**
     * TR-31 Card Personalization Key.
     */
    public final static KeyUsage EMVCPMK   = create("E5", "EMV/Chip card Master Key, Card Personalization");

    /**
     * TR-31 Chip card Master Key.
     */
    public final static KeyUsage EMVOTMK   = create("E6", "EMV/Chip card Master Key, Other");

    /**
     * TR-31 Master Personalization Key.
     */
    public final static KeyUsage EMVMPMK   = create("E7", "EMV/Master Personalization Key");


    private final String code;
    private final String name;

    /**
     * Internal constructor.
     * <p>
     * The constructor is protected to guarantee only one instance of the key
     * usage in the entire JVM. This makes it possible to use the operator
     * {@code ==} or {@code !=} as it does for enums.
     *
     * @param code the key usage code
     * @param name the usage name
     */
    protected KeyUsage(String code, String name) {
        Objects.requireNonNull(code, "The code of key usage is required");
        Objects.requireNonNull(name, "The name of key usage is required");
        if (code.length() != 2)  //The length of the code must be 2
            throw new IllegalArgumentException("The length of the key block Key Usage code must be 2");

        this.code = code;
        this.name = name;
    }

    private static KeyUsage create(String code, String name) {
        KeyUsage ku = new KeyUsage(code, name);
        if (ISOUtil.isNumeric(code, 10))
            throw new IllegalArgumentException(
                "The TR-31 Key Usage code can not consist of digits only"
            );
        if (TR31MAP.containsKey(ku.getCode()))
            throw new IllegalArgumentException(
                "The TR-31 Key Usage code can by registered only once"
            );
        TR31MAP.put(ku.getCode(), ku);
        return ku;
    }

    /**
     * Get key usage code.
     *
     * @return two characters which represents key usage code
     */
    public String getCode() {
        return code;
    }

    /**
     * Get key usage name.
     *
     * @return the key usage name
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("KeyUsage[code: %s, name: %s]", code, name);
    }

    /**
     * Returns the enum constant of this type with the specified {@code code}.
     *
     * @param code
     * @return the enum constant with the specified processing code or
     *         {@code null} if unknown.
     */
    public static KeyUsage valueOfByCode(String code) {
        return TR31MAP.get(code);
    }

    public static Map<String, KeyUsage> entries() {
        return Collections.unmodifiableMap(TR31MAP);
    }

}

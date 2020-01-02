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

package  org.jpos.security;

import java.util.Arrays;


/**
 * Util class contains some useful methods.
 * @author Hani S. Kirollos
 * @version $Revision$ $Date$
 */
public class Util {

    /**
     * DES Keys use the LSB as the odd parity bit.  This method can
     * be used enforce correct parity.
     *
     * @param bytes the byte array to set the odd parity on.
     */
    public static void adjustDESParity (byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i];
            bytes[i] = (byte)(b & 0xfe | (b >> 1 ^ b >> 2 ^ b >> 3 ^ b >> 4 ^ b >> 5 ^ b >> 6 ^ b >> 7 ^ 0x01) & 0x01);
        }
    }

    /**
     * DES Keys use the LSB as the odd parity bit.  This method checks
     * whether the parity is adjusted or not
     *
     * @param bytes the byte[] to be checked
     * @return true if parity is adjusted else returns false
     */
    public static boolean isDESParityAdjusted (byte[] bytes) {
        byte[] correct = bytes.clone();
        adjustDESParity(correct);
        return  Arrays.equals(bytes, correct);
    }
}




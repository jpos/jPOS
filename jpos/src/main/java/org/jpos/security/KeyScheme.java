/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2019 jPOS Software SRL
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
 * Key Encription Scheme.
 *
 * @author Robert Demski
 */
public enum KeyScheme {

    /**
     * Encryption of a single length DES key using X9.17 methods.
     * <p>
     * Used for encryption of keys under a variant LMK..
     */
    Z,

    /**
     * Encryption of a double length key using X9.17 methods.
     */
    X,

    /**
     * Encryption of a double length DES key using the variant method.
     * <p>
     * Used for encryption of keys under a variant LMK.
     */
    U,

    /**
     * Encryption of a triple length key using X9.17 methods.
     */
    Y,

    /**
     * Encryption of a triple length DES key using the variant method.
     * <p>
     * Used for encryption of keys under a variant LMK.
     */
    T
}


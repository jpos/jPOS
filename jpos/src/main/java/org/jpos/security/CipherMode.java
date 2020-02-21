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

/**
 * Block cipher modes.
 *
 * @author Grzegorz Wieczorek <grw1@wp.pl>
 */
public enum CipherMode {

    /**
     * Electronic Code Book.
     */
    ECB,

    /**
     * Cipher-block chaining.
     */
    CBC,

    /**
     * Cipher feedback, self-synchronizing with 8 bit shift register.
     */
    CFB8,

    /**
     * Cipher feedback, self-synchronizing with 64 bit shift register.
     */
    CFB64
}


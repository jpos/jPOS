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
 * Indicate method for the derivation by the issuer of a 16-byte
 * ICC Master Key used for Application Cryptogram generation,
 * issuer authentication, and secure messaging.
 * <br>
 * These methods take as input the PAN and PAN Sequence Number,
 * plus a 16-byte Issuer Master Key IMK,
 * and produce the 16-byte ICC Master Key MK
 * <br>
 * Described in EMV v4.2 Book 2, Annex A1.4 Master Key Derivation
 *
 * @author Robert Demski
 * @version $Revision$ $Date$
 */
public enum MKDMethod {

   /**
    * Uses PAN, PAN Sequence Number, IMK, Triple DES
    * Described in EMV v4.2 Book 2, Annex A1.4.1
    */
   OPTION_A

   /**
    * Uses PAN, PAN Sequence Number, IMK, Triple DES and SHA-1
    * and decimalisation of hex digits.
    * Described in EMV v4.2 Book 2, Annex A1.4.2
    * NOTE: For PAN with length less or equals 16 it works as {@code OPTION_A}
    */
  ,OPTION_B

}

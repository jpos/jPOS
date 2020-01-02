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
 * Indicate method for generation of the ARPC used for issuer authentication
 * <br>
 * Described in EMV v4.2 Book 2 section 8.2
 * @author Robert Demski
 * @version $Revision$ $Date$
 */
public enum ARPCMethod {

   /**
    * Method for the generation of an 8-byte ARPC consists of applying
    * the Triple-DES algorithm:
    * <li>the 8-byte ARQC
    * <li>the 2-byte Authorisation Response Code (ARC)
    */
   METHOD_1

   /**
    * Method For the generation of a 4-byte ARPC consists of applying
    * the MAC algorithm:
    * <li>the 4-byte ARQC
    * <li>the 4-byte binary Card Status Update (CSU)
    * <li>the 0-8 byte binary Proprietary Authentication Data
    */
  ,METHOD_2

}

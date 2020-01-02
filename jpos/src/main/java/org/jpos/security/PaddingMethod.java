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
 * Indicate padding method for encripting data (e.g. pin block) used by the issuer.
 * <br>
 *
 * @author Robert Demski
 * @version $Revision$ $Date$
 */
public enum PaddingMethod {

  /**
   * VISA padding
   * <p>
   * Prefix data with byte containing the length of that data and then
   * force ISO/IEC 9797-1 padding method 2 even if length of padded data
   * are multiply of eight.
   */
  VSDC,

  /**
   * Common Core Definitions padding.
   * <p>
   * Force ISO/IEC 9797-1 padding method 2 even if length of padded data
   * are multiply of eight.
   * In addition use the Cipher Block Chaining (CBC) Mode of Triple DES algotithm
   * Described in EMV v4.2 Book 2 - June 2008, section 9.3.3
   */
  CCD,

  /**
   * M/Chip 4 padding.
   * Used ISO/IEC 9797-1 padding method 2
   */
  MCHIP

}

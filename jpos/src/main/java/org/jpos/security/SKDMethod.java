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
 * Indicate method for derivation by the issuer of a 16-byte
 * Unique DEA Key UDK (Session Key) used for Application Cryptogram generation,
 * issuer authentication, and secure messaging.
 * <br>
 * These methods take as input the ATC (and unpredictable number),
 * plus a 16-byte ICC Master Key MK,
 * and produce the 16-byte Unique DEA Key UDK (Session Key)
 *
 * @author Robert Demski
 * @version $Revision$ $Date$
 */
public enum SKDMethod {

   /**
    * Visa Smart Debit/Credit or UKIS in England
    * <br>
    * Described in Visa Integrated Circuit Card
    * Specification (VIS) Version 1.5 - May 2009, section B.4
    */
   VSDC

   /**
    * MasterCard Proprietary SKD method
    */
  ,MCHIP
   /**
    * American Express
    */
  ,AEPIS_V40

   /**
    * EMV Common Session Key Derivation Method
    * Described in EMV v4.2 Book 2 - June 2008, Annex A1.3
    */
  ,EMV_CSKD

   /**
    * EMV2000 Session Key Method
    * Described in EMV 2000 v4.0 Book 2 - December 2000, Annex A1.3
    */
  ,EMV2000_SKM

}

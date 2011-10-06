/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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

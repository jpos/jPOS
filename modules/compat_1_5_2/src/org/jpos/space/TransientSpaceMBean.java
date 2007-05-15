/*
 * Generated file - Do not edit!
 */
package org.jpos.space;

/**
 * MBean interface.
 * @author Alejandro Revilla
 * @version $Revision: 2175 $ $Date: 2005-08-01 21:32:36 -0300 (Mon, 01 Aug 2005) $
 * @since 2.0
 */
public interface TransientSpaceMBean {

  java.util.Set getKeySet() ;

   /**
    * same as Space.out (key,value)
    * @param key Key
    * @param value value
    */
  void write(java.lang.String key,java.lang.String value) ;

   /**
    * same as (String) Space.rdp (key)
    * @param key Key
    * @return value.toString()
    */
  java.lang.String read(java.lang.String key) ;

}

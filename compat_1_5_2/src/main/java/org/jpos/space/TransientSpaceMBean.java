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

/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2018 jPOS Software SRL
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

package org.jpos.iso;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author apr
 * @version $Id$
 * @see ISOComponent
 */
public interface ISOPackager {
    /**
     * @param   m   the Component to pack
     * @return      Message image
     * @exception ISOException on error
     */
    byte[] pack(ISOComponent m) throws ISOException;

    /**
     * @param   m   the Container of this message
     * @param   b   ISO message image
     * @return      consumed bytes
     * @exception ISOException on error
     */
    int unpack(ISOComponent m, byte[] b) throws ISOException;

    void unpack(ISOComponent m, InputStream in) throws IOException, ISOException;

    /**
     * @return  Packager's Description
     */
    String getDescription();
    
    /**
     * @param   m   the Container (i.e. an ISOMsg)
     * @param   fldNumber the Field Number
     * @return  Field Description
     */
    String getFieldDescription(ISOComponent m, int fldNumber);

    /**
     * @return an ISOMsg
     */
    ISOMsg createISOMsg();
}


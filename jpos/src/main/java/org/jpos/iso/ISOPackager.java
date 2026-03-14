/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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
 * Core interface for ISO-8583 packagers; responsible for packing and unpacking {@link org.jpos.iso.ISOMsg} instances.
 * @author apr
 * @version $Id$
 * @see ISOComponent
 */
public interface ISOPackager {
    /**
     * Packs an ISO-8583 message into a byte array.
     * @param   m   the Component to pack
     * @return      Message image
     * @exception ISOException on packing error
     */
    byte[] pack(ISOComponent m) throws ISOException;

    /**
     * Unpacks an ISO-8583 byte array into the given message container.
     * @param   m   the Container of this message
     * @param   b   ISO message image
     * @return      consumed bytes
     * @exception ISOException on unpacking error
     */
    int unpack(ISOComponent m, byte[] b) throws ISOException;

    /**
     * Unpacks an ISO-8583 message from an input stream into the given container.
     * @param m the container
     * @param in the input stream
     * @throws IOException on I/O failure
     * @throws ISOException on unpacking error
     */
    void unpack(ISOComponent m, InputStream in) throws IOException, ISOException;

    /**
     * Returns a human-readable description of this packager.
     * @return  Packager's Description
     */
    String getDescription();
    
    /**
     * Emits a description of the field identified by {@code fldno} in the given message to the log event.
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


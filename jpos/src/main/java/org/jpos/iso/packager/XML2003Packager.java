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

package org.jpos.iso.packager;

import org.jpos.iso.*;


/**
 * packs/unpacks ISOMsgs into XML representation
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOPackager
 *
 */
public class XML2003Packager extends XMLPackager
{
    private final int[] BINARY_FIELDS  = new int[] { 72 };

    public XML2003Packager() throws ISOException {
        super();
        forceBinary(BINARY_FIELDS);

        // For backward compatibility with older implementation of XML2003Packager
        // we revert certain restrictions that XMLPackager sets
        try {
            setXMLParserFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
            setXMLParserFeature("http://xml.org/sax/features/external-general-entities", true);
            setXMLParserFeature("http://xml.org/sax/features/external-parameter-entities", true);
        } catch (Exception e) {
            throw new ISOException(e.toString(), e);
        }
    }
}

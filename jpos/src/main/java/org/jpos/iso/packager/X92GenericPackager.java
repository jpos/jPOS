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

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOFieldPackager;
import org.jpos.iso.X92_BITMAP;

import java.io.InputStream;

/**
 * @see GenericPackager
 * @author Alejandro Revilla
 * @version $$evision: $ $Date$
 */

public class X92GenericPackager extends GenericPackager {
    protected static ISOFieldPackager bitMapPackager = 
        new X92_BITMAP (16, "X9.2 BIT MAP");

    public X92GenericPackager() throws ISOException {
        super();
    }
    public X92GenericPackager(String filename) throws ISOException {
        super(filename);
    }
    public X92GenericPackager(InputStream stream) throws ISOException {
        super(stream);
    }
    /**
     * @return Bitmap's ISOFieldPackager
     */
    protected ISOFieldPackager getBitMapfieldPackager() {
        return bitMapPackager;
    }
    /**
     * Although field 1 is not a Bitmap ANSI X9.2 do have
     * a Bitmap field that have to be packed/unpacked
     * @see org.jpos.iso.ISOBasePackager
     * @return true
     */
    protected boolean emitBitMap () {
        return true;
    }
    /**
     * @return 64 for ANSI X9.2
     */
    protected int getMaxValidField() {
        return 64;
    }
}


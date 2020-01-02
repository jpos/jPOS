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

package org.jpos.iso.header;

import org.jpos.iso.ISOHeader;
import org.jpos.iso.ISOUtil;
import org.jpos.util.Loggeable;

import java.io.PrintStream;

/**
 * @author <a href="mailto:Eoin.Flood@orbiscom.com">Eoin Flood</a>
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 */
public class BaseHeader implements ISOHeader, Loggeable {
    /**
     * 
     */
    private static final long serialVersionUID = 8674535007934468935L;
    protected byte[] header;
    transient boolean asciiEncoding = false;

    /**
     * Default Constructor.
     * Used by Class.forName.newInstance(...);
     */
    public BaseHeader()
    {
        header = null;
    }

    public BaseHeader (byte[] header) {
        unpack(header);
    }

    public Object clone()
    {
        try {
            BaseHeader h = (BaseHeader) super.clone();
            if (this.header != null)
                h.header = this.header.clone();
            return h;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    public byte[] pack() {
        return header != null ? header.clone() : null;
    }

    public int unpack (byte[] header) {
        if (header != null) {
            this.header = header.clone();
            return header.length;
        } else {
            this.header = null;
        }
        return 0;
    }

    public int getLength () {
        return header != null ? header.length : 0;
    }

    public void setDestination(String dst) {}
    public void setSource(String src) {}
    public String getDestination() { return null; }
    public String getSource() { return null; }
    public void swapDirection() {}

    public void dump (PrintStream p, String indent) {
        if (header != null) {
            p.println (
                indent
              + "<header>" + ISOUtil.hexString (header) + "</header>"
            );
        }
    }
    public void setAsciiEncoding(boolean asciiEncoding) {
        this.asciiEncoding = asciiEncoding;
    }
    public boolean isAsciiEncoding() {
        return asciiEncoding;
    }
}

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

package org.jpos.tlv.packager;

import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOFieldPackager;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.TaggedFieldPackager;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Field Separator Terminated packager
 * @author Vishnu Pillai
 */
public class IF_FSTCHAR extends ISOFieldPackager implements TaggedFieldPackager {

    private char terminator = '\\';

    /** Default constructor. */
    public IF_FSTCHAR() {
        super();
    }

    @Override
    public void setToken(String token) {
        if (token == null || token.length() != 1 || !ISOUtil.CHARSET.newEncoder().canEncode(token)
          || token.getBytes(ISOUtil.CHARSET).length != 1) {
            throw new IllegalArgumentException("IF_FSTCHAR needs a token of 1 character.");
        }
        terminator = token.charAt(0);
    }

    @Override
    public String getToken() {
        return String.valueOf(terminator);
    }

    /**
     * Constructs a packager with the given length and description.
     * @param len         - field len
     * @param description symbolic descrption
     */
    public IF_FSTCHAR(int len, String description) {
        super(len, description);
    }

    /**
     * @param c - a component
     * @return packed component
     * @throws org.jpos.iso.ISOException on pack/unpack error
     */
    public byte[] pack(ISOComponent c) throws ISOException {
        byte[] value = String.valueOf(c.getValue()).getBytes(ISOUtil.CHARSET);
        byte token = terminatorByte();
        checkUnpackedLength(value.length);
        for (byte b : value) {
            if (b == token)
                throw new ISOException("Field value contains terminator");
        }
        byte[] packed = new byte[value.length + 1];
        System.arraycopy(value, 0, packed, 0, value.length);
        packed[value.length] = token;
        return packed;
    }

    /**
     * @param c      - the Component to unpack
     * @param b      - binary image
     * @param offset - starting offset within the binary image
     * @return consumed bytes
     * @throws org.jpos.iso.ISOException on pack/unpack error
     */
    public int unpack(ISOComponent c, byte[] b, int offset)
            throws ISOException {
        if (!(c instanceof ISOField))
            throw new ISOException
                    (c.getClass().getName() + " is not an ISOField");
        byte token = terminatorByte();
        checkUnpackedLength(0);
        checkAvailable(b, offset, 0, "field separator terminated field");
        int available = b.length - offset;
        int maximum = Math.min(getLength(), available - 1);
        for (int length = 0; length <= maximum; length++) {
            if (b[offset + length] == token) {
                c.setValue(new String(b, offset, length, ISOUtil.CHARSET));
                return length + 1;
            }
        }
        throw new ISOException("Field terminator not found within maximum length " + getLength());
    }

    public void unpack(ISOComponent c, InputStream in)
            throws IOException, ISOException {

        if (!(c instanceof ISOField))
            throw new ISOException
                    (c.getClass().getName() + " is not an ISOField");

        byte token = terminatorByte();
        checkUnpackedLength(0);
        ByteArrayOutputStream value = new ByteArrayOutputStream(Math.min(getLength(), 32));
        for (long length = 0; length <= getLength(); length++) {
            int next = in.read();
            if (next < 0)
                throw new EOFException("Field terminator not found before end of stream");
            if ((byte) next == token) {
                c.setValue(value.toString(ISOUtil.CHARSET));
                return;
            }
            value.write(next);
        }
        throw new ISOException("Field terminator not found within maximum length " + getLength());
    }

    private byte terminatorByte() {
        return String.valueOf(terminator).getBytes(ISOUtil.CHARSET)[0];
    }

    public int getMaxPackedLength() {
        return getLength() < 0 ? 0 : (int) Math.min((long) getLength() + 1, Integer.MAX_VALUE);
    }
}

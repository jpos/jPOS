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

package org.jpos.tlv.packager;

import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOFieldPackager;
import org.jpos.iso.TaggedFieldPackager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Field Separator Terminated packager
 * @author Vishnu Pillai
 */
public class IF_FSTCHAR extends ISOFieldPackager implements TaggedFieldPackager {

    private char terminator = '\\';

    public IF_FSTCHAR() {
        super();
    }

    @Override
    public void setToken(String token) {
        if (token == null || token.length() != 1) {
            throw new IllegalArgumentException("IF_FSTCHAR needs a token of 1 character.");
        }
        terminator = token.charAt(0);
    }

    @Override
    public String getToken() {
        return String.valueOf(terminator);
    }

    /**
     * @param len         - field len
     * @param description symbolic descrption
     */
    public IF_FSTCHAR(int len, String description) {
        super(len, description);
    }

    /**
     * @param c - a component
     * @return packed component
     * @throws org.jpos.iso.ISOException
     */
    public byte[] pack(ISOComponent c) throws ISOException {
        int len;
        String s = (String) c.getValue();

        if ((len = s.length()) > getLength()) {
            throw new ISOException(
                    "Invalid length " + len + " packing IF_FSTCHAR field "
                            + c.getKey() + " max length=" + getLength()
            );
        }

        s = s + terminator;
        return s.getBytes();
    }

    /**
     * @param c      - the Component to unpack
     * @param b      - binary image
     * @param offset - starting offset within the binary image
     * @return consumed bytes
     * @throws org.jpos.iso.ISOException
     */
    public int unpack(ISOComponent c, byte[] b, int offset)
            throws ISOException {
        if (!(c instanceof ISOField))
            throw new ISOException
                    (c.getClass().getName() + " is not an ISOField");
        int length = -1;
        for (int i = 0; i < getMaxPackedLength(); i++) {
            byte dataByte = b[offset + i];
            if ((char) dataByte == terminator) {
                length = i;
                break;
            }
        }
        if (length >= 0) {
            String value = new String(b, offset, length);
            c.setValue(value);
            return length + 1;
        } else {
            throw new ISOException("Terminating Backslash does not exist");
        }
    }

    public void unpack(ISOComponent c, InputStream in)
            throws IOException, ISOException {

        if (!(c instanceof ISOField))
            throw new ISOException
                    (c.getClass().getName() + " is not an ISOField");

        boolean endFound = false;
        if (in.markSupported()) {
            in.mark(getMaxPackedLength());
        }
        ByteBuffer buf = ByteBuffer.allocate(getMaxPackedLength());

        for (int i = 0; i < getMaxPackedLength() && in.available() > 0; i++) {
            byte dataByte = (byte) in.read();
            if ((char) dataByte == terminator) {
                endFound = true;
                break;
            } else {
                buf.put(dataByte);
            }
        }
        if (endFound) {
            byte[] data = byteBufferToBytes(buf);
            String value = new String(data);
            c.setValue(value);
        } else {
            if (in.markSupported()) {
                in.reset();
            }
            throw new ISOException("Terminating Backslash does not exist");
        }
    }

    private byte[] byteBufferToBytes(ByteBuffer buffer) {
        int dataLength = buffer.position();
        byte[] bytes = new byte[dataLength];
        buffer.position(0);
        buffer.get(bytes);
        buffer.position(dataLength);
        return bytes;
    }

    public int getMaxPackedLength() {
        return getLength() + 1;
    }
}


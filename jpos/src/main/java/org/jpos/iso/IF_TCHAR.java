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

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * ISOFieldPackager CHARACTERS (ASCII and BINARY)
 * deal fields terminated by special token
 * @author Zhiyu Tang
 * @version $Id$
 * @see ISOComponent
 */
public class IF_TCHAR extends IF_TBASE {
    /** Default constructor. */
    public IF_TCHAR() {
        super();
    }
    /**
     * Constructs a packager with the given length and description.
     * @param len - field len
     * @param description symbolic descrption
     */
    public IF_TCHAR(int len, String description) {
        super(len, description);
    }
    /**
     * Constructs a packager with the given length and description.
     * @param len - field len
     * @param description symbolic descrption
     * @param token token descrption
     */
    public IF_TCHAR(int len, String description, String token) {
        super(len, description, token);
    }

    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException on ISO processing error
     */
    @Override
    public byte[] pack (ISOComponent c) throws ISOException {
        byte[] token = tokenBytes();
        byte[] value = String.valueOf(c.getValue()).getBytes(ISOUtil.CHARSET);
        checkUnpackedLength(value.length);
        for (int offset = 0; offset <= value.length - token.length; offset++) {
            if (matches(value, offset, value.length - offset, token))
                throw new ISOException("Field value contains terminator");
        }
        byte[] packed;
        try {
            packed = new byte[Math.addExact(value.length, token.length)];
        } catch (ArithmeticException e) {
            throw new ISOException("Packed field length overflow", e);
        }
        System.arraycopy(value, 0, packed, 0, value.length);
        System.arraycopy(token, 0, packed, value.length, token.length);
        return packed;
    }
    /**
     * @param c - the Component to unpack
     * @param b - binary image
     * @param offset - starting offset within the binary image
     * @return consumed bytes
     * @exception ISOException on ISO processing error
     */
    @Override
    public int unpack (ISOComponent c, byte[] b, int offset)
        throws ISOException
    {
        byte[] token = tokenBytes();
        checkUnpackedLength(0);
        checkAvailable(b, offset, 0, "terminated field");
        int available = b.length - offset;
        int maxLength = Math.min(getLength(), available);
        for (int length = 0; length <= maxLength; length++) {
            if (matches(b, offset + length, available - length, token)) {
                c.setValue(new String(b, offset, length, ISOUtil.CHARSET));
                return length + token.length;
            }
        }
        throw new ISOException("Field terminator not found within maximum length " + getLength());
    }

    @Override
    public void unpack(ISOComponent c, InputStream in) throws IOException, ISOException {
        byte[] token = tokenBytes();
        checkUnpackedLength(0);
        ByteArrayOutputStream value = new ByteArrayOutputStream(Math.min(getLength(), 32));
        long maximumPackedLength = (long) getLength() + token.length;
        int[] failure = failureTable(token);
        int matched = 0;
        long consumed = 0;

        while (consumed < maximumPackedLength) {
            int next = in.read();
            if (next < 0)
                throw new EOFException("Field terminator not found before end of stream");
            value.write(next);
            consumed++;
            while (matched > 0 && (byte) next != token[matched])
                matched = failure[matched - 1];
            if ((byte) next == token[matched])
                matched++;
            if (matched == token.length) {
                byte[] bytes = value.toByteArray();
                int length = bytes.length - token.length;
                c.setValue(new String(bytes, 0, length, ISOUtil.CHARSET));
                return;
            }
        }
        throw new ISOException("Field terminator not found within maximum length " + getLength());
    }

    @Override
    public int getMaxPackedLength() {
        return getLength() + getToken().length();
    }

    private byte[] tokenBytes() throws ISOException {
        String token = getToken();
        if (token == null || token.isEmpty())
            throw new ISOException("Field terminator is not configured");
        return token.getBytes(ISOUtil.CHARSET);
    }

    private static boolean matches(byte[] b, int offset, int available, byte[] token) {
        if (available < token.length)
            return false;
        for (int i = 0; i < token.length; i++) {
            if (b[offset + i] != token[i])
                return false;
        }
        return true;
    }

    private static int[] failureTable(byte[] token) {
        int[] failure = new int[token.length];
        for (int i = 1, matched = 0; i < token.length; i++) {
            while (matched > 0 && token[i] != token[matched])
                matched = failure[matched - 1];
            if (token[i] == token[matched])
                failure[i] = ++matched;
        }
        return failure;
    }
}

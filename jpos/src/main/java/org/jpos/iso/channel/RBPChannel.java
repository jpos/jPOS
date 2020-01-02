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

package org.jpos.iso.channel;

import org.jpos.iso.BaseChannel;
import org.jpos.iso.ISOException;

import java.io.IOException;

/**
 * Implements Record Boundary Preservation protocol
 */
public class RBPChannel extends BaseChannel {
    static final byte[] PROTOCOL_IDENTIFIER = new byte[] {(byte) 0xd0, 0x4a };
    static final byte[] MORE = new byte[] {(byte) 0x01, 0x00 };
    static final byte[] LAST = new byte[] {(byte) 0x00, 0x00 };
    protected void sendMessageLength(int len) throws IOException {
        serverOut.write (PROTOCOL_IDENTIFIER);
        serverOut.write (len >> 8);
        serverOut.write (len);
        serverOut.write (LAST);
    }
    protected int getMessageLength() throws IOException, ISOException {
        byte[] b = new byte[6];
        serverIn.readFully(b,0,6);
        return ((int)b[2] &0xFF) << 8 | (int)b[3] &0xFF;
    }
}

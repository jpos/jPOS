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

package org.jpos.iso.channel;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TelnetXMLChannelTest {
    @Test
    public void testStreamReceiveAllowsExactMaxPacketLength() throws Exception {
        TelnetXMLChannel channel = new TelnetXMLChannel();
        String message = "<isomsg></isomsg>";
        channel.setMaxPacketLength(message.getBytes().length);
        channel.reader = new BufferedReader(new StringReader("<isomsg>\n</isomsg>\n"));

        assertEquals(message, new String(channel.streamReceive()));
    }

    @Test
    public void testStreamReceiveRejectsMessageOverMaxPacketLength() {
        TelnetXMLChannel channel = new TelnetXMLChannel();
        String message = "<isomsg></isomsg>";
        channel.setMaxPacketLength(message.getBytes().length - 1);
        channel.reader = new BufferedReader(new StringReader("<isomsg>\n</isomsg>\n"));

        assertThrows(IOException.class, channel::streamReceive);
    }

    @Test
    public void testStreamReceiveRejectsOverlongLine() {
        TelnetXMLChannel channel = new TelnetXMLChannel();
        channel.setMaxPacketLength(16);
        channel.reader = new BufferedReader(new StringReader("<isomsg>" + "x".repeat(9)));

        assertThrows(IOException.class, channel::streamReceive);
    }
}

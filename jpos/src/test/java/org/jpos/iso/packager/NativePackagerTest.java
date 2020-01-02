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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.jpos.iso.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NativePackagerTest {
    ISOMsg m;
    ISOPackager p;
    static final byte[] PACKED = ISOUtil.hex2byte("ACED0005778100FFFF460000000430383030460003000630303030303046000B000630303030303146002900083239313130303031420037000455AA11224D00007F460002000A54657374203132372E32460003000A54657374203132372E33460004000A54657374203132372E344D000005460001000C54657374203132372E352E31454545");

    @BeforeEach
    public void setup() throws ISOException, NoSuchFieldException {
        p = new NativePackager();
        m = new ISOMsg();
        m.set(0, "0800");
        m.set(3, "000000");
        m.set(11, "000001");
        m.set(41, "29110001");
        m.set(55, ISOUtil.hex2byte("55AA1122"));
        m.set("127.2", "Test 127.2");
        m.set("127.3", "Test 127.3");
        m.set("127.4", "Test 127.4");
        m.set("127.5.1", "Test 127.5.1");
        m.setPackager(p);
    }

    @Test
    public void testPack() throws ISOException {
        assertArrayEquals(PACKED, m.pack(), "Expected image");
    }

    @Test
    public void testUnpack() throws ISOException {
        ISOMsg m1 = new ISOMsg();
        m1.setPackager(p);
        m1.unpack(PACKED);
        assertEquals("0800", m1.getMTI());
        assertEquals("000000", m1.getString(3));
        assertEquals("000001", m1.getString(11));
        assertEquals("29110001", m1.getString(41));
        assertArrayEquals(ISOUtil.hex2byte("55AA1122"), m1.getBytes(55));
        assertEquals("Test 127.2", m1.getString("127.2"));
        assertEquals("Test 127.3", m1.getString("127.3"));
        assertEquals("Test 127.4", m1.getString("127.4"));
        assertEquals("Test 127.5.1", m1.getString("127.5.1"));
    }
}


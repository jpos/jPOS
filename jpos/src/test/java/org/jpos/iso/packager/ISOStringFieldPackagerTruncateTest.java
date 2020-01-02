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

public class ISOStringFieldPackagerTruncateTest {
    private ISOPackager p;

    @BeforeEach
    public void setup() throws ISOException {
        p = new GenericPackager("jar:packager/field59-truncate.xml");;
    }

    @Test
    public void test6() throws ISOException {
        byte[] expected =  ISOUtil.hex2byte("080000000000000000200BF0F1F0F0F2F1F2F3F4F5F6");
        ISOMsg m = new ISOMsg ("0800");
        m.set("59.1", "01");
        m.set("59.2", "002");
        m.set("59.3", "123456");
        m.setPackager(p);

        assertArrayEquals(expected, m.pack());
        ISOMsg m1 = new ISOMsg();
        m1.setPackager(p);
        m1.unpack(expected);
        assertEquals(m.getString("59.3"), m1.getString("59.3"));
        assertArrayEquals(m.pack(), m1.pack());
    }

    @Test
    public void test9() throws ISOException {
        byte[] expected =  ISOUtil.hex2byte("080000000000000000200EF0F1F0F0F2F1F2F3F4F5F6F7F8F9");
        ISOMsg m = new ISOMsg ("0800");
        m.set("59.1", "01");
        m.set("59.2", "002");
        m.set("59.3", "123456789");
        m.setPackager(p);

        assertArrayEquals(expected, m.pack());
        ISOMsg m1 = new ISOMsg();
        m1.setPackager(p);
        m1.unpack(expected);
        assertEquals(m.getString("59.3"), m1.getString("59.3"));
        assertArrayEquals(m.pack(), m1.pack());
    }
}

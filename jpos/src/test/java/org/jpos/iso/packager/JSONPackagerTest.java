/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2015 Alejandro P. Revilla
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

import static org.junit.Assert.assertEquals;

import org.jpos.iso.*;
import org.junit.Before;
import org.junit.Test;

public class JSONPackagerTest {
    ISOMsg m;
    JSONPackager p;
    public static final String SAMPLE_JSON = "{\"0\":\"0800\",\"3\":\"000000\",\"11\":\"000001\",\"41\":\"29110001\",\"55b\":\"55AA1122\",\"127.2\":\"Test 127.2\",\"127.3\":\"Test 127.3\",\"127.4\":\"Test 127.4\",\"127.5.1\":\"Test 127.5.1\"}";

    @Before
    public void setup() throws ISOException, NoSuchFieldException {
        p = new JSONPackager();
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
        assertEquals("Expected JSON", SAMPLE_JSON, new String(m.pack()));
    }

    @Test
    public void testUnpack() throws ISOException {
        ISOMsg m1 = new ISOMsg();
        m1.setPackager(p);
        m1.unpack(SAMPLE_JSON.getBytes());
        assertEquals("Expected JSON", new String(m.pack()), new String(m1.pack()));
    }
}

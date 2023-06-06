/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2023 jPOS Software SRL
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

package org.jpos.security;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.jpos.iso.ISOUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class KeySerialNumberTest {


    @Test
    public void testDump() throws Throwable {
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true, "UTF-8");
        Object[] objects = new Object[1];
        p.format("testKeySerialNumberParam1", objects);
        new KeySerialNumber("FFFF987654", "3210E", "000008")
                .dump(p, "testKeySerialNumberIndent");
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testBinaryConstructor() {
        byte[] ksnBin = ISOUtil.hex2byte("9876543210E00008");
        KeySerialNumber ksn = new KeySerialNumber(ksnBin);
        assertEquals("FFFF987654", ksn.getBaseKeyID());
        assertEquals("03210E", ksn.getDeviceID());
        assertEquals("000008", ksn.getTransactionCounter());
    }

    @Test
    public void testHexConstructorWrongLength() {
        assertThrows(IllegalArgumentException.class, () -> {
            new KeySerialNumber(ISOUtil.hex2byte("9876543210E008"));
        });
    }

    @Test
    public void testHexConstructorNullKSN() {
        assertThrows(NullPointerException.class, () -> {
            new KeySerialNumber(null);
        });
    }
}

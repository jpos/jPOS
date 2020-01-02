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

package org.jpos.tlv;

import org.jpos.iso.ISOUtil;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TLVMsgTest {

    static final int TEST_TAG1      = 0x64;
    static final int TEST_TAG3      = 0x1fe8;

    TLVMsg msg;
    TLVList tl;

    @BeforeEach
    public void setUp() {
        tl = new TLVList();
    }

    @Test
    public void testGetL() {
        byte[] value = new byte[3];
        byte[] result = tl.createTLVMsg(TEST_TAG1, value).getL();
        assertArrayEquals(ISOUtil.hex2byte("03"), result);
    }

    @Test
    public void testGetL1() {
        byte[] value = new byte[1];
        byte[] result = tl.createTLVMsg(TEST_TAG1, value).getL();
        assertArrayEquals(ISOUtil.hex2byte("01"), result);
    }

    @Test
    public void testGetL2() {
        byte[] result = tl.createTLVMsg(TEST_TAG1, null).getL();
        assertArrayEquals(ISOUtil.hex2byte("00"), result);
    }

    @Test
    public void testGetL3() {
        byte[] value = new byte[200];
        byte[] result = tl.createTLVMsg(TEST_TAG1, value).getL();
        assertArrayEquals(ISOUtil.hex2byte("81C8"), result);
    }

    @Test
    public void testGetL4() {
        byte[] value = new byte[0x7ff7];
        byte[] result = tl.createTLVMsg(TEST_TAG1, value).getL();
        assertArrayEquals(ISOUtil.hex2byte("827FF7"), result);
    }

    @Test
    public void testGetL5() {
        byte[] value = new byte[0x8ff8];
        byte[] result = tl.createTLVMsg(TEST_TAG1, value).getL();
        assertArrayEquals(ISOUtil.hex2byte("828FF8"), result);
    }

    @Test
    public void testGetL6() {
        byte[] value = new byte[0];
        byte[] result = tl.createTLVMsg(TEST_TAG1, value).getL();
        assertArrayEquals(ISOUtil.hex2byte("00"), result);
    }

    @Test
    public void testGetTLV() {
        byte[] value = new byte[1];
        byte[] result = tl.createTLVMsg(TEST_TAG1, value).getTLV();
        assertArrayEquals(ISOUtil.hex2byte("640100"), result);
    }

    @Test
    public void testGetTLV1() {
        byte[] result = tl.createTLVMsg(TEST_TAG1, null).getTLV();
        assertArrayEquals(ISOUtil.hex2byte("6400"), result);
    }

    @Test
    public void testGetTLVEmptyValue1() {
        byte[] value = new byte[0];
        byte[] result = tl.createTLVMsg(TEST_TAG1, value).getTLV();
        assertArrayEquals(ISOUtil.hex2byte("6400"), result);
    }

    @Test
    public void testGetTLVEmptyValue2() {
        byte[] value = new byte[0];
        byte[] result = tl.createTLVMsg(TEST_TAG3, value).getTLV();
        assertArrayEquals(ISOUtil.hex2byte("1FE800"), result);
    }

    @Test
    public void testGetStringValue() {
        msg = tl.createTLVMsg(23, "987612".getBytes());
        String result = msg.getStringValue();
        assertEquals("393837363132", result);
    }

    @Test
    public void testLowTagID() {
        msg = tl.createTLVMsg(8, "987612".getBytes());
        String result = msg.getStringValue();
        assertEquals("393837363132", result);
        byte[] b = msg.getTLV();
        assertArrayEquals(ISOUtil.hex2byte("0806393837363132"), b);
    }

}

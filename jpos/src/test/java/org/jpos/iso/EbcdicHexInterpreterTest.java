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

package org.jpos.iso;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EbcdicHexInterpreterTest {
    EbcdicHexInterpreter ebcdicHexInterpreter;
    byte[] hexEbcdicData;
    byte[] asciiData;

    @BeforeEach
    public void setUp() throws Exception {
        ebcdicHexInterpreter = EbcdicHexInterpreter.INSTANCE;
        asciiData = "01234567890ABCDEF".getBytes();
        hexEbcdicData = ISOUtil.hex2byte("F3F0F3F1F3F2F3F3F3F4F3F5F3F6F3F7F3F8F3F9F3F0F4F1F4F2F4F3F4F4F4F5F4F6");
    }

    @Test
    public void testInterpret() {
        byte[] result = new byte[asciiData.length * 2];
        ebcdicHexInterpreter.interpret(asciiData, result, 0);
        assertThat(result, is(hexEbcdicData));
    }

    @Test
    public void testUninterpret() {
        int offset = 0;
        byte[] result = ebcdicHexInterpreter.uninterpret(hexEbcdicData, offset, hexEbcdicData.length >> 1);
        assertThat(result, is(asciiData));
    }
}

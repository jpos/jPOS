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

import org.junit.jupiter.api.Test;

public class EbcdicBinaryInterpreterTest {
    public static final BinaryInterpreter interpreter = EbcdicBinaryInterpreter.INSTANCE;
    public static final  byte[] EBCDICDATA = ISOUtil.hex2byte(
              "F1F2F3F4F5F6F7F8F9F0C1C2C3C4C5C6C7C8C9D1D2D3D4D5D6D7D8D9E2E3E4E5E6E7E8E9818283848586878"
             +"889919293949596979899A2A3A4A5A6A7A8A95FC0D0ADBD7F7E7D7C7A4F6B6C6D4C6E6F5A5C50");
    final static byte[] binaryData = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz^{}[]\"='@:|,%_<>?!*&".getBytes();

    @Test
    public void interpret() {
        byte[] result = new byte[binaryData.length];
        interpreter.interpret(binaryData, result, 0);
        assertThat(result, is(EBCDICDATA));
    }

    @Test
    public void uninterpret() {
        int offset = 0;
        int length = EBCDICDATA.length;
        byte[] result = interpreter.uninterpret(EBCDICDATA, offset, length);
        assertThat(result, is(binaryData));
    }
}

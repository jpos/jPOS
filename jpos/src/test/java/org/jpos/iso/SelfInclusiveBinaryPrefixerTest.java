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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SelfInclusiveBinaryPrefixerTest {
    @Test
    void encodesAndDecodesOneByteLengths() throws Exception {
        byte[] prefix = new byte[1];
        SelfInclusiveBinaryPrefixer.B.encodeLength(10, prefix);

        assertEquals(11, prefix[0] & 0xFF);
        assertEquals(10, SelfInclusiveBinaryPrefixer.B.decodeLength(prefix, 0));
    }

    @Test
    void encodesAndDecodesTwoByteLengths() throws Exception {
        byte[] prefix = new byte[2];
        SelfInclusiveBinaryPrefixer.BB.encodeLength(10, prefix);

        assertEquals(12, (prefix[0] & 0xFF) * 256 + (prefix[1] & 0xFF));
        assertEquals(10, SelfInclusiveBinaryPrefixer.BB.decodeLength(prefix, 0));
    }
}

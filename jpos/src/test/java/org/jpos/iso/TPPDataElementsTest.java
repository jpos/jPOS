/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2025 jPOS Software SRL
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

import org.jpos.iso.packager.GenericPackager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TPPDataElementsTest {
    private byte[] TPPDE =
        ISOUtil.hex2byte(
            ISOUtil.hexString(("tpp-card-token                          ").getBytes()) // Card Token
            + "00000000000000022220" // Transaction ID
            + "00000000000000011110" // Transaction GroupID
        );

    @Test
    public void testTPPDataElements() throws ISOException {
        ISOPackager p = new GenericPackager("jar:packager/cmf.xml");
        ISOMsg m = new ISOMsg("2100");
        m.set("113.69", "tpp-card-token");
        m.set("113.70", "2222");
        m.set("113.71", "1111");
        m.setPackager(p);

        assertEquals(ISOUtil.hexString(TPPDE), ISOUtil.hexString(m.pack()).substring(72));

        assertEquals(m.getString("113.69"), "tpp-card-token");
        assertEquals(m.getString("113.70"), "2222");
        assertEquals(m.getString("113.71"), "1111");
    }
}

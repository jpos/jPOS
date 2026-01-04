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

import static org.junit.jupiter.api.Assertions.*;

import org.jpos.iso.packager.GenericPackager;
import org.junit.jupiter.api.Test;

public class PosCapabilityTest {
    private byte[] POSCAP =
     ISOUtil.hex2byte(
          "FFFFFFFF"  // Reading Capabilities
         +"FFFFFFFF"  // Verification Capabilities
         +ISOUtil.hexString(
         (
           "6"        // Approval Code Length
          +"040"      // Cardholder receipt data length
          +"080"      // Card acceptor receipt data length
          +"016"      // Cardholder display data length
          +"032"      // Card acceptor display data length
          +"003"      // ICC scripts data length
          +"N"        // Magnetic stripe track 3 rewrite capability
          +"Y"        // Card capture capability
          +"6"        // PIN Input length
          ).getBytes()
         )
     );

    @Test
    public void testPosCapabiiltyCreation() throws ISOException {
        ISOPackager p = new GenericPackager("jar:packager/cmf.xml");
        ISOMsg m = new ISOMsg("2800");
        m.set("27.0", ISOUtil.hex2byte("FFFFFFFFFFFFFFFF"));
        m.set("27.1", "6");
        m.set("27.2", "040");
        m.set("27.3", "080");
        m.set("27.4", "016");
        m.set("27.5", "032");
        m.set("27.6", "003");
        m.set("27.7", "N");
        m.set("27.8", "Y");
        m.set("27.9", "6");
        m.setPackager(p);
        assertEquals(ISOUtil.hexString(POSCAP), ISOUtil.hexString(m.pack()).substring(20));
        PosCapability pc = PosCapability.valueOf(m.getBytes("27.0"));
        for (PosCapability.ReadingCapability rc : PosCapability.ReadingCapability.values()) {
            assertTrue (pc.hasReadingCapability(rc));
        }
        for (PosCapability.VerificationCapability vc : PosCapability.VerificationCapability.values()) {
            assertTrue (pc.hasVerificationCapability(vc));
        }
        pc = PosCapability.valueOf(ISOUtil.hex2byte("0000000000000000"));
        for (PosCapability.ReadingCapability rc : PosCapability.ReadingCapability.values()) {
            assertFalse (pc.hasReadingCapability(rc));
        }
        for (PosCapability.VerificationCapability vc : PosCapability.VerificationCapability.values()) {
            assertFalse (pc.hasVerificationCapability(vc));
        }
    }
}

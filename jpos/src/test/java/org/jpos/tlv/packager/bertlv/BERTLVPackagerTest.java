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

package org.jpos.tlv.packager.bertlv;

import org.jpos.emv.EMVStandardTagType;
import org.jpos.iso.*;
import org.jpos.tlv.ISOTaggedField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BERTLVPackagerTest {

    @Test
    public void testZeroLength() throws ISOException {
        BERTLVPackager p = new BERTLVAsciiHexPackager();
        ISOTaggedField t = new ISOTaggedField(EMVStandardTagType.AMOUNT_AUTHORISED_NUMERIC_0x9F02.getTagNumberHex(),
          new ISOField(0, ""));
        t.setFieldNumber(1);

        ISOMsg m = new ISOMsg(55);
        m.set(t);
        byte[] b = p.pack(m, true, 1, 1);
        assertArrayEquals(b, ISOUtil.hex2byte("394630323030"));
    }

    @Test
    public void testUnpackingZeroLength() {
        try {
            BERTLVPackager p = new BERTLVBinaryPackager();
            p.setFieldPackager(new ISOFieldPackager[]{new IFA_TTLLBINARY()});

            ISOMsg m = new ISOMsg(55);
            p.unpack(m, ISOUtil.hex2byte("9F3400"));
        } catch (ISOException e) {
            fail("Unexpected java.lang.ArithmeticException: divide by zero", e);
        }
    }
}

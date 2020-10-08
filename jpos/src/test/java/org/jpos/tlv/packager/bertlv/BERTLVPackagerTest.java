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

import org.jpos.emv.BinaryEMVTag;
import org.jpos.emv.EMVStandardTagType;
import org.jpos.emv.EMVTagSequence;
import org.jpos.emv.LiteralEMVTag;
import org.jpos.iso.*;
import org.jpos.tlv.ISOTaggedField;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

public class BERTLVPackagerTest {

    @Test
    public void testZeroLength() throws ISOException {
        // APPLICATION_FILE_LOCATOR_0x94 supports zero length
        BERTLVPackager p = new BERTLVAsciiHexPackager();
        ISOTaggedField t = new ISOTaggedField(
          EMVStandardTagType.APPLICATION_FILE_LOCATOR_0x94.getTagNumberHex(),
          new ISOField(0, ""));
        t.setFieldNumber(1);

        ISOMsg m = new ISOMsg(55);
        m.set(t);
        byte[] b = p.pack(m, true, 1, 1);
        assertArrayEquals(ISOUtil.hex2byte("39343030"), b, ISOUtil.hexString(b));
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

    @Test
    public void testUnpackingDate() {
        try {
            BERTLVPackager p = new BERTLVBinaryPackager();
            p.setFieldPackager(new ISOFieldPackager[]{new IFA_TTLLBINARY()});

            ISOMsg m = new ISOMsg(55);
            p.unpack(m, ISOUtil.hex2byte("9A03020618"));

            assertEquals("020618", m.getComponent("1").getValue());
        } catch (ISOException e) {
            fail("Unexpected exception", e);
        }
    }

    @Test
    public void bug349() throws ISOException {
        ISOMsg msg = new ISOMsg("0600");

        msg.set(11, Integer.toString(123));
        msg.set(12, new SimpleDateFormat("HHmmss").format(Calendar.getInstance().getTime()));
        msg.set(13, new SimpleDateFormat("MMYY").format(Calendar.getInstance().getTime()));

        ISOMsg field55 = new ISOMsg(55);
        EMVTagSequence sequence = new EMVTagSequence();
        sequence.add(new BinaryEMVTag(Bug349TagType.BMP55_SF14, Bug349TagType.BMP55_SF14.getTagNumber(), new byte[] {0x01, 0x02, 0x03}));
        sequence.add(new LiteralEMVTag(Bug349TagType.BMP55_SF99, Bug349TagType.BMP55_SF99.getTagNumber(), Integer.toString(0)));
        sequence.writeTo(field55);
        msg.set(field55); // ICC data

        Bug349BinaryPackager packager = new Bug349BinaryPackager();
        Logger logger = new Logger();
        logger.addListener(new SimpleLogListener(System.err));
        packager.setLogger(logger, "bug349");
        msg.setPackager(packager);

        byte[] out = msg.pack();
        System.out.println("bin msg: " + out);
    }
}


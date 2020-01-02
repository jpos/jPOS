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

package org.jpos.tlv.packager;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.tlv.GenericTagSequence;
import org.jpos.tlv.ISOMsgRef;
import org.jpos.tlv.LiteralTagValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @author Vishnu Pillai
 *         Date: 1/21/14
 */
public class ISOTaggedSequenceTest {

    @Test
    public void testPacking() throws ISOException, FileNotFoundException {

        ISOMsg msg = new ISOMsg("0100");

        GenericTagSequence tagValueSequence = new GenericTagSequence();

        tagValueSequence.add(new LiteralTagValue("0012", "19960930000000"));
        tagValueSequence.add(new LiteralTagValue("0165", "M"));
        tagValueSequence.add(new LiteralTagValue("0023", "CT2"));

        ISOMsg field48 = new ISOMsg(48);

        tagValueSequence.writeTo(field48);

        msg.set(field48);

        msg.recalcBitMap();

        GenericPackager packager = new GenericPackager(new FileInputStream("build/resources/test/org/jpos/tlv/tagged-sequence-packager.xml"));
        msg.setPackager(packager);

        byte[] packed = packager.pack(msg);

        //skip 4 byte MTI and 8 byte Primary BitMap
        byte[] field48Packed = new byte[packed.length - 12];
        System.arraycopy(packed, 12, field48Packed, 0, field48Packed.length);

        Assertions.assertEquals(42, field48Packed.length, "Pack error");

        Assertions.assertEquals("0390012014199609300000000165001M0023003CT2", new String(field48Packed), "Pack error");

        msg = new ISOMsg();
        packager.unpack(msg, packed);

        msg.recalcBitMap();
        packed = packager.pack(msg);

        //skip 4 byte MTI and 8 byte Primary BitMap
        field48Packed = new byte[packed.length - 12];
        System.arraycopy(packed, 12, field48Packed, 0, field48Packed.length);

        Assertions.assertEquals(42, field48Packed.length, "Pack error");

        Assertions.assertEquals("0390012014199609300000000165001M0023003CT2", new String(field48Packed), "Pack error");
    }

    @Test
    public void testPackingWithISOField() throws ISOException, FileNotFoundException {

        ISOMsg msg = new ISOMsg("0100");

        GenericTagSequence tagValueSequence = new GenericTagSequence();


        ISOMsg field48 = new ISOMsg(48);
        field48.set("12", "19960930000000");
        field48.set("0165", "M");
        field48.set("23", "CT2");

        tagValueSequence.writeTo(field48);

        msg.set(field48);

        msg.recalcBitMap();

        GenericPackager packager = new GenericPackager(new FileInputStream("build/resources/test/org/jpos/tlv/tagged-sequence-packager.xml"));
        msg.setPackager(packager);

        byte[] packed = packager.pack(msg);

        //skip 4 byte MTI and 8 byte Primary BitMap
        byte[] field48Packed = new byte[packed.length - 12];
        System.arraycopy(packed, 12, field48Packed, 0, field48Packed.length);

        Assertions.assertEquals(42, field48Packed.length, "Pack error");

        Assertions.assertEquals("0390012014199609300000000023003CT20165001M", new String(field48Packed), "Pack error");

        msg = new ISOMsg();
        packager.unpack(msg, packed);

        msg.recalcBitMap();
        packed = packager.pack(msg);

        //skip 4 byte MTI and 8 byte Primary BitMap
        field48Packed = new byte[packed.length - 12];
        System.arraycopy(packed, 12, field48Packed, 0, field48Packed.length);

        Assertions.assertEquals(42, field48Packed.length, "Pack error");

        Assertions.assertEquals("0390012014199609300000000023003CT20165001M", new String(field48Packed), "Pack error");
    }

    @Test
    public void testPacking2() throws ISOException, FileNotFoundException {

        ISOMsg msg = new ISOMsg("0100");

        GenericTagSequence tagValueSequence = new GenericTagSequence();

        tagValueSequence.add(new LiteralTagValue("12", "A"));

        ISOMsg field64 = new ISOMsg(64);

        field64.set(new ISOField(0, "R"));

        tagValueSequence.writeTo(field64);

        msg.set(field64);

        msg.recalcBitMap();

        GenericPackager packager = new GenericPackager(new FileInputStream("build/resources/test/org/jpos/tlv/tagged-sequence-packager.xml"));
        msg.setPackager(packager);

        byte[] packed = packager.pack(msg);

        //skip 4 byte MTI and 8 byte Primary BitMap
        byte[] field64Packed = new byte[packed.length - 12];
        System.arraycopy(packed, 12, field64Packed, 0, field64Packed.length);

        Assertions.assertEquals(9, field64Packed.length, "Pack error");

        Assertions.assertEquals("006R1201A", new String(field64Packed), "Pack error");

        msg = new ISOMsg();
        packager.unpack(msg, packed);

        msg.recalcBitMap();
        packed = packager.pack(msg);

        //skip 4 byte MTI and 8 byte Primary BitMap
        field64Packed = new byte[packed.length - 12];
        System.arraycopy(packed, 12, field64Packed, 0, field64Packed.length);

        Assertions.assertEquals(9, field64Packed.length, "Pack error");

        Assertions.assertEquals("006R1201A", new String(field64Packed), "Pack error");
    }

    @Test
    public void testPackingWithPrefixes() throws ISOException, FileNotFoundException {

        ISOMsg msg = new ISOMsg("0100");

        GenericTagSequence tagValueSequence = new GenericTagSequence();

        tagValueSequence.add(new LiteralTagValue("0012", "19960930000000"));
        tagValueSequence.add(new LiteralTagValue("0165", "M"));
        tagValueSequence.add(new LiteralTagValue("0023", "CT2"));

        ISOMsg field63 = new ISOMsg(63);
        field63.set(new ISOField(0, "M"));
        tagValueSequence.writeTo(field63);

        msg.set(field63);

        msg.recalcBitMap();

        GenericPackager packager = new GenericPackager(new FileInputStream("build/resources/test/org/jpos/tlv/tagged-sequence-packager.xml"));
        msg.setPackager(packager);

        byte[] packed = packager.pack(msg);

        //skip 4 byte MTI and 8 byte Primary BitMap
        byte[] field48Packed = new byte[packed.length - 12];
        System.arraycopy(packed, 12, field48Packed, 0, field48Packed.length);

        Assertions.assertEquals(43, field48Packed.length, "Pack error");

        Assertions.assertEquals("040M0012014199609300000000165001M0023003CT2", new String(field48Packed), "Pack error");

        msg = new ISOMsg();
        packager.unpack(msg, packed);

        packed = packager.pack(msg);

        //skip 4 byte MTI and 8 byte Primary BitMap
        field48Packed = new byte[packed.length - 12];
        System.arraycopy(packed, 12, field48Packed, 0, field48Packed.length);

        Assertions.assertEquals(43, field48Packed.length, "Pack error");

        Assertions.assertEquals("040M0012014199609300000000165001M0023003CT2", new String(field48Packed), "Pack error");

    }

    @Test
    public void testPackingSpanningMultipleFields() throws ISOException, FileNotFoundException {

        ISOMsg msg = new ISOMsg("0100");

        GenericTagSequence tagValueSequence = new GenericTagSequence();

        tagValueSequence.add(new LiteralTagValue("0012", "19960930000000"));
        tagValueSequence.add(new LiteralTagValue("0165", "M"));
        tagValueSequence.add(new LiteralTagValue("0023", "CT2"));
        tagValueSequence.add(new LiteralTagValue("0170", "1-800-555-1212"));

        ISOMsg field48 = new ISOMsg(48);
        tagValueSequence.writeTo(field48);
        ISOMsgRef isoMsgRef = new ISOMsgRef(field48);
        msg.set(isoMsgRef.reference(48));

        msg.set(isoMsgRef.reference(62));

        msg.recalcBitMap();

        GenericPackager packager = new GenericPackager(new FileInputStream("build/resources/test/org/jpos/tlv/tagged-sequence-packager.xml"));
        msg.setPackager(packager);

        byte[] packed = packager.pack(msg);

        //skip 4 byte MTI and 8 byte Primary BitMap
        byte[] field48Packed = new byte[packed.length - 12];
        System.arraycopy(packed, 12, field48Packed, 0, field48Packed.length);

        Assertions.assertEquals(66, field48Packed.length, "Pack error");

        Assertions.assertEquals("0390012014199609300000000165001M0023003CT202101700141-800-555-1212", new String(field48Packed), "Pack error");

        msg = new ISOMsg();
        packager.unpack(msg, packed);

        tagValueSequence = new GenericTagSequence();
        tagValueSequence.readFrom((ISOMsg) msg.getComponent(48));
        tagValueSequence.readFrom((ISOMsg) msg.getComponent(62));

        Assertions.assertEquals(4, tagValueSequence.getAll().size(), "Unpack error");

        msg.recalcBitMap();
        packed = packager.pack(msg);

        //skip 4 byte MTI and 8 byte Primary BitMap
        field48Packed = new byte[packed.length - 12];
        System.arraycopy(packed, 12, field48Packed, 0, field48Packed.length);

        Assertions.assertEquals(66, field48Packed.length, "Pack error");

        Assertions.assertEquals("0390012014199609300000000165001M0023003CT202101700141-800-555-1212", new String(field48Packed), "Pack error");

    }
}

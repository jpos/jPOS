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

import org.jpos.emv.*;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.iso.packager.ISO87BPackager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @author Vishnu Pillai
 *         Date: 1/21/14
 */
public class EMVTagSequenceTest {

    @Test
    public void testPacking() throws ISOException, FileNotFoundException, UnknownTagNumberException {

        ISOMsg msg = new ISOMsg("0100");

        EMVTagSequence tagValueSequence = new EMVTagSequence();

        tagValueSequence.add(new LiteralEMVTag(EMVStandardTagType.APPLICATION_PRIMARY_ACCOUNT_NUMBER_0x5A, "999999123456789"));

        tagValueSequence.add(new LiteralEMVTag(EMVStandardTagType.APPLICATION_PREFERRED_NAME_0x9F12, "JPOS"));

        tagValueSequence.add(new LiteralEMVTag(EMVStandardTagType.APPLICATION_LABEL_0x50, "Q2"));

        tagValueSequence.add(new LiteralEMVTag(EMVStandardTagType.TRANSACTION_CURRENCY_CODE_0x5F2A, "840"));

        tagValueSequence.add(new LiteralEMVTag(EMVStandardTagType.AMOUNT_AUTHORISED_NUMERIC_0x9F02, "000000000100"));

        ISOMsg field48 = new ISOMsg(48);

        tagValueSequence.writeTo(field48);

        msg.set(field48);

        msg.recalcBitMap();

        GenericPackager packager = new GenericPackager(new FileInputStream("build/resources/test/org/jpos/tlv/emv-tlv-packager.xml"));
        msg.setPackager(packager);

        byte[] packed = packager.pack(msg);

        // 30313030
        // 00 00 00 00 00 01 00 00
        // 30 32 30
        // 50 02 51 32
        // 5a 07 19 96 09 30 00 00 00
        // 9f 12 04 4a 50 4f 53
        System.out.println("Packed: " + ISOUtil.byte2hex(packed));

        //skip 4 byte MTI and 8 byte Primary BitMap
        byte[] field48Packed = new byte[packed.length - 12];
        System.arraycopy(packed, 12, field48Packed, 0, field48Packed.length);

        Assertions.assertEquals(38, field48Packed.length, "Pack error");
        Assertions.assertEquals("3033355a08999999123456789f9f12044a504f53500251325f2a0208409f0206000000000100", ISOUtil.byte2hex(field48Packed), "Pack error");

        msg = new ISOMsg();
        packager.unpack(msg, packed);

        msg.recalcBitMap();

        tagValueSequence = new EMVTagSequence();
        tagValueSequence.readFrom((ISOMsg) msg.getComponent(48));

        Assertions.assertEquals(5, tagValueSequence.getAll().size(), "Unpack error");

        String tag1 = EMVStandardTagType.APPLICATION_PRIMARY_ACCOUNT_NUMBER_0x5A.getTagNumberHex();
        LiteralEMVTag pan = (LiteralEMVTag) tagValueSequence.getFirst(tag1);
        Assertions.assertEquals("999999123456789", pan.getValue(), "Unpack error");

        String tag2 = EMVStandardTagType.APPLICATION_PREFERRED_NAME_0x9F12.getTagNumberHex();
        LiteralEMVTag name = (LiteralEMVTag) tagValueSequence.getFirst(tag2);
        Assertions.assertEquals("JPOS", name.getValue(), "Unpack error");

        String tag3 = EMVStandardTagType.APPLICATION_LABEL_0x50.getTagNumberHex();
        LiteralEMVTag label = (LiteralEMVTag) tagValueSequence.getFirst(tag3);
        Assertions.assertEquals("Q2", label.getValue(), "Unpack error");

        String tag4 = EMVStandardTagType.TRANSACTION_CURRENCY_CODE_0x5F2A.getTagNumberHex();
        LiteralEMVTag currency = (LiteralEMVTag) tagValueSequence.getFirst(tag4);
        Assertions.assertEquals("840", currency.getValue(), "Unpack error");

        String tag5 = EMVStandardTagType.AMOUNT_AUTHORISED_NUMERIC_0x9F02.getTagNumberHex();
        LiteralEMVTag amount = (LiteralEMVTag) tagValueSequence.getFirst(tag5);
        Assertions.assertEquals("100", amount.getValue(), "Unpack error");

        packed = packager.pack(msg);

        //skip 4 byte MTI and 8 byte Primary BitMap
        field48Packed = new byte[packed.length - 12];
        System.arraycopy(packed, 12, field48Packed, 0, field48Packed.length);

        Assertions.assertEquals(38, field48Packed.length, "Pack error " + ISOUtil.hexString(field48Packed));

        Assertions.assertEquals("3033355a08999999123456789f9f12044a504f53500251325f2a0208409f0206000000000100", ISOUtil.byte2hex(field48Packed), "Pack error");
    }

    @Test
    public void testARPC() throws ISOException, FileNotFoundException {
        String arpcResponse = "283f473f613b3f3f0012";
        ISOMsg m = new ISOMsg();
        ISOMsg field55 = new ISOMsg(48);
        EMVTagSequence field55Tags = new EMVTagSequence();
        byte[] arpc = ISOUtil.hex2byte(arpcResponse);
        field55Tags.add(new BinaryEMVTag(EMVStandardTagType.ISSUER_AUTHENTICATION_DATA_0x91, arpc));
        field55Tags.writeTo(field55);
        m.set(field55);
        m.dump (System.out, "");
        GenericPackager packager = new GenericPackager(new FileInputStream("build/resources/test/org/jpos/tlv/emv-tlv-packager.xml"));
        m.setPackager(packager);
        System.out.printf(ISOUtil.hexdump (m.pack()));
    }
}

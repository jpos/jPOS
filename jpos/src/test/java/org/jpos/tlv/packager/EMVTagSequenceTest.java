package org.jpos.tlv.packager;

import org.jpos.emv.EMVStandardTagType;
import org.jpos.emv.EMVTagSequence;
import org.jpos.emv.LiteralEMVTag;
import org.jpos.emv.UnknownTagNumberException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.GenericPackager;
import org.junit.Assert;
import org.junit.Test;

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

        tagValueSequence.add(new LiteralEMVTag(EMVStandardTagType.APPLICATION_PRIMARY_ACCOUNT_NUMBER_0x5A, "19960930000000"));

        tagValueSequence.add(new LiteralEMVTag(EMVStandardTagType.APPLICATION_PREFERRED_NAME_0x9F12, "JPOS"));

        tagValueSequence.add(new LiteralEMVTag(EMVStandardTagType.APPLICATION_LABEL_0x50, "Q2"));

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

        Assert.assertEquals("Pack error", 23, field48Packed.length);

        Assert.assertEquals("Pack error", "303230500251325a07199609300000009f12044a504f53", ISOUtil.byte2hex(field48Packed));

        msg = new ISOMsg();
        packager.unpack(msg, packed);

        msg.recalcBitMap();

        tagValueSequence = new EMVTagSequence();
        tagValueSequence.readFrom((ISOMsg) msg.getComponent(48));

        Assert.assertEquals("Unpack error", 3, tagValueSequence.getAll().size());

        String tag1 = EMVStandardTagType.APPLICATION_PRIMARY_ACCOUNT_NUMBER_0x5A.getTagNumberHex();
        LiteralEMVTag pan = (LiteralEMVTag) tagValueSequence.getFirst(tag1);
        Assert.assertEquals("Unpack error", "19960930000000", pan.getValue());

        String tag2 = EMVStandardTagType.APPLICATION_PREFERRED_NAME_0x9F12.getTagNumberHex();
        LiteralEMVTag name = (LiteralEMVTag) tagValueSequence.getFirst(tag2);
        Assert.assertEquals("Unpack error", "JPOS", name.getValue());

        String tag3 = EMVStandardTagType.APPLICATION_LABEL_0x50.getTagNumberHex();
        LiteralEMVTag label = (LiteralEMVTag) tagValueSequence.getFirst(tag3);
        Assert.assertEquals("Unpack error", "Q2", label.getValue());

        packed = packager.pack(msg);

        //skip 4 byte MTI and 8 byte Primary BitMap
        field48Packed = new byte[packed.length - 12];
        System.arraycopy(packed, 12, field48Packed, 0, field48Packed.length);

        Assert.assertEquals("Pack error", 23, field48Packed.length);

        Assert.assertEquals("Pack error", "303230500251325a07199609300000009f12044a504f53", ISOUtil.byte2hex(field48Packed));
    }

}
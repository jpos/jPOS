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

import java.lang.reflect.Method;
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
    public void testRejectsLengthThatExceedsRemainingInput() throws ISOException {
        BERTLVPackager p = new BERTLVBinaryPackager();
        p.setFieldPackager(new ISOFieldPackager[]{new IFA_TTLLBINARY()});

        ISOException e = assertThrows(
          ISOException.class,
          () -> p.unpack(new ISOMsg(55), ISOUtil.hex2byte("9F34031122"))
        );
        assertTrue(e.getMessage().contains("Truncated BER-TLV value"));
    }

    @Test
    public void testRejectsMaxIntegerLengthWithoutAllocating() throws ISOException {
        BERTLVPackager p = new BERTLVBinaryPackager();
        p.setFieldPackager(new ISOFieldPackager[]{new IFA_TTLLBINARY()});

        ISOException e = assertThrows(
          ISOException.class,
          () -> p.unpack(new ISOMsg(55), ISOUtil.hex2byte("9F34847FFFFFFF"))
        );
        assertTrue(e.getMessage().contains("Truncated BER-TLV value"));
    }

    @Test
    public void testRejectsTruncatedExtendedTag() throws ISOException {
        BERTLVPackager p = new BERTLVBinaryPackager();
        p.setFieldPackager(new ISOFieldPackager[]{new IFA_TTLLBINARY()});

        ISOException e = assertThrows(
          ISOException.class,
          () -> p.unpack(new ISOMsg(55), ISOUtil.hex2byte("9F"))
        );
        assertTrue(e.getMessage().contains("Truncated BER-TLV tag"));
    }

    @Test
    public void testRejectsTagLongerThanSupported() throws ISOException {
        BERTLVPackager p = new BERTLVBinaryPackager();
        p.setFieldPackager(new ISOFieldPackager[]{new IFA_TTLLBINARY()});

        ISOException e = assertThrows(
          ISOException.class,
          () -> p.unpack(new ISOMsg(55), ISOUtil.hex2byte("9F8180"))
        );
        assertTrue(e.getMessage().contains("tag exceeds 3 bytes"));
    }

    @Test
    public void testRejectsTruncatedLongFormLength() throws ISOException {
        BERTLVPackager p = new BERTLVBinaryPackager();
        p.setFieldPackager(new ISOFieldPackager[]{new IFA_TTLLBINARY()});

        ISOException e = assertThrows(
          ISOException.class,
          () -> p.unpack(new ISOMsg(55), ISOUtil.hex2byte("9F3482"))
        );
        assertTrue(e.getMessage().contains("Truncated BER-TLV length"));
    }

    @Test
    public void testRejectsIndefiniteLength() throws ISOException {
        BERTLVPackager p = new BERTLVBinaryPackager();
        p.setFieldPackager(new ISOFieldPackager[]{new IFA_TTLLBINARY()});

        ISOException e = assertThrows(
          ISOException.class,
          () -> p.unpack(new ISOMsg(55), ISOUtil.hex2byte("9F3480"))
        );
        assertTrue(e.getMessage().contains("Indefinite BER-TLV lengths are not supported"));
    }

    @Test
    public void testRejectsLengthLongerThanSupported() throws ISOException {
        BERTLVPackager p = new BERTLVBinaryPackager();
        p.setFieldPackager(new ISOFieldPackager[]{new IFA_TTLLBINARY()});

        ISOException e = assertThrows(
          ISOException.class,
          () -> p.unpack(new ISOMsg(55), ISOUtil.hex2byte("9F3485"))
        );
        assertTrue(e.getMessage().contains("length exceeds 4 bytes"));
    }

    @Test
    public void testRejectsInvalidConfiguredFirstFieldConsumption() throws ISOException {
        byte[] data = ISOUtil.hex2byte("009400");
        for (int reported : new int[] {-1, 0, Integer.MAX_VALUE}) {
            BERTLVPackager p = new BERTLVBinaryPackager();
            p.setFieldPackager(new ISOFieldPackager[] {
              null, new ControlledConsumptionPackager(data.length, reported)
            });
            ISOMsg m = new ISOMsg(55);

            ISOException e = assertThrows(ISOException.class, () -> p.unpack(m, data));

            assertTrue(e.getMessage().contains("Configured field 1"));
            assertFalse(m.hasField(1));
        }
    }

    @Test
    public void testRejectsInvalidConfiguredLastFieldConsumption() throws ISOException {
        byte[] data = ISOUtil.hex2byte("0102");
        for (int reported : new int[] {-1, 0, Integer.MAX_VALUE}) {
            BERTLVPackager p = new BERTLVBinaryPackager();
            p.setFieldPackager(new ISOFieldPackager[] {
              null, null, new ControlledConsumptionPackager(data.length, reported)
            });
            ISOMsg m = new ISOMsg(55);

            ISOException e = assertThrows(ISOException.class, () -> p.unpack(m, data));

            assertTrue(e.getMessage().contains("Configured field 2"));
            assertFalse(m.hasField(2));
        }
    }

    @Test
    public void testAcceptsValidConfiguredFieldConsumption() throws ISOException {
        BERTLVPackager firstPackager = new BERTLVBinaryPackager();
        firstPackager.setFieldPackager(new ISOFieldPackager[] {
          null, new ControlledConsumptionPackager(1, 1)
        });
        ISOMsg firstMessage = new ISOMsg(55);
        assertEquals(3, firstPackager.unpack(firstMessage, ISOUtil.hex2byte("009400")));
        assertEquals("decoded", firstMessage.getString(1));

        BERTLVPackager lastPackager = new BERTLVBinaryPackager();
        lastPackager.setFieldPackager(new ISOFieldPackager[] {
          null, null, new ControlledConsumptionPackager(2, 2)
        });
        ISOMsg lastMessage = new ISOMsg(55);
        assertEquals(2, lastPackager.unpack(lastMessage, ISOUtil.hex2byte("0102")));
        assertEquals("decoded", lastMessage.getString(2));
    }

    @Test
    public void testUninterpretLengthDoesNotOverflow() throws Exception {
        BERTLVPackager p = new BERTLVBinaryPackager();
        Method method = BERTLVPackager.class.getDeclaredMethod(
          "getUninterpretLength", int.class, BinaryInterpreter.class
        );
        method.setAccessible(true);

        assertEquals(1 << 30, method.invoke(p, 1 << 30, LiteralBinaryInterpreter.INSTANCE));
    }

    @Test
    public void testAcceptsConstructedValueAtMaximumDepth() throws ISOException {
        BERTLVPackager p = new BERTLVBinaryPackager();
        p.setFieldPackager(new ISOFieldPackager[]{new IFA_TTLLBINARY()});
        byte[] encoded = nestedConstructedValue(64);

        assertEquals(encoded.length, p.unpack(new ISOMsg(55), encoded));
    }

    @Test
    public void testRejectsConstructedValueBeyondMaximumDepth() throws ISOException {
        BERTLVPackager p = new BERTLVBinaryPackager();
        p.setFieldPackager(new ISOFieldPackager[]{new IFA_TTLLBINARY()});

        ISOException e = assertThrows(
          ISOException.class,
          () -> p.unpack(new ISOMsg(55), nestedConstructedValue(65))
        );
        assertTrue(e.getMessage().contains("maximum depth of 64"));
    }

    @Test
    public void testPackingAcceptsConstructedValueAtMaximumDepth() throws ISOException {
        BERTLVPackager p = new BERTLVBinaryPackager();

        assertDoesNotThrow(() -> p.pack(nestedConstructedMessage(64), true, 0, 1));
    }

    @Test
    public void testPackingRejectsConstructedValueBeyondMaximumDepth() throws ISOException {
        BERTLVPackager p = new BERTLVBinaryPackager();

        ISOException e = assertThrows(
          ISOException.class,
          () -> p.pack(nestedConstructedMessage(65), true, 0, 1)
        );
        assertTrue(e.getMessage().contains("maximum depth of 64"));
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

    private byte[] nestedConstructedValue(int depth) {
        byte[] encoded = ISOUtil.hex2byte("9400");
        for (int i = 0; i < depth; i++) {
            byte[] length = encodeLength(encoded.length);
            byte[] nested = new byte[1 + length.length + encoded.length];
            nested[0] = 0x71;
            System.arraycopy(length, 0, nested, 1, length.length);
            System.arraycopy(encoded, 0, nested, 1 + length.length, encoded.length);
            encoded = nested;
        }
        return encoded;
    }

    private byte[] encodeLength(int length) {
        if (length <= 0x7F)
            return new byte[] { (byte) length };
        if (length <= 0xFF)
            return new byte[] { (byte) 0x81, (byte) length };
        return new byte[] { (byte) 0x82, (byte) (length >>> 8), (byte) length };
    }

    private ISOMsg nestedConstructedMessage(int depth) throws ISOException {
        ISOMsg root = new ISOMsg(55);
        ISOMsg parent = root;
        for (int i = 0; i < depth; i++) {
            ISOMsg child = new ISOMsg(1);
            parent.set(new ISOTaggedField("71", child));
            parent = child;
        }
        parent.set(new ISOTaggedField("94", new ISOField(1, "")));
        return root;
    }

    private static class ControlledConsumptionPackager extends ISOFieldPackager {
        private final int reportedConsumption;

        private ControlledConsumptionPackager(int length, int reportedConsumption) {
            super(length, "controlled consumption");
            this.reportedConsumption = reportedConsumption;
        }

        @Override
        public int getMaxPackedLength() {
            return getLength();
        }

        @Override
        public byte[] pack(ISOComponent c) {
            return new byte[0];
        }

        @Override
        public int unpack(ISOComponent c, byte[] b, int offset) throws ISOException {
            c.setValue("decoded");
            return reportedConsumption;
        }
    }
}

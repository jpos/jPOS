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

import org.jpos.iso.packager.DatasetPackager;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.tlv.TLVList;
import org.jpos.tlv.TLVMsg;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DatasetPackagerTest {
    @Test
    public void testDBMSingleBitmapParsing() throws Exception {
        DatasetPackager packager = createBinaryDatasetPackager(2);
        ISODatasetField field = new ISODatasetField(49);
        byte[] packed = ISOUtil.hex2byte("71000460001122");

        int consumed = packager.unpack(field, packed);

        assertEquals(packed.length, consumed);
        assertEquals(1, field.getDatasets().size());
        Dataset dataset = field.getDataset(0x71);
        assertNotNull(dataset);
        assertEquals(DatasetFormat.DBM, dataset.getFormat());
        assertArrayEquals(new byte[] { 0x11 }, ((ISODataset) dataset).getBytes(1));
        assertArrayEquals(new byte[] { 0x22 }, ((ISODataset) dataset).getBytes(2));
        assertArrayEquals(packed, packager.pack(field));
    }

    @Test
    public void testDBMChainedBitmapRoundTrip() throws Exception {
        DatasetPackager packager = createBinaryDatasetPackager(16);
        ISODatasetField field = new ISODatasetField(49);
        ISODataset dataset = new ISODataset(0x71, DatasetFormat.DBM);
        dataset.addElement(1, new ISOBinaryField(1, new byte[] { 0x11 }));
        dataset.addElement(16, new ISOBinaryField(16, new byte[] { 0x22 }));
        field.addDataset(dataset);

        byte[] packed = packager.pack(field);

        assertEquals("710005C000401122", ISOUtil.hexString(packed));

        ISODatasetField unpackedField = new ISODatasetField(49);
        packager.unpack(unpackedField, packed);
        ISODataset unpacked = (ISODataset) unpackedField.getDataset(0x71);
        assertArrayEquals(new byte[] { 0x11 }, unpacked.getBytes(1));
        assertArrayEquals(new byte[] { 0x22 }, unpacked.getBytes(16));
    }

    @Test
    public void testTLVParsingSupportsOneTwoAndThreeByteTags() throws Exception {
        DatasetPackager packager = new DatasetPackager();
        packager.setFieldPackager(new ISOFieldPackager[1]);
        ISODatasetField field = new ISODatasetField(34);
        byte[] packed = ISOUtil.hex2byte("0100145A0212349F02060102030405069F810103070809");

        packager.unpack(field, packed);

        ISODataset dataset = (ISODataset) field.getDataset(0x01);
        assertNotNull(dataset);
        assertArrayEquals(ISOUtil.hex2byte("1234"), dataset.getBytes(0x5A));
        assertArrayEquals(ISOUtil.hex2byte("010203040506"), dataset.getBytes(0x9F02));
        assertArrayEquals(ISOUtil.hex2byte("070809"), dataset.getBytes(0x9F8101));
        assertArrayEquals(packed, packager.pack(field));
    }

    @Test
    public void testCompositeFieldSupportsMixedAndRepeatedDatasets() throws Exception {
        DatasetPackager packager = createBinaryDatasetPackager(2);
        ISODatasetField field = new ISODatasetField(49);

        ISODataset tlvDataset = new ISODataset(0x01, DatasetFormat.TLV);
        tlvDataset.addElement(0x5A, new ISOBinaryField(0x5A, ISOUtil.hex2byte("1234")));
        field.addDataset(tlvDataset);

        ISODataset dbmDataset1 = new ISODataset(0x71, DatasetFormat.DBM);
        dbmDataset1.addElement(1, new ISOBinaryField(1, new byte[] { 0x11 }));
        field.addDataset(dbmDataset1);

        ISODataset dbmDataset2 = new ISODataset(0x71, DatasetFormat.DBM);
        dbmDataset2.addElement(2, new ISOBinaryField(2, new byte[] { 0x22 }));
        field.addDataset(dbmDataset2);

        byte[] packed = packager.pack(field);

        ISODatasetField unpackedField = new ISODatasetField(49);
        packager.unpack(unpackedField, packed);
        assertEquals(3, unpackedField.getDatasets().size());
        assertEquals(2, unpackedField.getDatasets(0x71).size());
        assertArrayEquals(ISOUtil.hex2byte("1234"), ((ISODataset) unpackedField.getDataset(0x01)).getBytes(0x5A));
        assertArrayEquals(new byte[] { 0x11 }, ((ISODataset) unpackedField.getDatasets(0x71).get(0)).getBytes(1));
        assertArrayEquals(new byte[] { 0x22 }, ((ISODataset) unpackedField.getDatasets(0x71).get(1)).getBytes(2));
    }

    @Test
    public void testCMFElectronicCommerceDataRoundTrip() throws Exception {
        GenericPackager packager = createCMFV3Packager();

        ISOMsg msg = new ISOMsg("0100");
        msg.setPackager(packager);
        ISODatasetField field34 = new ISODatasetField(34);
        ISODataset dataset = new ISODataset(0x01, DatasetFormat.TLV)
          .with(0x5A, ISOUtil.hex2byte("1234"))
          .with(0x9F02, ISOUtil.hex2byte("000000000100"));
        field34.addDataset(dataset);
        msg.set(field34);

        byte[] packed = msg.pack();

        ISOMsg unpacked = new ISOMsg();
        unpacked.setPackager(packager);
        unpacked.unpack(packed);

        assertInstanceOf(ISODatasetField.class, unpacked.getComponent(34));
        ISODataset parsed = (ISODataset) ((ISODatasetField) unpacked.getComponent(34)).getDataset(0x01);
        assertArrayEquals(ISOUtil.hex2byte("1234"), parsed.getBytes(0x5A));
        assertArrayEquals(ISOUtil.hex2byte("000000000100"), parsed.getBytes(0x9F02));
    }

    @Test
    public void testCMFVerificationDataAndICCDataRoundTrip() throws Exception {
        GenericPackager packager = createCMFV3Packager();

        ISOMsg msg = new ISOMsg("0100");
        msg.setPackager(packager);

        ISODatasetField field49 = new ISODatasetField(49);
        ISODataset verification = new ISODataset(0x71, DatasetFormat.DBM)
          .with(1, "1")
          .with(2, "1234");
        field49.addDataset(verification);
        msg.set(field49);

        ISODatasetField field55 = new ISODatasetField(55);
        ISODataset icc = new ISODataset(55, DatasetFormat.TLV)
          .with(0x9F26, ISOUtil.hex2byte("1122334455667788"))
          .with(0x9F10, ISOUtil.hex2byte("06011203A0B800"));
        field55.addDataset(icc);
        msg.set(field55);

        byte[] packed = msg.pack();

        ISOMsg unpacked = new ISOMsg();
        unpacked.setPackager(packager);
        unpacked.unpack(packed);

        assertInstanceOf(ISODatasetField.class, unpacked.getComponent(49));
        ISODataset parsed49 = (ISODataset) ((ISODatasetField) unpacked.getComponent(49)).getDataset(0x71);
        assertEquals("1", parsed49.getValue(1));
        assertEquals("1234", parsed49.getValue(2));

        assertInstanceOf(ISODatasetField.class, unpacked.getComponent(55));
        ISODataset parsed55 = (ISODataset) ((ISODatasetField) unpacked.getComponent(55)).getDataset(55);
        assertArrayEquals(ISOUtil.hex2byte("1122334455667788"), parsed55.getBytes(0x9F26));
        assertArrayEquals(ISOUtil.hex2byte("06011203A0B800"), parsed55.getBytes(0x9F10));
    }

    @Test
    public void testCMFJarPackagerAllDatasetFieldsWithFluentMessageBuilder() throws Exception {
        GenericPackager packager = createCMFV3Packager();

        ISOMsg msg = new ISOMsg("0100");
        msg.setPackager(packager);

        msg.with("34.0x01.0x5A", ISOUtil.hex2byte("1234"))
          .with("43.0x01.0x9F1A", ISOUtil.hex2byte("0840"))
          .with("43.0x71.2", "Example Merchant")
          .with("43.0x71.7", "USA")
          .with("49.0x01.0x5F2A", ISOUtil.hex2byte("0840"))
          .with("49.0x71.1", "1")
          .with("49.0x71.2", "1234")
          .with("55.0x9F26", ISOUtil.hex2byte("1122334455667788"))
          .with("55.0x9F10", ISOUtil.hex2byte("06011203A0B800"))
          .with("71.0x01.0x9F36", ISOUtil.hex2byte("0022"))
          .with("104.0x01.0xDF01", ISOUtil.hex2byte("CAFEBABE"));

        byte[] packed = msg.pack();

        ISOMsg unpacked = new ISOMsg();
        unpacked.setPackager(packager);
        unpacked.unpack(packed);

        assertInstanceOf(ISODatasetField.class, unpacked.getComponent(34));
        assertInstanceOf(ISODatasetField.class, unpacked.getComponent(43));
        assertInstanceOf(ISODatasetField.class, unpacked.getComponent(49));
        assertInstanceOf(ISODatasetField.class, unpacked.getComponent(55));
        assertInstanceOf(ISODatasetField.class, unpacked.getComponent(71));
        assertInstanceOf(ISODatasetField.class, unpacked.getComponent(104));

        assertArrayEquals(ISOUtil.hex2byte("1234"), ((ISODataset) ((ISODatasetField) unpacked.getComponent(34)).getDataset(0x01)).getBytes(0x5A));
        assertArrayEquals(ISOUtil.hex2byte("0840"), ((ISODataset) ((ISODatasetField) unpacked.getComponent(43)).getDataset(0x01)).getBytes(0x9F1A));
        assertEquals("Example Merchant", ((ISODataset) ((ISODatasetField) unpacked.getComponent(43)).getDataset(0x71)).getValue(2));
        assertEquals("USA", ((ISODataset) ((ISODatasetField) unpacked.getComponent(43)).getDataset(0x71)).getValue(7));
        assertArrayEquals(ISOUtil.hex2byte("0840"), ((ISODataset) ((ISODatasetField) unpacked.getComponent(49)).getDataset(0x01)).getBytes(0x5F2A));
        assertEquals("1", ((ISODataset) ((ISODatasetField) unpacked.getComponent(49)).getDataset(0x71)).getValue(1));
        assertEquals("1234", ((ISODataset) ((ISODatasetField) unpacked.getComponent(49)).getDataset(0x71)).getValue(2));
        assertArrayEquals(ISOUtil.hex2byte("1122334455667788"), ((ISODataset) ((ISODatasetField) unpacked.getComponent(55)).getDataset(55)).getBytes(0x9F26));
        assertArrayEquals(ISOUtil.hex2byte("06011203A0B800"), ((ISODataset) ((ISODatasetField) unpacked.getComponent(55)).getDataset(55)).getBytes(0x9F10));
        assertArrayEquals(ISOUtil.hex2byte("0022"), ((ISODataset) ((ISODatasetField) unpacked.getComponent(71)).getDataset(0x01)).getBytes(0x9F36));
        assertArrayEquals(ISOUtil.hex2byte("CAFEBABE"), ((ISODataset) ((ISODatasetField) unpacked.getComponent(104)).getDataset(0x01)).getBytes(0xDF01));

        msg.dump (System.out, "");
    }

    @Test
    public void testCMFAndCMFV3Field55CompatibilityWithTLVList() throws Exception {
        GenericPackager legacyPackager = createLegacyCMFPackager();
        GenericPackager datasetPackager = createCMFV3Packager();

        TLVList expected55 = new TLVList()
          .append(0x9F26, "1122334455667788")
          .append(0x9F10, "06011203A0B800")
          .append(0x9F36, "0022")
          .append(0x95, "0000000000");
        byte[] expected55Bytes = expected55.pack();

        ISOMsg legacyMsg = new ISOMsg("0100");
        legacyMsg.setPackager(legacyPackager);
        legacyMsg.set(3, "000000");
        legacyMsg.set(11, "123456");
        legacyMsg.set(41, "TERMID01");
        legacyMsg.set(new ISOBinaryField(55, expected55Bytes));

        byte[] legacyPacked = legacyMsg.pack();

        ISOMsg unpackedLegacy = new ISOMsg();
        unpackedLegacy.setPackager(legacyPackager);
        unpackedLegacy.unpack(legacyPacked);

        ISOMsg datasetMsg = new ISOMsg("0100");
        datasetMsg.setPackager(datasetPackager);
        datasetMsg.with(3, "000000")
          .with(11, "123456")
          .with(41, "TERMID01")
          .with("55.0x9F26", ISOUtil.hex2byte("1122334455667788"))
          .with("55.0x9F10", ISOUtil.hex2byte("06011203A0B800"))
          .with("55.0x9F36", ISOUtil.hex2byte("0022"))
          .with("55.0x95", ISOUtil.hex2byte("0000000000"));

        byte[] datasetPacked = datasetMsg.pack();

        ISOMsg unpackedDataset = new ISOMsg();
        unpackedDataset.setPackager(datasetPackager);
        unpackedDataset.unpack(datasetPacked);

        assertArrayEquals(legacyPacked, datasetPacked);
        assertEquals(unpackedLegacy.getMTI(), unpackedDataset.getMTI());
        assertEquals(unpackedLegacy.getString(3), unpackedDataset.getString(3));
        assertEquals(unpackedLegacy.getString(11), unpackedDataset.getString(11));
        assertEquals(unpackedLegacy.getString(41), unpackedDataset.getString(41));
        assertArrayEquals(legacyPacked, unpackedDataset.pack());

        byte[] legacy55Bytes = unpackedLegacy.getBytes(55);
        assertArrayEquals(expected55Bytes, legacy55Bytes);

        TLVList legacy55 = new TLVList();
        legacy55.unpack(legacy55Bytes);
        assertTLVListsEqual(expected55, legacy55);

        assertInstanceOf(ISODatasetField.class, unpackedDataset.getComponent(55));
        ISODataset field55 = (ISODataset) ((ISODatasetField) unpackedDataset.getComponent(55)).getDataset(55);
        assertArrayEquals(ISOUtil.hex2byte("1122334455667788"), field55.getBytes(0x9F26));
        assertArrayEquals(ISOUtil.hex2byte("06011203A0B800"), field55.getBytes(0x9F10));
        assertArrayEquals(ISOUtil.hex2byte("0022"), field55.getBytes(0x9F36));
        assertArrayEquals(ISOUtil.hex2byte("0000000000"), field55.getBytes(0x95));
        assertDatasetMatchesTLVList(legacy55, field55);
        assertDatasetMatchesTLVList(expected55, field55);
    }

    @Test
    public void testCloneDeepClonesDatasetsAndSupportsFluentWithout() throws Exception {
        ISOMsg original = new ISOMsg("0100");
        original.setPackager(createCMFV3Packager());
        original.with(3, "000000")
          .with(11, "123456")
          .with("63.2", "ORIGINAL")
          .with("55.0x9F26", ISOUtil.hex2byte("1122334455667788"))
          .with("55.0x9F10", ISOUtil.hex2byte("06011203A0B800"));

        ISOMsg clone = (ISOMsg) original.clone();
        clone.with(11, "654321")
          .without(3)
          .without("63.2")
          .with("63.3", "CLONE")
          .without("55.0x9F10")
          .with("55.0x95", ISOUtil.hex2byte("0000000000"));

        assertNotSame(original.getComponent(55), clone.getComponent(55));
        assertNotSame(((ISODatasetField) original.getComponent(55)).getDataset(55), ((ISODatasetField) clone.getComponent(55)).getDataset(55));

        assertEquals("000000", original.getString(3));
        assertEquals("123456", original.getString(11));
        assertEquals("ORIGINAL", original.getString("63.2"));
        assertArrayEquals(ISOUtil.hex2byte("1122334455667788"), ((ISODatasetField) original.getComponent(55)).getBytes(55, 0x9F26));
        assertArrayEquals(ISOUtil.hex2byte("06011203A0B800"), ((ISODatasetField) original.getComponent(55)).getBytes(55, 0x9F10));
        assertNull(((ISODatasetField) original.getComponent(55)).getBytes(55, 0x95));

        assertNull(clone.getString(3));
        assertEquals("654321", clone.getString(11));
        assertNull(clone.getString("63.2"));
        assertEquals("CLONE", clone.getString("63.3"));
        assertArrayEquals(ISOUtil.hex2byte("1122334455667788"), ((ISODatasetField) clone.getComponent(55)).getBytes(55, 0x9F26));
        assertNull(((ISODatasetField) clone.getComponent(55)).getBytes(55, 0x9F10));
        assertArrayEquals(ISOUtil.hex2byte("0000000000"), ((ISODatasetField) clone.getComponent(55)).getBytes(55, 0x95));

        clone.without("55.0x9F26", "55.0x95");
        assertFalse(clone.hasField(55));
        assertTrue(original.hasField(55));
        assertArrayEquals(ISOUtil.hex2byte("1122334455667788"), ((ISODatasetField) original.getComponent(55)).getBytes(55, 0x9F26));
        assertArrayEquals(ISOUtil.hex2byte("06011203A0B800"), ((ISODatasetField) original.getComponent(55)).getBytes(55, 0x9F10));
    }

    private DatasetPackager createBinaryDatasetPackager(int maxElement) throws Exception {
        DatasetPackager packager = new DatasetPackager();
        ISOFieldPackager[] fieldPackagers = new ISOFieldPackager[maxElement + 1];
        for (int i = 1; i <= maxElement; i++) {
            fieldPackagers[i] = new IFB_BINARY(1, "dataset-element-" + i);
        }
        packager.setFieldPackager(fieldPackagers);
        return packager;
    }

    private GenericPackager createLegacyCMFPackager() throws Exception {
        return new GenericPackager("jar:packager/cmf.xml");
    }

    private GenericPackager createCMFV3Packager() throws Exception {
        return new GenericPackager("jar:packager/cmfv3.xml");
    }

    private void assertTLVListsEqual(TLVList expected, TLVList actual) {
        List<TLVMsg> expectedTags = expected.getTags();
        List<TLVMsg> actualTags = actual.getTags();
        assertEquals(expectedTags.size(), actualTags.size());
        for (int i = 0; i < expectedTags.size(); i++) {
            TLVMsg expectedTag = expectedTags.get(i);
            TLVMsg actualTag = actualTags.get(i);
            assertEquals(expectedTag.getTag(), actualTag.getTag());
            assertArrayEquals(expectedTag.getValue(), actualTag.getValue());
            assertArrayEquals(expectedTag.getTLV(), actualTag.getTLV());
        }
    }

    private void assertDatasetMatchesTLVList(TLVList expected, ISODataset actual) throws ISOException {
        List<TLVMsg> expectedTags = expected.getTags();
        assertEquals(expectedTags.size(), actual.getElements().size());
        for (TLVMsg expectedTag : expectedTags) {
            assertArrayEquals(expectedTag.getValue(), actual.getBytes(expectedTag.getTag()));
        }
    }
}

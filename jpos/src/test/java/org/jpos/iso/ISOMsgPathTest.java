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

import java.util.List;
import java.util.Optional;

import org.jpos.iso.packager.GenericPackager;
import org.jpos.iso.packager.XMLPackager;
import org.jpos.tlv.TLVList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Strict field path readers, dataset element writers and dataset-aware
 * {@link ISOMsg#clone(String...)}.
 */
public class ISOMsgPathTest {
    private static final byte[] AC = ISOUtil.hex2byte("1122334455667788");
    private static final byte[] IAD = ISOUtil.hex2byte("06011203A0B800");
    // issuer script templates: 9F18 script id + 86 command
    private static final byte[] SCRIPT1 = ISOUtil.hex2byte("9F180411223344" + "86058418000000");
    private static final byte[] SCRIPT2 = ISOUtil.hex2byte("9F180455667788" + "86058424000000");

    // ---- path grammar

    @ParameterizedTest
    @ValueSource(strings = {
      "", ".", "62..1", ".62", "62.", "62.1.", "6a", "-1", "+1", " 1", "1 ", "62. 1",
      "0X1F", "0x", "0x.1", "0xG1", "0x-1", "0x+1", "2147483648", "0x80000000", "99999999999",
      "٣", "１"
    })
    public void malformedPathsThrow(String fpath) throws Exception {
        for (ISOMsg m : List.of(new ISOMsg(), populated())) {
            assertThrows(IllegalArgumentException.class, () -> m.findValue(fpath), fpath);
            assertThrows(IllegalArgumentException.class, () -> m.findDatasetElements(fpath), fpath);
            assertThrows(IllegalArgumentException.class, () -> m.clone(fpath), fpath);
            assertThrows(IllegalArgumentException.class, () -> m.setDatasetElement(fpath, 0x9F26, AC), fpath);
            assertThrows(IllegalArgumentException.class, () -> m.unsetDatasetElement(fpath, 0x9F26), fpath);
        }
    }

    @Test
    public void nullPathThrows() {
        ISOMsg m = new ISOMsg();
        assertThrows(IllegalArgumentException.class, () -> m.findValue(null));
        assertThrows(IllegalArgumentException.class, () -> m.findDatasetElements(null));
    }

    @Test
    public void malformedPathValidityDoesNotDependOnContents() {
        ISOMsg m = new ISOMsg();
        m.set(62, "leaf");
        // hasField stops at the leaf and never parses "x"; findValue always does
        assertFalse(m.hasField("62.x"));
        assertThrows(IllegalArgumentException.class, () -> m.findValue("62.x"));
        assertThrows(IllegalArgumentException.class, () -> m.findValue("64.x"));
    }

    @Test
    public void malformedPathMessageNamesSegment() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> new ISOMsg().findValue("62..1"));
        assertEquals("Invalid path '62..1': empty segment at position 2", e.getMessage());
        e = assertThrows(IllegalArgumentException.class, () -> new ISOMsg().findValue("62.0X1"));
        assertEquals("Invalid path '62.0X1': invalid segment '0X1' at position 2", e.getMessage());
        e = assertThrows(IllegalArgumentException.class, () -> new ISOMsg().findValue("0x80000000"));
        assertEquals("Invalid path '0x80000000': segment '0x80000000' at position 1 is out of range", e.getMessage());
    }

    @Test
    public void validPathForms() throws Exception {
        ISOMsg m = new ISOMsg();
        m.set(7, "0123456789");
        m.set(10, "61000000");
        assertEquals(Optional.of("0123456789"), m.findValue("007"));
        assertEquals(Optional.of("61000000"), m.findValue("0x0A"));
        assertEquals(Optional.of("61000000"), m.findValue("0x0a"));
        assertEquals(Optional.empty(), m.findValue("2147483647"));
        assertEquals(Optional.empty(), m.findValue("0x7FFFFFFF"));
    }

    // ---- strict walk

    @Test
    public void strictWalk() throws Exception {
        ISOMsg m = populated();
        assertEquals(Optional.of("0100"), m.findValue("0"));
        assertEquals(Optional.of("000000"), m.findValue("3"));
        assertArrayEquals(ISOUtil.hex2byte("CAFEBABE"), (byte[]) m.findValue("52").orElseThrow());
        assertEquals(Optional.of("value63.2.3"), m.findValue("63.2.3"));
        assertSame(m.getComponent(63), m.findValue("63").orElseThrow());
        assertSame(m.getComponent("63.2"), m.findValue("63.2").orElseThrow());

        assertEquals(Optional.empty(), m.findValue("4"), "absent");
        assertEquals(Optional.empty(), m.findValue("64.1"), "absent first level");
        assertEquals(Optional.empty(), m.findValue("63.9.1"), "absent second level");
        assertEquals(Optional.empty(), m.findValue("63.2.9"), "absent leaf");
        assertEquals(Optional.empty(), m.findValue("62.1"), "through a leaf");
        assertEquals(Optional.empty(), m.findValue("63.2.3.1"), "through a nested leaf");
        assertEquals(Optional.empty(), new ISOMsg().findValue("0"), "absent MTI");

        // the lenient readers disagree on a path through a leaf
        assertThrows(ISOException.class, () -> m.getValue("62.1"));
        assertNull(m.getString("62.1"));
        assertNull(m.getComponent("62.1"));
    }

    @Test
    public void findDatasetElementsOnNonDatasetPaths() throws Exception {
        ISOMsg m = populated();
        assertTrue(m.findDatasetElements("3").isEmpty());
        assertTrue(m.findDatasetElements("62.1").isEmpty());
        assertTrue(m.findDatasetElements("63.2.3").isEmpty());
        assertTrue(m.findDatasetElements("64.1").isEmpty());
        assertTrue(m.findDatasetElements("55").isEmpty(), "bare dataset field");
    }

    // ---- dataset descent

    @Test
    public void datasetPathsWithPackager() throws Exception {
        GenericPackager packager = cmfv3();
        ISOMsg m = roundTrip(packager, cmfMsg(packager)
          .with("55.0x9F26", AC)
          .with("55.0x9F10", IAD)
          .with("104.0x01.0xDF01", ISOUtil.hex2byte("CAFEBABE"))
          .with("49.0x71.2", "1234"));

        assertArrayEquals(AC, (byte[]) m.findValue("55.0x9F26").orElseThrow());
        assertArrayEquals(IAD, (byte[]) m.findValue("55.0x9F10").orElseThrow());
        assertArrayEquals(ISOUtil.hex2byte("CAFEBABE"), (byte[]) m.findValue("104.0x01.0xDF01").orElseThrow());
        assertEquals(Optional.of("1234"), m.findValue("49.0x71.2"));

        assertEquals(Optional.empty(), m.findValue("55.0x9F27"), "absent tag");
        assertEquals(Optional.empty(), m.findValue("104.0x02.0xDF01"), "absent dataset");
        assertEquals(Optional.empty(), m.findValue("104.0x01.0xDF01.1"), "more than two segments");
        assertEquals(Optional.empty(), m.findValue("104.0xDF01"), "envelope field, one segment");
        assertEquals(Optional.empty(), m.findValue("55.55.0x9F26"), "no-envelope field, two segments");
        assertTrue(m.findDatasetElements("55.55.0x9F26").isEmpty());

        // without the packager the layout is inferred from the segment count, so the
        // dataset that DE 55 stores under its own field number is reachable both ways
        m.setPackager(null);
        assertArrayEquals(AC, (byte[]) m.findValue("55.0x9F26").orElseThrow());
        assertArrayEquals(AC, (byte[]) m.findValue("55.55.0x9F26").orElseThrow());
        assertEquals(Optional.empty(), m.findValue("104.0xDF01"));
    }

    @Test
    public void bareDatasetFieldValue() throws Exception {
        ISOMsg m = populated();
        ISODatasetField f55 = (ISODatasetField) m.getComponent(55);
        assertEquals(f55.getDatasets(), m.findValue("55").orElseThrow());
    }

    @Test
    public void datasetPathsWithoutPackager() throws Exception {
        ISOMsg m = populated();
        ISODatasetField f104 = new ISODatasetField(104);
        f104.addDataset(new ISODataset(0x01, DatasetFormat.TLV).with(0xDF01, "CAFE".getBytes()));
        f104.addDataset(new ISODataset(0x71, DatasetFormat.DBM).with(2, "1234"));
        m.set(f104);

        assertArrayEquals(AC, (byte[]) m.findValue("55.0x9F26").orElseThrow());
        assertArrayEquals("CAFE".getBytes(), (byte[]) m.findValue("104.0x01.0xDF01").orElseThrow());
        assertEquals(Optional.of("1234"), m.findValue("104.0x71.2"));
        assertEquals(Optional.empty(), m.findValue("104.0xDF01"), "inferred no-envelope: no dataset 104");
        assertEquals(Optional.empty(), m.findValue("104.0x01.0xDF01.1"));
    }

    @Test
    public void nestedDatasetField() throws Exception {
        ISOMsg m = new ISOMsg("0100");
        ISOMsg f127 = new ISOMsg(127);
        ISODatasetField f55 = new ISODatasetField(55);
        f55.addDataset(new ISODataset(55, DatasetFormat.TLV).with(0x9F26, AC));
        f127.set(f55);
        m.set(f127);

        assertArrayEquals(AC, (byte[]) m.findValue("127.55.0x9F26").orElseThrow());
        assertArrayEquals(AC, (byte[]) m.findValue("127.55.55.0x9F26").orElseThrow(), "inferred envelope");

        // the sub-message's packager decides the layout of its dataset fields
        f127.setPackager(cmfv3());
        assertArrayEquals(AC, (byte[]) m.findValue("127.55.0x9F26").orElseThrow());
        assertEquals(Optional.empty(), m.findValue("127.55.55.0x9F26"));
    }

    @Test
    public void xmlRoundTrip() throws Exception {
        ISOMsg m = populated();
        m.setDatasetElement("104", 0x71, 2, "1234");
        m.setPackager(new XMLPackager());
        ISOMsg unpacked = new ISOMsg();
        unpacked.setPackager(new XMLPackager());
        unpacked.unpack(m.pack());

        assertInstanceOf(ISODatasetField.class, unpacked.getComponent(55));
        assertArrayEquals(AC, (byte[]) unpacked.findValue("55.0x9F26").orElseThrow());
        assertArrayEquals("1234".getBytes(), (byte[]) unpacked.findValue("104.0x71.2").orElseThrow());
        assertEquals(Optional.of("value63.2.3"), unpacked.findValue("63.2.3"));
    }

    @Test
    public void setWithoutPackagerCreatesPlainComposite() throws Exception {
        ISOMsg m = new ISOMsg("0100");
        m.set("55.0x9F26", AC);
        assertInstanceOf(ISOMsg.class, m.getComponent(55));
        assertArrayEquals(AC, (byte[]) m.findValue("55.0x9F26").orElseThrow());
        assertTrue(m.findDatasetElements("55.0x9F26").isEmpty());
    }

    // ---- repeats

    @Test
    public void repeatedTags() throws Exception {
        ISOMsg m = new ISOMsg("0100");
        ISODatasetField f55 = new ISODatasetField(55);
        ISODataset icc = new ISODataset(55, DatasetFormat.TLV).with(0x9F26, AC);
        icc.addElement(0x71, new ISOBinaryField(0x71, SCRIPT1), true);
        icc.addElement(0x71, new ISOBinaryField(0x71, SCRIPT2), true);
        f55.addDataset(icc);
        m.set(f55);
        m = roundTrip(cmfv3(), m);

        List<DatasetElement> scripts = m.findDatasetElements("55.0x71");
        assertEquals(2, scripts.size());
        assertArrayEquals(SCRIPT1, scripts.get(0).getBytes());
        assertArrayEquals(SCRIPT2, scripts.get(1).getBytes());
        assertTrue(scripts.get(0).isConstructed());
        assertTrue(scripts.get(1).isConstructed());
        assertArrayEquals(SCRIPT1, (byte[]) m.findValue("55.0x71").orElseThrow(), "first occurrence");
        assertThrows(UnsupportedOperationException.class, () -> scripts.add(scripts.get(0)));

        // constructed elements carry their encoded children
        TLVList children = new TLVList();
        children.unpack(scripts.get(1).getBytes());
        assertArrayEquals(ISOUtil.hex2byte("55667788"), children.find(0x9F18).getValue());
        assertArrayEquals(ISOUtil.hex2byte("8424000000"), children.find(0x86).getValue());
    }

    @Test
    public void repeatedDatasets() throws Exception {
        ISOMsg m = new ISOMsg("0100");
        ISODatasetField f104 = new ISODatasetField(104);
        f104.addDataset(new ISODataset(0x01, DatasetFormat.TLV).with(0xDF02, "A".getBytes()));
        f104.addDataset(new ISODataset(0x01, DatasetFormat.TLV).with(0xDF01, "B".getBytes()));
        f104.addDataset(new ISODataset(0x01, DatasetFormat.TLV).with(0xDF01, "C".getBytes()));
        m.set(f104);

        assertNull(f104.getValue(0x01, 0xDF01), "ISODatasetField only reads the first dataset");
        assertArrayEquals("B".getBytes(), (byte[]) m.findValue("104.0x01.0xDF01").orElseThrow());
        List<DatasetElement> elements = m.findDatasetElements("104.0x01.0xDF01");
        assertEquals(2, elements.size());
        assertArrayEquals("B".getBytes(), elements.get(0).getBytes());
        assertArrayEquals("C".getBytes(), elements.get(1).getBytes());
    }

    // ---- setDatasetElement / withDatasetElement / unsetDatasetElement

    @Test
    public void setDatasetElementWithoutPackager() throws Exception {
        ISOMsg m = new ISOMsg("0100")
          .withDatasetElement("55", 0x9F26, AC)
          .withDatasetElement("55", 0x71, SCRIPT1)
          .withDatasetElement("104", 0x01, 0xDF01, "CAFE".getBytes())
          .withDatasetElement("49", 0x71, 2, "1234")
          .withDatasetElement("49", 0x71, 1, new ISOField(99, "1"));

        ISODatasetField f55 = (ISODatasetField) m.getComponent(55);
        ISODataset icc = (ISODataset) f55.getDataset(55);
        assertEquals(DatasetFormat.TLV, icc.getFormat());
        assertFalse(icc.getElement(0x9F26).isConstructed());
        assertTrue(icc.getElement(0x71).isConstructed());

        ISODatasetField f49 = (ISODatasetField) m.getComponent(49);
        ISODataset verification = (ISODataset) f49.getDataset(0x71);
        assertEquals(DatasetFormat.DBM, verification.getFormat());
        assertEquals(1, verification.getComponent(1).getFieldNumber());
        assertFalse(verification.getElement(1).isConstructed(), "DBM elements are never constructed");
        assertEquals(DatasetFormat.TLV, ((ISODatasetField) m.getComponent(104)).getDataset(0x01).getFormat());

        // the result packs with a dataset-aware packager
        ISOMsg unpacked = roundTrip(cmfv3(), m);
        assertArrayEquals(AC, (byte[]) unpacked.findValue("55.0x9F26").orElseThrow());
        assertArrayEquals(SCRIPT1, (byte[]) unpacked.findValue("55.0x71").orElseThrow());
        assertArrayEquals("CAFE".getBytes(), (byte[]) unpacked.findValue("104.0x01.0xDF01").orElseThrow());
        assertEquals(Optional.of("1"), unpacked.findValue("49.0x71.1"));
        assertEquals(Optional.of("1234"), unpacked.findValue("49.0x71.2"));
    }

    @Test
    public void setDatasetElementReplacesAndCreatesHierarchy() throws Exception {
        ISOMsg m = new ISOMsg("0100");
        m.setDatasetElement("127.55", 0x9F26, AC);
        m.setDatasetElement("127.55", 0x9F26, IAD);
        assertInstanceOf(ISOMsg.class, m.getComponent(127));
        List<DatasetElement> elements = m.findDatasetElements("127.55.0x9F26");
        assertEquals(1, elements.size());
        assertArrayEquals(IAD, elements.get(0).getBytes());

        // a no-envelope field above 0x70 still gets a TLV dataset
        m.setDatasetElement("120", 0x9F26, AC);
        assertEquals(DatasetFormat.TLV, ((ISODatasetField) m.getComponent(120)).getDataset(120).getFormat());
    }

    @Test
    public void setDatasetElementErrors() throws Exception {
        ISOMsg m = populated();
        assertThrows(ISOException.class, () -> m.setDatasetElement("62", 0x9F26, AC), "leaf target");
        assertThrows(ISOException.class, () -> m.setDatasetElement("63", 0x9F26, AC), "composite target");
        assertThrows(ISOException.class, () -> m.setDatasetElement("62.55", 0x9F26, AC), "through a leaf");
        assertThrows(ISOException.class, () -> m.setDatasetElement("55.1", 0x9F26, AC), "through a dataset field");
        assertThrows(ISOException.class, () -> m.setDatasetElement("56", 0x9F26, 42), "unsupported value");
        assertThrows(IllegalArgumentException.class, () -> m.setDatasetElement("56", -1, AC));
        assertThrows(IllegalArgumentException.class, () -> m.setDatasetElement("104", -1, 1, AC));
        assertEquals("leaf62", m.getString(62));
        assertFalse(m.hasField(56));
    }

    @Test
    public void unsetDatasetElement() throws Exception {
        ISOMsg m = new ISOMsg("0100");
        ISODatasetField f104 = new ISODatasetField(104);
        f104.addDataset(new ISODataset(0x01, DatasetFormat.TLV).with(0xDF01, "A".getBytes()).with(0xDF02, "B".getBytes()));
        f104.addDataset(new ISODataset(0x01, DatasetFormat.TLV).with(0xDF01, "C".getBytes()));
        m.set(f104);

        m.unsetDatasetElement("104", 0x01, 0xDF01);
        assertTrue(m.findDatasetElements("104.0x01.0xDF01").isEmpty(), "every occurrence removed");
        assertEquals(1, f104.getDatasets().size(), "empty dataset removed");
        m.unsetDatasetElement("104", 0x01, 0xDF02);
        assertFalse(m.hasField(104), "field removed with its last dataset");

        m.setDatasetElement("55", 0x9F26, AC);
        m.setDatasetElement("55", 0x9F26, null);
        assertFalse(m.hasField(55), "null value removes");

        m.set(62, "leaf62");
        m.unsetDatasetElement("56", 0x9F26);
        m.unsetDatasetElement("64.55", 0x9F26);
        m.unsetDatasetElement("62.55", 0x9F26);
        assertEquals("leaf62", m.getString(62));
        assertThrows(ISOException.class, () -> m.unsetDatasetElement("62", 0x9F26));
    }

    // ---- set/unset by dataset path

    @Test
    public void mismatchedDatasetPathDoesNotReplaceField() throws Exception {
        ISOMsg m = new ISOMsg("0100");
        m.setPackager(cmfv3());
        m.set("104.0x01.0xDF01", "CAFE".getBytes());
        m.set("55.0x9F26", AC);

        assertThrows(IllegalArgumentException.class, () -> m.set("104.0xDF02", "BABE".getBytes()));
        assertThrows(IllegalArgumentException.class, () -> m.set("104.0x01.0xDF02.1", "BABE"));
        assertThrows(ISOException.class, () -> m.set("104.0xDF02", new ISOBinaryField(0, "BABE".getBytes())));
        assertThrows(IllegalArgumentException.class, () -> m.set("55.0x01.0x9F27", AC));
        assertThrows(IllegalArgumentException.class, () -> m.unset("104.0xDF01"));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> m.set("55.0x01.0x9F27", AC));
        assertEquals("Path '55.0x01.0x9F27' does not match the dataset layout of field 55 (expected 1 segment after the field)", e.getMessage());

        assertInstanceOf(ISODatasetField.class, m.getComponent(104));
        assertInstanceOf(ISODatasetField.class, m.getComponent(55));
        assertArrayEquals("CAFE".getBytes(), (byte[]) m.findValue("104.0x01.0xDF01").orElseThrow());
        assertArrayEquals(AC, (byte[]) m.findValue("55.0x9F26").orElseThrow());
    }

    @Test
    public void setByDatasetPathOnHighNoEnvelopeFieldUsesTLV() throws Exception {
        GenericPackager packager = cmfv3();
        packager.setFieldPackager(120, packager.getFieldPackager(55));
        ISOMsg m = new ISOMsg("0100");
        m.setPackager(packager);
        m.set("120.0x9F26", AC);
        m.set("120.0x71", SCRIPT1);
        ISODataset dataset = (ISODataset) ((ISODatasetField) m.getComponent(120)).getDataset(120);
        assertEquals(DatasetFormat.TLV, dataset.getFormat());
        assertTrue(dataset.getElement(0x71).isConstructed());
    }

    @Test
    public void unsetByDatasetPathRemovesFromRepeatedDatasets() throws Exception {
        ISOMsg m = new ISOMsg("0100");
        m.setPackager(cmfv3());
        ISODatasetField f104 = new ISODatasetField(104);
        f104.addDataset(new ISODataset(0x01, DatasetFormat.TLV).with(0xDF01, "A".getBytes()).with(0xDF02, "B".getBytes()));
        f104.addDataset(new ISODataset(0x01, DatasetFormat.TLV).with(0xDF01, "C".getBytes()));
        m.set(f104);

        m.unset("104.0x01.0xDF01");
        assertTrue(m.findDatasetElements("104.0x01.0xDF01").isEmpty());
        assertArrayEquals("B".getBytes(), (byte[]) m.findValue("104.0x01.0xDF02").orElseThrow());
    }

    // ---- clone

    @Test
    public void cloneDatasetElementPath() throws Exception {
        ISOMsg m = populated();
        ISOMsg c = m.clone("0", "55.0x9F26");

        assertEquals("0100", c.getMTI());
        ISODatasetField f55 = (ISODatasetField) c.getComponent(55);
        assertNotSame(m.getComponent(55), f55);
        ISODataset icc = (ISODataset) f55.getDataset(55);
        assertEquals(DatasetFormat.TLV, icc.getFormat());
        assertEquals(1, icc.getElements().size());
        byte[] cloned = icc.getBytes(0x9F26);
        assertArrayEquals(AC, cloned);

        // deep copy: the source element shares its array with the caller
        byte[] source = AC.clone();
        m.setDatasetElement("55", 0x9F26, source);
        c = m.clone("55.0x9F26");
        source[0] = 0;
        assertArrayEquals(AC, c.findValue("55.0x9F26").map(byte[].class::cast).orElseThrow());
    }

    @Test
    public void cloneDatasetElementsOrderAndOverlap() throws Exception {
        ISOMsg m = populated();

        ISOMsg c = m.clone("55.0x9F10", "55.0x9F26");
        List<DatasetElement> elements = ((ISODatasetField) c.getComponent(55)).getDataset(55).getElements();
        assertEquals(0x9F10, elements.get(0).getId());
        assertEquals(0x9F26, elements.get(1).getId());

        c = m.clone("55", "55.0x9F26", "55.0x9F26");
        assertEquals(1, c.findDatasetElements("55.0x9F26").size(), "overlapping paths replace");
        assertEquals(2, ((ISODatasetField) c.getComponent(55)).getDataset(55).getElements().size());

        c = m.clone("55.0x9F27", "62.1", "64.0x9F26");
        assertFalse(c.hasFields(), "absent paths are skipped");
    }

    @Test
    public void cloneRepeatedTagsAndDatasets() throws Exception {
        ISOMsg m = new ISOMsg("0100");
        ISODatasetField f55 = new ISODatasetField(55);
        ISODataset icc = new ISODataset(55, DatasetFormat.TLV).with(0x9F26, AC);
        icc.addElement(0x71, new ISOBinaryField(0x71, SCRIPT1), true);
        icc.addElement(0x71, new ISOBinaryField(0x71, SCRIPT2), true);
        f55.addDataset(icc);
        m.set(f55);
        ISODatasetField f104 = new ISODatasetField(104);
        f104.addDataset(new ISODataset(0x01, DatasetFormat.TLV).with(0xDF02, "A".getBytes()));
        f104.addDataset(new ISODataset(0x01, DatasetFormat.TLV).with(0xDF01, "B".getBytes()));
        f104.addDataset(new ISODataset(0x71, DatasetFormat.DBM).with(2, "1234"));
        m.set(f104);

        ISOMsg c = m.clone("55.0x71");
        List<DatasetElement> scripts = c.findDatasetElements("55.0x71");
        assertEquals(2, scripts.size());
        assertArrayEquals(SCRIPT1, scripts.get(0).getBytes());
        assertArrayEquals(SCRIPT2, scripts.get(1).getBytes());
        assertTrue(scripts.get(0).isConstructed() && scripts.get(1).isConstructed());

        c = m.clone("104.0x01.0xDF01");
        List<Dataset> datasets = ((ISODatasetField) c.getComponent(104)).getDatasets();
        assertEquals(1, datasets.size(), "dataset without the element is not kept");
        assertArrayEquals("B".getBytes(), datasets.get(0).getElement(0xDF01).getBytes());

        c = m.clone("104.0x01.0xDF01", "104.0x01.0xDF02", "104.0x71.2");
        datasets = ((ISODatasetField) c.getComponent(104)).getDatasets(0x01);
        assertEquals(2, datasets.size(), "same-identifier datasets stay apart");
        assertArrayEquals("A".getBytes(), datasets.get(0).getElement(0xDF02).getBytes());
        assertArrayEquals("B".getBytes(), datasets.get(1).getElement(0xDF01).getBytes());
        assertEquals(1, datasets.get(0).getElements().size());
        assertEquals(1, datasets.get(1).getElements().size());
        assertEquals(DatasetFormat.DBM, ((ISODatasetField) c.getComponent(104)).getDataset(0x71).getFormat());
        assertEquals(Optional.of("1234"), c.findValue("104.0x71.2"));
    }

    @Test
    public void cloneDatasetPathWithPackager() throws Exception {
        GenericPackager packager = cmfv3();
        ISOMsg m = roundTrip(packager, cmfMsg(packager)
          .with("55.0x9F26", AC)
          .with("55.0x9F10", IAD)
          .with("104.0x01.0xDF01", ISOUtil.hex2byte("CAFEBABE")));

        ISOMsg c = m.clone("0", "55.0x9F26", "104.0x01.0xDF01");
        ISOMsg unpacked = roundTrip(packager, c);
        assertArrayEquals(AC, (byte[]) unpacked.findValue("55.0x9F26").orElseThrow());
        assertEquals(Optional.empty(), unpacked.findValue("55.0x9F10"));
        assertArrayEquals(ISOUtil.hex2byte("CAFEBABE"), (byte[]) unpacked.findValue("104.0x01.0xDF01").orElseThrow());
    }

    @Test
    public void cloneNestedDatasetPath() throws Exception {
        ISOMsg m = new ISOMsg("0100");
        m.setDatasetElement("127.55", 0x9F26, AC);
        m.setDatasetElement("127.55", 0x9F10, IAD);
        m.set("127.2", "value127.2");

        ISOMsg c = m.clone("127.55.0x9F26");
        assertFalse(c.hasField("127.2"));
        assertArrayEquals(AC, (byte[]) c.findValue("127.55.0x9F26").orElseThrow());
        assertEquals(Optional.empty(), c.findValue("127.55.0x9F10"));
    }

    // ---- helpers

    private static ISOMsg populated() throws ISOException {
        ISOMsg m = new ISOMsg("0100");
        m.set(3, "000000");
        m.set(52, ISOUtil.hex2byte("CAFEBABE"));
        m.set(62, "leaf62");
        m.set("63.2.3", "value63.2.3");
        ISODatasetField f55 = new ISODatasetField(55);
        f55.addDataset(new ISODataset(55, DatasetFormat.TLV).with(0x9F26, AC).with(0x9F10, IAD));
        m.set(f55);
        return m;
    }

    private static GenericPackager cmfv3() throws ISOException {
        return new GenericPackager("jar:packager/cmfv3.xml");
    }

    private static ISOMsg cmfMsg(GenericPackager packager) {
        ISOMsg m = new ISOMsg("0100");
        m.setPackager(packager);
        return m;
    }

    private static ISOMsg roundTrip(GenericPackager packager, ISOMsg m) throws ISOException {
        m.setPackager(packager);
        ISOMsg unpacked = new ISOMsg();
        unpacked.setPackager(packager);
        unpacked.unpack(m.pack());
        return unpacked;
    }
}

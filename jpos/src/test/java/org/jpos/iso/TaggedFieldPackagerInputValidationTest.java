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

import org.jpos.iso.packager.TagMapper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaggedFieldPackagerInputValidationTest {
    @Test
    void rejectsTruncatedTagAtNonzeroOffset() {
        TestPackager packager = configuredPackager(2, "A1");

        assertThrows(ISOException.class,
          () -> packager.unpack(new ISOField(1), new byte[] { 'x', 'A' }, 1));
    }

    @Test
    void unpacksCompleteTagAtNonzeroOffset() throws ISOException {
        TestPackager packager = configuredPackager(2, "A1");
        ISOField field = new ISOField(1);

        assertEquals(4, packager.unpack(field, "xA11Zy".getBytes(ISOUtil.CHARSET), 1));
        assertEquals("Z", field.getValue());
    }

    @Test
    void lenientUnknownTagStillRequiresCompleteTag() throws ISOException {
        TestPackager packager = configuredPackager(2, "A1");
        packager.setUnpackingLenient(true);

        assertEquals(0, packager.unpack(new ISOField(1), "xZZ".getBytes(ISOUtil.CHARSET), 1));
        assertThrows(ISOException.class,
          () -> packager.unpack(new ISOField(1), new byte[] { 'x', 'Z' }, 1));
    }

    @Test
    void rejectsInvalidConfiguredTagLength() {
        TestPackager packager = configuredPackager(-1, "A1");

        assertThrows(ISOException.class,
          () -> packager.unpack(new ISOField(1), "A11Z".getBytes(ISOUtil.CHARSET), 0));
    }

    @Test
    void rejectsMappedTagWithWrongLength() {
        TestPackager packager = configuredPackager(2, "ABC");

        assertThrows(ISOException.class, () -> packager.pack(new ISOField(1, "Z")));
    }

    @Test
    void streamUnpackAcceptsBinaryFields() throws Exception {
        BinaryTestPackager packager = new BinaryTestPackager();
        packager.setParentFieldNumber(48);
        packager.setTagMapper(new TestTagMapper("A1"));
        ISOBinaryField field = new ISOBinaryField(1);

        packager.unpack(field, new ByteArrayInputStream(new byte[] { 'A', '1', '1', 0x7F }));

        assertArrayEquals(new byte[] { 0x7F }, (byte[]) field.getValue());
    }

    private static TestPackager configuredPackager(int tagLength, String mappedTag) {
        TestPackager packager = new TestPackager(tagLength);
        packager.setParentFieldNumber(48);
        packager.setTagMapper(new TestTagMapper(mappedTag));
        return packager;
    }

    private static class TestPackager extends TaggedFieldPackagerBase {
        private final int tagLength;

        TestPackager(int tagLength) {
            this.tagLength = tagLength;
            setLength(9);
        }

        @Override
        protected ISOFieldPackager getDelegate(int len, String description) {
            return new IFA_LCHAR(len, description);
        }

        @Override
        protected int getTagNameLength() {
            return tagLength;
        }
    }

    private static class BinaryTestPackager extends TaggedFieldPackagerBase {
        BinaryTestPackager() {
            setLength(9);
        }

        @Override
        protected ISOFieldPackager getDelegate(int len, String description) {
            return new IFA_LBINARY(len, description);
        }

        @Override
        protected int getTagNameLength() {
            return 2;
        }
    }

    private static class TestTagMapper implements TagMapper {
        private final String mappedTag;

        TestTagMapper(String mappedTag) {
            this.mappedTag = mappedTag;
        }

        @Override
        public String getTagForField(int fieldNumber, int subFieldNumber) {
            return mappedTag;
        }

        @Override
        public Integer getFieldNumberForTag(int fieldNumber, String tag) {
            return mappedTag.equals(tag) ? 1 : null;
        }
    }
}

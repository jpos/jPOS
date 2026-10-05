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

package org.jpos.tlv.packager;

import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOFieldPackager;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.TaggedFieldPackager;
import org.jpos.tlv.ISOTaggedField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TaggedSequencePackagerConsumptionTest {
    @Test
    public void rejectsZeroTaggedFieldConsumption() throws ISOException {
        TaggedSequencePackager packager = packager(new ControlledTaggedPackager(0));

        ISOException e = assertThrows(ISOException.class,
                () -> packager.unpack(new ISOMsg(), new byte[] {'A'}));

        assertTrue(e.getMessage().contains("Invalid tagged field consumption: 0"));
    }

    @Test
    public void rejectsNegativeTaggedFieldConsumption() throws ISOException {
        TaggedSequencePackager packager = packager(new ControlledTaggedPackager(-1));

        ISOException e = assertThrows(ISOException.class,
                () -> packager.unpack(new ISOMsg(), new byte[] {'A'}));

        assertTrue(e.getMessage().contains("Invalid tagged field consumption: -1"));
    }

    @Test
    public void rejectsTaggedFieldConsumptionBeyondRemainingInput() throws ISOException {
        TaggedSequencePackager packager = packager(new ControlledTaggedPackager(Integer.MAX_VALUE));

        ISOException e = assertThrows(ISOException.class,
                () -> packager.unpack(new ISOMsg(), new byte[] {'A'}));

        assertTrue(e.getMessage().contains("exceeds remaining input 1"));
    }

    @Test
    public void acceptsTaggedFieldConsumptionAtExactBoundary() throws ISOException {
        TaggedSequencePackager packager = packager(new ControlledTaggedPackager(3));
        ISOMsg msg = new ISOMsg();

        assertEquals(3, packager.unpack(msg, new byte[] {'A', 'B', 'C'}));
        assertTrue(msg.hasField(1));
    }

    @Test
    public void permitsOptionalPrefixWithZeroConsumption() throws ISOException {
        ControlledTaggedPackager tagged = new ControlledTaggedPackager(1);
        TaggedSequencePackager packager = packager(new ControlledPrefixPackager(0), tagged);
        ISOMsg msg = new ISOMsg();

        assertEquals(1, packager.unpack(msg, new byte[] {'A'}));
        assertTrue(msg.hasField(1));
    }

    @Test
    public void rejectsNegativePrefixConsumption() throws ISOException {
        TaggedSequencePackager packager = packager(
                new ControlledPrefixPackager(-1), new ControlledTaggedPackager(1));

        ISOException e = assertThrows(ISOException.class,
                () -> packager.unpack(new ISOMsg(), new byte[] {'A'}));

        assertTrue(e.getMessage().contains("Invalid prefix field consumption: -1"));
    }

    @Test
    public void rejectsPrefixConsumptionBeyondRemainingInput() throws ISOException {
        TaggedSequencePackager packager = packager(
                new ControlledPrefixPackager(Integer.MAX_VALUE), new ControlledTaggedPackager(1));

        ISOException e = assertThrows(ISOException.class,
                () -> packager.unpack(new ISOMsg(), new byte[] {'A'}));

        assertTrue(e.getMessage().contains("exceeds remaining input 1"));
    }

    private static TaggedSequencePackager packager(ISOFieldPackager... fields) throws ISOException {
        TaggedSequencePackager packager = new TaggedSequencePackager();
        packager.setToken("X");
        packager.setFieldPackager(fields);
        return packager;
    }

    private static class ControlledTaggedPackager extends ISOFieldPackager implements TaggedFieldPackager {
        private final int consumption;
        private String token = "A";

        private ControlledTaggedPackager(int consumption) {
            this.consumption = consumption;
        }

        @Override
        public int getMaxPackedLength() {
            return 0;
        }

        @Override
        public byte[] pack(ISOComponent c) {
            return new byte[0];
        }

        @Override
        public int unpack(ISOComponent c, byte[] b, int offset) throws ISOException {
            c.setValue("value");
            return consumption;
        }

        @Override
        public ISOComponent createComponent(int fieldNumber) {
            return new ISOTaggedField(token, new ISOField(fieldNumber));
        }

        @Override
        public void setToken(String token) {
            this.token = token;
        }

        @Override
        public String getToken() {
            return token;
        }
    }

    private static class ControlledPrefixPackager extends ISOFieldPackager {
        private final int consumption;

        private ControlledPrefixPackager(int consumption) {
            this.consumption = consumption;
        }

        @Override
        public int getMaxPackedLength() {
            return 0;
        }

        @Override
        public byte[] pack(ISOComponent c) {
            return new byte[0];
        }

        @Override
        public int unpack(ISOComponent c, byte[] b, int offset) {
            return consumption;
        }
    }
}

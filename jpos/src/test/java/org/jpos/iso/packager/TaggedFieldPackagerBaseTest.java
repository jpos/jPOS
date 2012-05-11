/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

package org.jpos.iso.packager;

import junit.framework.TestCase;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOMsg;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class TaggedFieldPackagerBaseTest extends TestCase {

    @Test
    public void testPack() throws Exception {
        String path = "target/test-classes/org/jpos/iso/packagers/";
        GenericPackager genericPackager = new GenericPackager(new FileInputStream(path + "ISO93TLVPackager.xml"));

        ISOMsg msg = new ISOMsg();
        msg.setMTI("1100");
        msg.set(new ISOField(2, "123456"));

        ISOMsg subFieldsContainer = new ISOMsg(48);
        ISOField tlvField = new ISOField(1);
        tlvField.setValue("48TagA1");
        subFieldsContainer.set(tlvField);

        ISOField tlvField2 = new ISOField(3);
        tlvField2.setValue("48TagA3");
        subFieldsContainer.set(tlvField2);

        msg.set(subFieldsContainer);

        ISOMsg subFieldsContainer2 = new ISOMsg(60);
        ISOField tlvField3 = new ISOField(1);
        tlvField3.setValue("60TagA1");
        subFieldsContainer2.set(tlvField3);

        msg.set(subFieldsContainer2);

        msg.setHeader("HEADER   ".getBytes());
        msg.setPackager(genericPackager);
        byte[] packed = msg.pack();
        assertNotNull(packed);

        FileOutputStream fos = new FileOutputStream(path + "ISO93TLVPackager.bin");
        fos.write(packed);
        fos.close();
    }


    @Test
    public void testUnpack() throws Exception {
        String path = "target/test-classes/org/jpos/iso/packagers/";
        GenericPackager genericPackager = new GenericPackager(new FileInputStream(path + "ISO93TLVPackager.xml"));

        ISOMsg msg = new ISOMsg();
        genericPackager.unpack(msg, new FileInputStream(path + "ISO93TLVPackager.bin"));

        assertEquals("1100", msg.getMTI());
        assertEquals("48TagA1", ((ISOField) ((ISOMsg) msg.getComponent(48)).getComponent(1)).getValue());
    }

    public static class TagMapperImpl implements TagMapper {

        private static Map<String, Integer> tagToNumberMap = new HashMap<String, Integer>();
        private static Map<String, String> numberToTagMap = new HashMap<String, String>();

        static {
            tagToNumberMap.put("48.A1", 1);
            numberToTagMap.put("48.1", "A1");

            tagToNumberMap.put("48.A2", 2);
            numberToTagMap.put("48.2", "A2");

            tagToNumberMap.put("48.A3", 3);
            numberToTagMap.put("48.3", "A3");

            tagToNumberMap.put("60.A1", 1);
            numberToTagMap.put("60.1", "A1");
        }

        public TagMapperImpl() {
        }

        public String getTagForField(int fieldNumber, int subFieldNumber) {
            return numberToTagMap.get(fieldNumber + "." + subFieldNumber);
        }

        public Integer getFieldNumberForTag(int fieldNumber, String tag) {
            return tagToNumberMap.get(fieldNumber + "." + tag);
        }
    }
}

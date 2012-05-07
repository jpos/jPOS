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

package org.jpos.iso;

import junit.framework.TestCase;
import org.jpos.iso.packager.GenericPackager;

import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * @author Vishnu Pillai
 */
public class TLVFieldPackagerTest extends TestCase {
    public void testPack() throws Exception {
        String path = "target/test-classes/org/jpos/iso/packagers/";
        GenericPackager genericPackager = new GenericPackager(new FileInputStream(path + "ISO93TLVPackager.xml"));

        ISOMsg msg = new ISOMsg();
        msg.setMTI("1100");
        msg.set(new ISOField(2, "123456"));

        ISOMsg subFieldsContainer = new ISOMsg(48);
        subFieldsContainer.set(new TLVField(1, "A1", "A1A1A1"));

        msg.set(subFieldsContainer);

        msg.setHeader("HEADER   ".getBytes());
        msg.recalcBitMap();
        byte[] packed = genericPackager.pack(msg);
        assertNotNull(packed);
    }


    public void testUnpack() throws Exception {
        String path = "target/test-classes/org/jpos/iso/packagers/";
        GenericPackager genericPackager = new GenericPackager(new FileInputStream(path + "ISO93TLVPackager.xml"));

        ISOMsg msg = new ISOMsg();
        genericPackager.unpack(msg, new FileInputStream(path + "ISO93TLVPackager.bin"));

        assertEquals("1100", msg.getMTI());
        assertEquals("A1", ((TLVField)((ISOMsg) msg.getComponent(48)).getComponent(1)).getTagName());
        assertEquals("A1A1A1", ((TLVField)((ISOMsg) msg.getComponent(48)).getComponent(1)).getValue());
    }
}

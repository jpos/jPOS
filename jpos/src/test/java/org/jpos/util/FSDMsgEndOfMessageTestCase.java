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

package org.jpos.util;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

public class FSDMsgEndOfMessageTestCase extends TestCase {
    private static final String SCHEMA_DIR_URL = "file:target/test-classes/org/jpos/util/";
    FSDMsg imsg;

    FSDMsg omsg;

    public void setUp() throws Exception {
        imsg = new FSDMsg(SCHEMA_DIR_URL + "eom-");
        omsg = new FSDMsg(SCHEMA_DIR_URL + "eom-");
    }

    public void testPack() throws Exception {
        imsg.set("length", "11");
        imsg.set("rest", "ABCDEFGHIJKLMNOPQRST");
        assertEquals("11ABCDEFGHIJKLMNOPQRST", imsg.pack());
    }

    public void testUnpack() throws Exception {
    	ByteArrayInputStream is = new ByteArrayInputStream("11ABCDEFGHIJKLMNOPQRST".getBytes());
    	omsg.unpack(is);

        assertEquals("11", omsg.get("length"));
        assertEquals("ABCDEFGHIJKLMNOPQRST", omsg.get("rest"));
    }
}

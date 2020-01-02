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

package org.jpos.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FSDMsgPropertiesTestCase {
    private static final String SCHEMA_DIR_URL = "file:build/resources/test/org/jpos/util/";
    private static String FS = "\034";
    private FSDMsg imsg;
    private FSDMsg omsg;

    @BeforeEach
    public void setUp() throws Exception {
        imsg = new FSDMsg(SCHEMA_DIR_URL + "msg-prop-");
        omsg = new FSDMsg(SCHEMA_DIR_URL + "msg-prop-");
    }

    @Test
    public void testSeparator() throws Exception {
        imsg.set("testafs", "x");
        imsg.set("b", "y");
        assertEquals("x'FS'y", imsg.pack().replace(FS, "'FS'"), "properties");

        omsg.unpack(imsg.pack().getBytes());

        assertEquals("x", omsg.get("testafs"));
        assertEquals("y", omsg.get("b"));
    }
}

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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LogUtilTest {
    @Test
    public void testNeedsCDATAjson() {
        String json = "{\"amount\": 123}";
        assertFalse(LogUtil.needsCDATA(json));
    }

    @Test
    public void testNeedsCDATAxml() {
        String xml = "<tag>hello</tag>";
        assertTrue(LogUtil.needsCDATA(xml));
    }

    @Test
    public void testNeedsCDATAapos() {
        String apostrophe = "Rock'n roll";
        assertFalse(LogUtil.needsCDATA(apostrophe));
    }

    @Test
    public void testNeedsCDATAamp() {
        String amp = "This & that";
        assertTrue(LogUtil.needsCDATA(amp));
    }

    @Test
    public void testNeedsCDATA() {
        String normal = "The quick brown fox jumps over the lazy dog";
        assertFalse(LogUtil.needsCDATA(normal));
    }
}

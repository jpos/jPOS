/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2021 jPOS Software SRL
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

package org.jpos.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.tlv.TLVList;
import org.junit.jupiter.api.Test;

public final class ProtectDebugInfoTest {

    @Test
    void test_wipe_tag() throws ConfigurationException {

        TLVList tlv = new TLVList();
        tlv.append(0x54, "AABBCCDDEEFF");
        tlv.append(0x55, "BBDDFFEEDDAA");

        SimpleConfiguration cfg = new SimpleConfiguration();
        cfg.put("protect-entry", "TEST_ENTRY");
        cfg.put("wipe-TLVList", "0x54");

        ProtectDebugInfo participant = new ProtectDebugInfo();
        participant.setConfiguration(cfg);

        Context ctx = new Context();
        ctx.put("TEST_ENTRY", tlv);

        participant.commit(0, ctx);

        ctx.dump(System.out, " ");
        assertNotEquals("AABBCCDDEEFF", tlv.getString(0x54));
        assertEquals("BBDDFFEEDDAA", tlv.getString(0x55));
    }
}
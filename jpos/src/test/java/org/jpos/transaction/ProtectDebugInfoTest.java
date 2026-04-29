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

package org.jpos.transaction;

import static org.junit.jupiter.api.Assertions.*;

import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.tlv.TLVList;
import org.jpos.util.ProtectedLogListener;
import org.junit.jupiter.api.Test;

public final class ProtectDebugInfoTest {

    @Test
    void test_wipe_tag() throws ConfigurationException {
        TLVList tlv = new TLVList();
        tlv.append(0x54, "AABBCCDDEEFF");
        tlv.append(0x55, "BBDDFFEEDDAA");
        tlv.append(0x56, "112233445566");

        SimpleConfiguration cfg = new SimpleConfiguration();
        cfg.put("protect-entry", "TEST_ENTRY");
        cfg.put("wipe-TLVList", new String[] {  "0x54,  ",
                                                ",,0x56, " });  // noisy junk separators

        ProtectDebugInfo participant = new ProtectDebugInfo();
        participant.setConfiguration(cfg);

        Context ctx = new Context();
        ctx.put("TEST_ENTRY", tlv);

        participant.commit(0, ctx);

        assertNotEquals("AABBCCDDEEFF", tlv.getString(0x54));
        assertEquals("BBDDFFEEDDAA", tlv.getString(0x55));
        assertNotEquals("112233445566", tlv.getString(0x56));
    }

    @Test
    void test_protect_and_wipe_iso_fields() throws ConfigurationException {
        ISOMsg original = new ISOMsg();
        original.set(2, "4761739001010119");
        original.set(8, "12345678");
        original.set(13, "0427");
        original.set(38, "ABC123");
        original.set(52, "A1B2C3D4E5F60708");
        original.set(55, ISOUtil.hex2byte("11223344"));
        original.set(70, "001");

        SimpleConfiguration cfg = new SimpleConfiguration();
        cfg.put("protect-entry", "REQUEST");
        cfg.put("protect-ISOMsg", new String[] { "2,", "8,,  38" });    // noisy junk separators
        cfg.put("wipe-ISOMsg", "52,   55,,");

        ProtectDebugInfo participant = new ProtectDebugInfo();
        participant.setConfiguration(cfg);

        Context ctx = new Context();
        ctx.put("REQUEST", original.clone());

        participant.commit(0, ctx);

        ISOMsg protectedMsg = ctx.get("REQUEST");

        // untouched fields
        assertEquals(original.getString(13), protectedMsg.getString(13));
        assertEquals(original.getString(70), protectedMsg.getString(70));

        // protected/wiped fields
        assertEquals(ISOUtil.protect(original.getString(2)),  protectedMsg.getString(2));
        assertEquals(ISOUtil.protect(original.getString(8)),  protectedMsg.getString(8));
        assertEquals(ISOUtil.protect(original.getString(38)), protectedMsg.getString(38));
        assertEquals(ProtectedLogListener.WIPED, protectedMsg.getString(52));
        assertArrayEquals(ProtectedLogListener.BINARY_WIPED, (byte[]) protectedMsg.getValue(55));
    }

    @Test
    void test_protect_and_wipe_entries() throws ConfigurationException {
        SimpleConfiguration cfg = new SimpleConfiguration();
        cfg.put("protect-entry", new String[] { "PAN,   TRACK2", "EXPDATE,,," });
        cfg.put("wipe-entry", "SECRET,  , TEMP");

        ProtectDebugInfo participant = new ProtectDebugInfo();
        participant.setConfiguration(cfg);

        Context ctx = new Context();
        ctx.put("PAN", "4761739001010119");
        ctx.put("TRACK2", "4761739001010119=29041010000012345678");
        ctx.put("EXPDATE", "2904");
        ctx.put("SECRET", "very-secret");
        ctx.put("TEMP", "remove-me");
        ctx.put("SAFE", "untouched");

        participant.commit(0, ctx);

        assertEquals(ISOUtil.protect("4761739001010119"), ctx.get("PAN"));
        assertEquals(ISOUtil.protect("4761739001010119=29041010000012345678"), ctx.get("TRACK2"));
        assertEquals(ISOUtil.protect("2904"), ctx.get("EXPDATE"));
        assertFalse(ctx.hasKey("SECRET"));
        assertFalse(ctx.hasKey("TEMP"));
        assertEquals("untouched", ctx.get("SAFE"));
    }
}

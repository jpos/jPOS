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

package org.jpos.transaction.participant;


import org.jpos.core.Configuration;
import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.ISOAmount;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.rc.CMF;
import org.jpos.rc.Result;
import org.jpos.transaction.Context;
import org.jpos.transaction.ContextConstants;
import org.jpos.transaction.TransactionConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class CheckFieldsTest implements TransactionConstants {
    private CheckFields cf;
    private Configuration cfg;

    @BeforeEach
    public void setUp() {
        cfg = new SimpleConfiguration();
        cf = new CheckFields();
        cf.setConfiguration(cfg);
    }

    @Test
    public void testInvalidTransaction () {
        Context ctx = new Context();
        int action = cf.prepare(1L, ctx);
        assertEquals (ABORTED | NO_JOIN | READONLY, action, "Action should be ABORTED|NO_JOIN|READONLY");
        Result rc = ctx.getResult();
        assertTrue(rc.hasFailures(), "RC should have failures");
        assertEquals(CMF.INVALID_TRANSACTION, rc.failure().getIrc(), "IRC should be INVALID_TRANSACTION");
    }

    @Test
    public void testMandatoryPCodeNotPresent () {
        Context ctx = new Context();
        ISOMsg m = new ISOMsg();
        ctx.put(ContextConstants.REQUEST.toString(), m);
        cfg.put("mandatory", "3");
        int action = cf.prepare(1L, ctx);
        assertEquals (ABORTED | NO_JOIN | READONLY, action, "Action should be ABORTED|NO_JOIN|READONLY");
        Result rc = ctx.getResult();
        assertFalse(rc.hasInfo(), "RC should have no info");
        assertFalse(rc.hasWarnings(), "RC should have no warnings");
        assertTrue(rc.hasFailures(), "RC should have failures");
        assertEquals(CMF.MISSING_FIELD, rc.failure().getIrc(), "IRC should be MISSING_FIELD");
    }
    @Test
    public void testOptionalPCodeNotPresent () {
        Context ctx = new Context();
        ISOMsg m = new ISOMsg();
        ctx.put(ContextConstants.REQUEST.toString(), m);
        int action = cf.prepare(1L, ctx);
        assertEquals (PREPARED | NO_JOIN | READONLY, action, "Action should be PREPARED|NO_JOIN|READONLY");
        Result rc = ctx.getResult();
        assertFalse(rc.hasInfo(), "RC should have no info");
        assertFalse(rc.hasWarnings(), "RC should have no warnings");
        assertFalse(rc.hasFailures(), "RC should have no failures");
    }

    @Test
    public void testNoExtraFields () {
        Context ctx = new Context();
        ISOMsg m = new ISOMsg();
        m.set(3, "000000");
        m.set(11, "000001");
        ctx.put(ContextConstants.REQUEST.toString(), m);
        cfg.put("mandatory", "11");
        cfg.put("optional", "3");
        int action = cf.prepare(1L, ctx);
        assertEquals (PREPARED | NO_JOIN | READONLY, action, "Action should be PREPARED|NO_JOIN|READONLY");
        Result rc = ctx.getResult();
        assertFalse(rc.hasInfo(), "RC should have no info");
        assertFalse(rc.hasWarnings(), "RC should have no warnings");
        assertFalse(rc.hasFailures(), "RC should have no failures");
    }

    @Test
    public void testExtraFields () {
        Context ctx = new Context();
        ISOMsg m = new ISOMsg();
        m.set(3, "000000");
        m.set(11, "000001");
        m.set(41, "29110001");
        m.set(42, "000000000000001");
        ctx.put(ContextConstants.REQUEST.toString(), m);
        cfg.put("mandatory", "11");
        cfg.put("optional", "3");
        int action = cf.prepare(1L, ctx);
        assertEquals (ABORTED | NO_JOIN | READONLY, action, "Action should be ABORTED|NO_JOIN|READONLY");
        Result rc = ctx.getResult();
        assertEquals(CMF.EXTRA_FIELD, rc.failure().getIrc());
        assertFalse(rc.hasInfo());
        assertFalse(rc.hasWarnings());
        assertTrue(rc.hasFailures());
        assertEquals("41 42", rc.failure().getMessage());
    }

    @Test
    public void testInvalidPCode () {
        Context ctx = new Context();
        ISOMsg m = new ISOMsg();
        m.set(3, "*00000");
        m.set(11, "000001");
        ctx.put(ContextConstants.REQUEST.toString(), m);
        cfg.put("optional", "PCODE");
        int action = cf.prepare(1L, ctx);
        assertEquals (ABORTED | NO_JOIN | READONLY, action, "Action should be ABORTED|NO_JOIN|READONLY");
        Result rc = ctx.getResult();
        assertEquals(CMF.INVALID_FIELD, rc.failure().getIrc());
        assertFalse(rc.hasInfo());
        assertFalse(rc.hasWarnings());
        assertTrue(rc.hasFailures());
        assertTrue(rc.failure().getMessage().startsWith("Invalid PCODE"));
    }

    @Test
    public void testValidCard () {
        Context ctx = new Context();
        ISOMsg m = new ISOMsg();
        m.set(2, "4111111111111111");
        m.set(14, "2912");
        m.set(35, "4111111111111111=2912");
        m.set(45, "B4111111111111111^TEST CARDHOLDER^29120000000000000000");

        ctx.put(ContextConstants.REQUEST.toString(), m);
        cfg.put("mandatory", "CARD");
        int action = cf.prepare(1L, ctx);
        assertEquals(PREPARED | NO_JOIN | READONLY, action);
    }
    @Test
    public void testInvalidCard () {
        Context ctx = new Context();
        ISOMsg m = new ISOMsg();
        m.set(2, "4111111111111111");
        m.set(14, "2912");
        m.set(35, "4111111111111112=2912");

        ctx.put(ContextConstants.REQUEST.toString(), m);
        cfg.put("mandatory", "CARD");
        int action = cf.prepare(1L, ctx);
        assertEquals(ABORTED | NO_JOIN | READONLY, action);
        Result rc = ctx.getResult();
        assertEquals(CMF.INVALID_CARD_NUMBER, rc.failure().getIrc());
        assertFalse(rc.hasInfo());
        assertFalse(rc.hasWarnings());
        assertTrue(rc.hasFailures());
        assertEquals("track2 PAN mismatch", rc.failure().getMessage());
    }

    @Test
    public void testInvalidMidAndTid () {
        Context ctx = new Context();
        ISOMsg m = new ISOMsg();
        m.set(41, "*1");
        m.set(42, "*2");
        ctx.put(ContextConstants.REQUEST.toString(), m);
        cfg.put("mandatory", "MID, TID");
        int action = cf.prepare(1L, ctx);
        assertEquals(ABORTED | NO_JOIN | READONLY, action);
        Result rc = ctx.getResult();
        assertEquals(CMF.INVALID_FIELD, rc.failure().getIrc());
        assertFalse(rc.hasInfo());
        assertFalse(rc.hasWarnings());
        assertTrue(rc.hasFailures());
        assertEquals(2, rc.failureList().size());
    }

    @Test
    public void testMidAndTidWithSpaces () {
        Context ctx = new Context();
        ISOMsg m = new ISOMsg();
        m.set(41, "0001    ");
        m.set(42, "0000000001     ");
        ctx.put(ContextConstants.REQUEST.toString(), m);
        cfg.put("mandatory", "MID, TID");
        int action = cf.prepare(1L, ctx);
        assertEquals(PREPARED | NO_JOIN | READONLY, action);
        Result rc = ctx.getResult();
        assertFalse(rc.hasFailures());
    }

    @Test
    public void testValidTimestamps () {
        Date now = new Date();
        Context ctx = new Context();
        ISOMsg m = new ISOMsg();
        m.set(7, ISODate.getDateTime(now));
        m.set(12, ISODate.getDateTime(now));
        ctx.put(ContextConstants.REQUEST.toString(), m);
        cfg.put("mandatory", "7 12");
        int action = cf.prepare(1L, ctx);
        assertEquals(PREPARED | NO_JOIN | READONLY, action);
        Result rc = ctx.getResult();
        assertFalse(rc.hasInfo());
        assertFalse(rc.hasWarnings());
        assertFalse(rc.hasFailures());
    }

    @Test
    public void testInvalidTimestamp () {
        Date now = new Date();
        Context ctx = new Context();
        ISOMsg m = new ISOMsg();
        m.set(7, ISODate.getDateTime(now));
        m.set(12, "*");
        ctx.put(ContextConstants.REQUEST.toString(), m);
        cfg.put("mandatory", "TRANSMISSION_TIMESTAMP TRANSACTION_TIMESTAMP");
        int action = cf.prepare(1L, ctx);
        // ctx.dump (System.out, "");
        assertEquals(ABORTED | NO_JOIN | READONLY, action);
        Result rc = ctx.getResult();
        assertFalse(rc.hasInfo());
        assertFalse(rc.hasWarnings());
        assertTrue(rc.hasFailures());
        assertEquals(1, rc.failureList().size());
        assertEquals(CMF.INVALID_FIELD, rc.failure().getIrc());
        assertEquals("Invalid TRANSACTION_TIMESTAMP '*'", rc.failure().getMessage());
    }

    @Test
    public void testGenericFields () {
        Context ctx = new Context();
        ISOMsg m = new ISOMsg();
        m.set(48, "Mandatory field 48");
        m.set(60, "Optional field");
        ctx.put(ContextConstants.REQUEST.toString(), m);
        cfg.put("mandatory", "48");
        cfg.put("optional", "60");
        int action = cf.prepare(1L, ctx);
        assertEquals(PREPARED | NO_JOIN | READONLY, action);
        Result rc = ctx.getResult();
        assertFalse(rc.hasInfo());
        assertFalse(rc.hasWarnings());
        assertFalse(rc.hasFailures());
    }

    @Test
    public void testGenericFieldsOptionalNotPresent () {
        Context ctx = new Context();
        ISOMsg m = new ISOMsg();
        m.set(48, "Mandatory field 48");
        ctx.put(ContextConstants.REQUEST.toString(), m);
        cfg.put("mandatory", "48");
        cfg.put("optional", "60");
        int action = cf.prepare(1L, ctx);
        assertEquals(PREPARED | NO_JOIN | READONLY, action);
        Result rc = ctx.getResult();
        assertFalse(rc.hasInfo());
        assertFalse(rc.hasWarnings());
        assertFalse(rc.hasFailures());
    }

    @Test
    public void testGenericFieldsExtraPresent () {
        Context ctx = new Context();
        ISOMsg m = new ISOMsg();
        m.set(48, "Mandatory field 48");
        m.set(60, "Extra field 60");
        ctx.put(ContextConstants.REQUEST.toString(), m);
        cfg.put("mandatory", "48");
        int action = cf.prepare(1L, ctx);
        // ctx.dump (System.out, "");
        assertEquals(ABORTED | NO_JOIN | READONLY, action);
        Result rc = ctx.getResult();
        assertFalse(rc.hasInfo());
        assertFalse(rc.hasWarnings());
        assertTrue(rc.hasFailures());
        assertEquals(CMF.EXTRA_FIELD, rc.failure().getIrc());
    }

    @Test
    public void testPDC () {
        Context ctx = new Context();
        ISOMsg m = new ISOMsg();
        m.set(22, new byte[16]);
        ctx.put(ContextConstants.REQUEST.toString(), m);
        cfg.put("mandatory", "POS_DATA_CODE");
        int action = cf.prepare(1L, ctx);
        assertEquals(PREPARED | NO_JOIN | READONLY, action);
        Result rc = ctx.getResult();
        assertFalse(rc.hasInfo());
        assertFalse(rc.hasWarnings());
        assertFalse(rc.hasFailures());
    }

    @Test
    public void testCaptureDate () {
        Context ctx = new Context();
        ISOMsg m = new ISOMsg();
        m.set(17, "0101");
        ctx.put(ContextConstants.REQUEST.toString(), m);
        cfg.put("mandatory", "CAPTURE_DATE");
        int action = cf.prepare(1L, ctx);
        assertEquals(PREPARED | NO_JOIN | READONLY, action);
        Result rc = ctx.getResult();
        assertFalse(rc.hasInfo());
        assertFalse(rc.hasWarnings());
        assertFalse(rc.hasFailures());
    }
    @Test
    public void testAmounts () throws ISOException {
        Context ctx = new Context();
        ISOMsg m = new ISOMsg();
        ISOAmount local = new ISOAmount(4, 858, new BigDecimal("290.00"));
        ISOAmount settlement = new ISOAmount (5, 840, new BigDecimal("10.00"));
        m.set(local);
        m.set(settlement);

        ctx.put(ContextConstants.REQUEST.toString(), m);
        cfg.put("mandatory", "AMOUNT");
        int action = cf.prepare(1L, ctx);
        assertEquals(PREPARED | NO_JOIN | READONLY, action);
        Result rc = ctx.getResult();
        assertFalse(rc.hasInfo());
        assertFalse(rc.hasWarnings());
        assertFalse(rc.hasFailures());
        assertEquals(local, ctx.get(ContextConstants.LOCAL_AMOUNT.toString()));
        assertEquals(settlement, ctx.get(ContextConstants.AMOUNT.toString()));
    }

    @Test
    public void testOriginalDataElements () throws ISOException {
        Context ctx = new Context();
        ISOMsg m = new ISOMsg();
        Date now = new Date();
        m.set(56, "0200000000000001" + ISODate.formatDate(now, "yyyyMMddHHmmss"));
        ctx.put(ContextConstants.REQUEST.toString(), m);
        cfg.put("mandatory", "ORIGINAL_DATA_ELEMENTS");
        int action = cf.prepare(1L, ctx);
        assertEquals(PREPARED | NO_JOIN | READONLY, action);
        Result rc = ctx.getResult();
        assertFalse(rc.hasInfo());
        assertFalse(rc.hasWarnings());
        assertFalse(rc.hasFailures());
        assertEquals("0200", ctx.get(ContextConstants.ORIGINAL_MTI.toString()));
        assertEquals("000000000001", ctx.get(ContextConstants.ORIGINAL_STAN.toString()));
        assertEquals(now.getTime()/1000, ((Date) ctx.get(ContextConstants.ORIGINAL_TIMESTAMP.toString())).getTime()/1000);
    }
}

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
import org.jpos.iso.*;
import org.jpos.rc.CMF;
import org.jpos.transaction.Context;
import org.jpos.transaction.ContextConstants;
import org.jpos.transaction.TransactionConstants;
import org.jpos.util.NameRegistrar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class QueryHostTest implements TransactionConstants, MUX {
    private QueryHost queryHost;
    private Configuration cfg;
    private long stan;

    @BeforeEach
    public void setUp() throws Exception {
        cfg = new SimpleConfiguration();
        queryHost = new QueryHost();
        queryHost.setConfiguration(cfg);
        NameRegistrar.register("mux.TEST", this);
    }

    @Test
    public void testSimpleQueryAsync() throws Exception {
        Context ctx = new Context();
        cfg.put("continuations", "yes");
        queryHost.setConfiguration(cfg);
        ctx.put(ContextConstants.REQUEST.toString(), createDummyRequest());
        ctx.put(ContextConstants.DESTINATION.toString(), "TEST");
        int action = queryHost.prepare(1L, ctx);
        assertTrue(action == (PREPARED | READONLY | PAUSE | NO_JOIN));
        assertNotNull (ctx.get(ContextConstants.RESPONSE.toString(), 1000));
        assertFalse (ctx.getResult().hasFailures(), "Should not have failures");
    }

    @Test
    public void testSimpleQuerySync() throws Exception {
        Context ctx = new Context();
        cfg.put("continuations", "no");
        queryHost.setConfiguration(cfg);
        ctx.put(ContextConstants.REQUEST.toString(), createDummyRequest());
        ctx.put(ContextConstants.DESTINATION.toString(), "TEST");
        int action = queryHost.prepare(1L, ctx);
        assertTrue(action == (PREPARED | READONLY | NO_JOIN));
        assertNotNull (ctx.get(ContextConstants.RESPONSE.toString(), 1000));
        assertFalse (ctx.getResult().hasFailures(), "Should not have failures");
    }

    @Test
    public void testNoRequest() throws Exception {
        Context ctx = new Context();
        cfg.put("continuations", "no");
        queryHost.setConfiguration(cfg);
        ctx.put(ContextConstants.DESTINATION.toString(), "TEST");
        int action = queryHost.prepare(1L, ctx);
        assertTrue(action == FAIL);
        assertNull (ctx.get(ContextConstants.RESPONSE.toString()));
        assertTrue (ctx.getResult().hasFailures(), "has Failures");
        assertTrue (ctx.getResult().failure().getIrc() == CMF.INVALID_REQUEST);
    }

    @Test
    public void testNoDestination() throws Exception {
        Context ctx = new Context();
        cfg.put("continuations", "no");
        queryHost.setConfiguration(cfg);
        ctx.put(ContextConstants.REQUEST.toString(), createDummyRequest());
        int action = queryHost.prepare(1L, ctx);
        assertTrue(action == FAIL);
        assertNull (ctx.get(ContextConstants.RESPONSE.toString()));
        assertTrue (ctx.getResult().hasFailures(), "has Failures");
        assertTrue (ctx.getResult().failure().getIrc() == CMF.MISCONFIGURED_ENDPOINT);
    }

    @Override
    public ISOMsg request(ISOMsg m, long timeout) throws ISOException {
        ISOMsg r = (ISOMsg) m.clone();
        r.setResponseMTI();
        r.set(39, "00");
        return r;
    }

    @Override
    public void request(ISOMsg m, long timeout, ISOResponseListener rl, Object handBack) throws ISOException {
        ISOMsg r = (ISOMsg) m.clone();
        r.setResponseMTI();
        r.set(39, "00");
        rl.responseReceived(r, handBack);
    }

    @Override
    public void send(ISOMsg m) throws IOException, ISOException { }

    @Override
    public boolean isConnected() {
        return true;
    }

    private ISOMsg createDummyRequest() {
        ISOMsg m = new ISOMsg("0800");
        m.set(7, ISODate.getANSIDate(new Date()));
        m.set(11, ISOUtil.zeropad(++stan, 6));
        m.set(70, "301");
        return m;
    }
}

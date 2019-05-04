/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2019 jPOS Software SRL
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


import org.jdom2.Element;
import org.jpos.core.*;
import org.jpos.iso.ISOMsg;
import org.jpos.rc.CMF;
import org.jpos.rc.Result;
import org.jpos.transaction.Context;
import org.jpos.transaction.ContextConstants;
import org.jpos.transaction.TransactionConstants;
import org.junit.Before;
import org.junit.Test;

import static org.jpos.transaction.ContextConstants.DESTINATION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class SelectDestinationTest implements TransactionConstants {
    private SelectDestination p;
    private Configuration cfg;

    @Before
    public void setUp() throws ConfigurationException {
        cfg = new SimpleConfiguration();
        cfg.put ("default-destination", "LOCAL");
        p = new SelectDestination();
        p.setConfiguration(cfg);

        Element qbean = new Element("participant");
        Element endpoint = new Element("endpoint");
        endpoint.setAttribute("destination", "V");
        endpoint.setText("4 41..42 4111111111111111\n42222");
        qbean.addContent(endpoint);

        endpoint = new Element("endpoint");
        endpoint.setAttribute("destination", "M");
        endpoint.setText("51");
        qbean.addContent(endpoint);

        qbean.addContent(regexp("N6", "^6[\\d]{15}$"));
        qbean.addContent(regexp("N7", "  ^7[\\d]{15}$  ")); // use some whitespace

        endpoint = new Element("endpoint");
        endpoint.setAttribute("destination", "DEFAULT");
        endpoint.setText("0..9");
        qbean.addContent(endpoint);

        p.setConfiguration(qbean);
    }

    private Element regexp (String endpoint, String regexp) {
        Element e = new Element("regexp");
        e.setAttribute("destination", endpoint);
        e.setText(regexp);
        return e;
    }

    @Test
    public void testNetwork_V () {
        cfg.put ("ignore-luhn", "false");
        p.setConfiguration(cfg);
        Context ctx = new Context();
        ctx.put (ContextConstants.REQUEST.toString(), createISOMsg("4111111111111111"));
        int action = p.prepare(1L, ctx);
        assertEquals ("Action should be PREPARED|NO_JOIN|READONLY", PREPARED | NO_JOIN | READONLY, action);
        Result rc = ctx.getResult();
        assertFalse("No failures", rc.hasFailures());
        assertEquals("Invalid Destination", "V", ctx.getString(DESTINATION.toString()));
    }
    @Test
    public void testNetwork_M () {
        cfg.put ("ignore-luhn", "false");
        p.setConfiguration(cfg);
        Context ctx = new Context();
        ctx.put (ContextConstants.REQUEST.toString(), createISOMsg("5111111111111118"));
        int action = p.prepare(1L, ctx);
        assertEquals ("Action should be PREPARED|NO_JOIN|READONLY", PREPARED | NO_JOIN | READONLY, action);
        Result rc = ctx.getResult();
        assertFalse("No failures", rc.hasFailures());
        assertEquals("Invalid Destination", "M", ctx.getString(DESTINATION.toString()));
    }
    @Test
    public void testInvalidLUHN () {
        cfg.put ("ignore-luhn", "false");
        p.setConfiguration(cfg);
        Context ctx = new Context();
        ctx.put (ContextConstants.REQUEST.toString(), createISOMsg("5111111111111111"));
        int action = p.prepare(1L, ctx);
        assertEquals ("Action should be ABORTED|NO_JOIN|READONLY", ABORTED | NO_JOIN | READONLY, action);
        Result rc = ctx.getResult();
        assertTrue("Has failures", rc.hasFailures());
        assertTrue("Should raise invalid card error", rc.failure().getIrc() == CMF.INVALID_CARD_OR_CARDHOLDER_NUMBER);
    }

    @Test
    public void testNetwork6 () {
        cfg.put ("ignore-luhn", "true");
        p.setConfiguration(cfg);
        Context ctx = new Context();
        ctx.put (ContextConstants.REQUEST.toString(), createISOMsg("6111111111111111"));
        int action = p.prepare(1L, ctx);
        assertEquals ("Action should be PREPARED|NO_JOIN|READONLY", PREPARED | NO_JOIN | READONLY, action);
        Result rc = ctx.getResult();
        assertFalse("No failures", rc.hasFailures());
        assertEquals("Invalid Destination", "N6", ctx.getString(DESTINATION.toString()));
    }

    @Test
    public void testNetwork7 () {
        cfg.put ("ignore-luhn", "true");
        p.setConfiguration(cfg);
        Context ctx = new Context();
        ctx.put (ContextConstants.REQUEST.toString(), createISOMsg("7111111111111111"));
        int action = p.prepare(1L, ctx);
        assertEquals ("Action should be PREPARED|NO_JOIN|READONLY", PREPARED | NO_JOIN | READONLY, action);
        Result rc = ctx.getResult();
        assertFalse("No failures", rc.hasFailures());
        assertEquals("Invalid Destination", "N7", ctx.getString(DESTINATION.toString()));
    }
    @Test
    public void testNetwork_Default () {
        cfg.put ("ignore-luhn", "true");
        p.setConfiguration(cfg);
        Context ctx = new Context();
        ctx.put (ContextConstants.REQUEST.toString(), createISOMsg("0000000000000001"));
        int action = p.prepare(1L, ctx);
        assertEquals ("Action should be PREPARED|NO_JOIN|READONLY", PREPARED | NO_JOIN | READONLY, action);
        Result rc = ctx.getResult();
        assertFalse("No failures", rc.hasFailures());
        assertEquals("Invalid Destination", "DEFAULT", ctx.getString(DESTINATION.toString()));
    }


    private ISOMsg createISOMsg(String pan) {
        ISOMsg m = new ISOMsg("2100");
        m.set(2, pan);
        m.set(14, "9901");
        return m;
    }
}

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

package org.jpos.iso;

import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.channel.LoopbackChannel;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.transaction.Context;
import org.jpos.transaction.ContextConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class IncomingListenerTest {

    @Test
    void contextCarriesTheMessageTraceId() throws Exception {
        IncomingListener listener = new IncomingListener();
        SimpleConfiguration cfg = new SimpleConfiguration();
        cfg.put("space", "tspace:default");
        cfg.put("queue", "incoming-listener-test");
        listener.setConfiguration(cfg);

        ISOMsg m = new ISOMsg("0200");
        m.set(7, "0904120000");
        m.set(11, "000123");
        m.set(41, "TERM0001");
        m.set(42, "MERCHANT0000001");

        listener.process(new LoopbackChannel(), m);

        Space<String, Context> sp = SpaceFactory.getSpace("tspace:default");
        Context ctx = sp.in("incoming-listener-test", 1000L);
        assertNotNull(ctx, "context queued");
        assertEquals(m.getTraceId(), ctx.get(ContextConstants.TRACE_ID.toString()));
        assertEquals(m.naturalTraceId(), ctx.get(ContextConstants.TRACE_ID.toString()));
    }
}

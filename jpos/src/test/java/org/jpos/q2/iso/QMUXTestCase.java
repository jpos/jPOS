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

package org.jpos.q2.iso;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOResponseListener;
import org.jpos.iso.MUX;
import org.jpos.q2.Q2;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.util.NameRegistrar;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;

@SuppressWarnings("unchecked")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class QMUXTestCase implements ISOResponseListener {
    static Q2 q2;
    static Space sp;
    static MUX mux;
    boolean expiredCalled;
    ISOMsg responseMsg;
    static Object receivedHandback;

    @BeforeAll
    public static void setUp() throws Exception {
        sp = SpaceFactory.getSpace();
        q2 = new Q2("build/resources/test/org/jpos/q2/iso");
        q2.start();
        Thread.sleep(2000L);
        try {
            mux = NameRegistrar.get("mux.mux");
        } catch (NameRegistrar.NotFoundException e) {
            fail("MUX not found");
        }
        receivedHandback = null;
    }

    @BeforeEach
    public void initExpired() {
        expiredCalled = false;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExpiredMessage() throws Exception {
        mux.request(createMsg("000001"), 500L, this, "Handback One");
        assertFalse(expiredCalled, "expired called too fast");
        assertNotNull(getInternalSpace(mux).rdp("send.0800000000029110001000001.req"), "Space doesn't contain message key");
        Thread.sleep(1000L);
        assertTrue(expiredCalled, "expired has not been called after 1 second");
        assertNull(getInternalSpace(mux).rdp("send.0800000000029110001000001.req"), "Cleanup failed, Space still contains message key");
        assertEquals("Handback One", receivedHandback, "Handback One not received");
    }

    @Test
    public void testAnsweredMessage() throws Exception {
        mux.request(createMsg("000002"), 500L, this, "Handback Two");
        assertFalse(expiredCalled, "expired called too fast");
        ISOMsg m = (ISOMsg) sp.in("send", 500L);
        assertNotNull(m, "Message not received by pseudo-channel");
        assertNotNull(getInternalSpace(mux).rdp("send.0800000000029110001000002.req"), "Space doesn't contain message key");
        m.setResponseMTI();
        sp.out("receive", m);
        Thread.sleep(100L);
        assertNotNull(responseMsg, "Response not received");
        Thread.sleep(1000L);
        assertFalse(expiredCalled, "Response received but expired was called");
        assertNull(getInternalSpace(mux).rdp("send.0800000000029110001000002.req"), "Cleanup failed, Space still contains message key");
        assertEquals("Handback Two", receivedHandback, "Handback Two not received");
    }

    @AfterAll
    public static void tearDown() throws Exception {
        Thread.sleep(2000L); // let the thing run
        q2.shutdown(true);
        Thread.sleep(2000L);
    }

    private ISOMsg createMsg(String stan) throws ISOException {
        ISOMsg m = new ISOMsg("0800");
        m.set(11, stan);
        m.set(41, "29110001"); // our favourite test terminal
        return m;
    }

    public void responseReceived(ISOMsg m, Object handBack) {
        responseMsg = m;
        receivedHandback = handBack;
    }

    public void expired(Object handBack) {
        expiredCalled = true;
        receivedHandback = handBack;
    }

    @Test
    public void testMTIMapping() throws ISOException {
        String[] requests = new String[] { "0100", "0101", "0400", "0401", "0420", "0800", "1100", "1800", "1804", "1820", "1820",
                "1200", "1220", "1240" };
        String[] responses = new String[] { "0110", "0110", "0410", "0410", "0430", "0810", "1110", "1810", "1814", "1824", "1830",
                "1210", "1230", "1250" };
        assertEquals(requests.length, responses.length, "Request/Response string arrays must hold same number of entries");
        ISOMsg request = new ISOMsg();
        ISOMsg response = new ISOMsg();
        for (int i = 0; i < requests.length; i++) {
            request.setMTI(requests[i]);
            request.set (11, Integer.toString(i));
            response.setMTI(responses[i]);
            response.set (11, Integer.toString(i));
            assertEquals(((QMUX) mux).getKey(request), ((QMUX) mux).getKey(response));
        }
    }

    private Space getInternalSpace (MUX mux) throws NoSuchFieldException, IllegalAccessException {
        Field field = mux.getClass().getDeclaredField("isp");
        field.setAccessible(true);
        return (Space) field.get(mux);
    }
}

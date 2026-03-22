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

package org.jpos.q2.iso;

import org.jdom2.Element;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.q2.Q2;
import org.jpos.q2.QFactory;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;
import org.jpos.util.Realm;
import org.junit.jupiter.api.Test;

import javax.management.MBeanServer;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AdaptorRealmTest {
    @Test
    void oneShotChannelAdaptorUsesCommClientRealm() {
        assertEquals(Realm.COMM_CLIENT, new OneShotChannelAdaptor().getLog().getRealm());
    }

    @Test
    void oneShotChannelAdaptorMk2UsesCommClientRealm() {
        assertEquals(Realm.COMM_CLIENT, new OneShotChannelAdaptorMK2().getLog().getRealm());
    }

    @Test
    void multiSessionChannelAdaptorUsesCommChannelRealm() {
        assertEquals(Realm.COMM_CHANNEL, new MultiSessionChannelAdaptor().getLog().getRealm());
    }

    @Test
    void nestedChannelInheritsAdaptorRealmWhenRealmIsNotExplicit() throws Exception {
        ChannelAdaptor adaptor = new ChannelAdaptor();
        adaptor.setName("iso-server");
        adaptor.setLogger("Q2");

        ISOChannel channel = adaptor.newChannel(channelElement(null), qFactory());

        assertEquals(Realm.COMM_CHANNEL, ((LogSource) channel).getRealm());
    }

    @Test
    void nestedChannelKeepsExplicitRealm() throws Exception {
        ChannelAdaptor adaptor = new ChannelAdaptor();
        adaptor.setName("iso-server");
        adaptor.setLogger("Q2");

        ISOChannel channel = adaptor.newChannel(channelElement("custom/channel"), qFactory());

        assertEquals("custom/channel", ((LogSource) channel).getRealm());
    }

    private Element channelElement(String realm) {
        Element channel = new Element("channel");
        channel.setAttribute("class", TestChannel.class.getName());
        channel.setAttribute("logger", "Q2");
        if (realm != null)
            channel.setAttribute("realm", realm);
        return channel;
    }

    public static class TestChannel implements ISOChannel, LogSource {
        private Logger logger;
        private String realm;
        private String name;
        private ISOPackager packager;

        @Override
        public void setLogger(Logger logger, String realm) {
            this.logger = logger;
            this.realm = realm;
        }

        @Override
        public String getRealm() {
            return realm;
        }

        @Override
        public Logger getLogger() {
            return logger;
        }

        @Override
        public void setPackager(ISOPackager p) {
            this.packager = p;
        }

        @Override
        public void connect() {
        }

        @Override
        public void disconnect() {
        }

        @Override
        public void reconnect() {
        }

        @Override
        public boolean isConnected() {
            return false;
        }

        @Override
        public ISOMsg receive() {
            return null;
        }

        @Override
        public void send(ISOMsg m) throws IOException, ISOException {
        }

        @Override
        public void send(byte[] b) throws IOException, ISOException {
        }

        @Override
        public void setUsable(boolean b) {
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ISOPackager getPackager() {
            return packager;
        }

        @Override
        public Object clone() {
            return this;
        }
    }

    private QFactory qFactory() throws Exception {
        Q2 q2 = mock(Q2.class);
        MBeanServer mBeanServer = mock(MBeanServer.class);
        when(q2.getMBeanServer()).thenReturn(mBeanServer);
        when(mBeanServer.instantiate(anyString(), any())).thenReturn(new TestChannel());
        return new QFactory(null, q2);
    }
}

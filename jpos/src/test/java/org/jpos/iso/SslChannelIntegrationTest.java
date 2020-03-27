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

package org.jpos.iso;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.EOFException;
import java.io.IOException;
import java.util.Properties;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jpos.core.Configuration;
import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.channel.XMLChannel;
import org.jpos.iso.packager.XMLPackager;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;
import org.jpos.util.ThreadPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * $Revision$
 * $Date$
 * $Author$
 */
public class SslChannelIntegrationTest {

    private static final int PORT = 4000;

    private Logger logger;

    @BeforeEach
    public void setUp() throws Exception {
        logger = new Logger();
        logger.addListener(new SimpleLogListener());
    }

    @Test
    public void serverSideDisconnect() throws Exception {
        ISOServer isoServer = newIsoServer();
        new Thread(isoServer).start();

        XMLChannel clientChannel = newClientChannel();

        clientChannel.connect();
        // need to push some traffic through to complete the SSL handshake
        clientChannel.send(new ISOMsg("0800"));
        assertThat(clientChannel.receive(), hasMti("0810"));

        isoServer.shutdown();

        try {
            clientChannel.receive();
            fail("clientChannel should be closed");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(EOFException.class)));
        }
    }

    private XMLChannel newClientChannel() throws IOException, ISOException {
        XMLChannel clientChannel = new XMLChannel(new XMLPackager());
        clientChannel.setSocketFactory(new GenericSSLSocketFactory());
        clientChannel.setConfiguration(clientConfiguration());
        clientChannel.setLogger(logger, "client.channel");
        clientChannel.setHost("localhost", PORT);
        return clientChannel;
    }

    private ISOServer newIsoServer() throws IOException, ISOException {
        XMLChannel clientSide = new XMLChannel(new XMLPackager());
        clientSide.setLogger(logger, "server.channel");

        ISOServer isoServer = new ISOServer(PORT, clientSide, new ThreadPool());
        isoServer.setSocketFactory(new GenericSSLSocketFactory());
        isoServer.setConfiguration(serverConfiguration());
        isoServer.setLogger(logger, "server");
        isoServer.addISORequestListener(new TestListener());
        return isoServer;
    }

// keystore.jks created using the following command in a shell:
//
//    [user@hostname]$ keytool -genkey -keyalg RSA -alias selfsigned -keystore keystore.jks -storepass password
//    What is your first and last name?
//      [Unknown]:  localhost
//    What is the name of your organizational unit?
//      [Unknown]:  Dummy
//    What is the name of your organization?
//      [Unknown]:  Dummy
//    What is the name of your City or Locality?
//      [Unknown]:  Somewhere
//    What is the name of your State or Province?
//      [Unknown]:  Somewhere
//    What is the two-letter country code for this unit?
//      [Unknown]:  AU
//    Is CN=localhost, OU=Dummy, O=Dummy, L=Somewhere, ST=Somewhere, C=AU correct?
//      [no]:  yes
//
//    Enter key password for <selfsigned>
//    	(RETURN if same as keystore password):

    private Configuration serverConfiguration() {
        Properties props = new Properties();
        props.put("keystore", "src/test/resources/keystore.jks");
        props.put("storepassword", "password");
        props.put("keypassword", "password");
        // props.put("addEnabledCipherSuite", "SSL_RSA_WITH_3DES_EDE_CBC_SHA");
        return new SimpleConfiguration(props);
    }

    private Configuration clientConfiguration() {
        Properties props = new Properties();
        props.put("keystore", "src/test/resources/keystore.jks");
        props.put("serverauth", "false");
        props.put("storepassword", "password");
        props.put("keypassword", "password");
        // props.put("addEnabledCipherSuite", "SSL_RSA_WITH_3DES_EDE_CBC_SHA");
        props.put("timeout", "1000");
        props.put("connect-timeout", "1000");
        return new SimpleConfiguration(props);
    }

    private class TestListener implements ISORequestListener {

        public boolean process(ISOSource source, ISOMsg m) {
            try {
                source.send(new ISOMsg("0810"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    private Matcher<ISOMsg> hasMti(final String mti) {
        return new TypeSafeMatcher<ISOMsg>() {
            @Override
            public boolean matchesSafely(ISOMsg isoMsg) {
                return mti.equals(isoMsg.getString(0));
            }

            public void describeTo(Description description) {
                description.appendText("ISOMsg with mti ").appendValue(mti);
            }
        };
    }

}

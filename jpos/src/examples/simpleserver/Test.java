/*
 * Copyright (c) 2000 jPOS.org.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the jPOS project 
 *    (http://www.jpos.org/)". Alternately, this acknowledgment may 
 *    appear in the software itself, if and wherever such third-party 
 *    acknowledgments normally appear.
 *
 * 4. The names "jPOS" and "jPOS.org" must not be used to endorse 
 *    or promote products derived from this software without prior 
 *    written permission. For written permission, please contact 
 *    license@jpos.org.
 *
 * 5. Products derived from this software may not be called "jPOS",
 *    nor may "jPOS" appear in their name, without prior written
 *    permission of the jPOS project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  
 * IN NO EVENT SHALL THE JPOS PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS 
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the jPOS Project.  For more
 * information please see <http://www.jpos.org/>.
 */

package simpleserver;

import java.io.IOException;
import java.io.EOFException;
import java.net.ServerSocket;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOField;
import org.jpos.iso.ServerChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOFactory;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOUtil;
import org.jpos.util.LogSource;
import org.jpos.util.LogEvent;
import org.jpos.util.SimpleLogListener;
import org.jpos.util.Logger;
import org.jpos.util.LogSource;
import org.jpos.core.Configuration;
import org.jpos.core.SimpleConfiguration;

public class Test implements Runnable, LogSource {
    Logger logger;
    String realm;
    public static final String CFG_PORT = "simpleserver.port";
    boolean testMux;

    public Test (Logger logger, String realm, boolean testMux) {
	super();
	setLogger (logger, realm);
	this.testMux = testMux;
    }
    public void setLogger (Logger logger, String realm) {
	this.logger = logger;
	this.realm  = realm;
    }
    public String getRealm () {
	return realm;
    }
    public Logger getLogger() {
	return logger;
    }
    protected class Session implements Runnable, LogSource {
        ServerChannel channel;
        protected Session(ServerChannel channel) {
            this.channel = channel;
        }
        public void run() {
	    Logger.log (new LogEvent (this, "SessionStart"));
            try {
                for (;;) {
                    ISOMsg m = channel.receive();
		    m.setResponseMTI();
                    m.set (new ISOField(39, "00"));
		    if (testMux)
			Thread.sleep (10);  // simulate server delay
                    channel.send(m);
		    if (testMux && m.getMTI().equals ("0810")) {
			// on 'testMux' mode we originate an unexpected
			// message that will be handled by ISOMUX's ISORequestListener
			m.setMTI ("0800");
			channel.send(m);
		    }
                }
	    } catch (EOFException e) {
            } catch (Exception e) { 
		Logger.log (new LogEvent (this, "SessionError", e));
            } 
	    Logger.log (new LogEvent (this, "SessionEnd"));
        }
	public void setLogger (Logger logger, String realm) { }
	public String getRealm () {
	    return realm + ".session";
	}
	public Logger getLogger() {
	    return logger;
	}
    }

    public void run() {
        ServerChannel  channel;
	String cfgFile    = System.getProperties().getProperty("jpos.config");
        try {
	    Configuration cfg = new SimpleConfiguration (cfgFile);
	    int port          = cfg.getInt (CFG_PORT);
            ServerSocket serverSocket = new ServerSocket(port);
	    Logger.log (
	       new LogEvent (this, "message", "listening on port "+port)
	    );
            for (;;) {
		channel = (ServerChannel) ISOFactory.newChannel 
		    (cfg, "simpleserver", logger, realm);
                channel.accept(serverSocket);
                Thread t = new Thread (new Session(channel));
                t.setDaemon(true);
                t.start();
            }
        } catch (ISOException e) {
	    Logger.log (new LogEvent (this, "mainrun", e));
        } catch (IOException e) {
	    Logger.log (new LogEvent (this, "mainrun", e));
        }
    }
    public static void main(String args[]) {
	boolean testMux = args.length > 0 && args[0].equals ("-testmux");
	Logger logger = new Logger();
	logger.addListener (new SimpleLogListener (System.out));
        new Thread(new Test(logger, "SimpleServer", testMux)).start();
    }
}

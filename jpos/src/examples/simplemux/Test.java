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

package simplemux;

import java.io.IOException;
import java.io.EOFException;
import java.net.ServerSocket;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ServerChannel;
import org.jpos.iso.ISOMUX;
import org.jpos.iso.ISORequest;
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
import org.jpos.util.ThreadPool;

/**
 * ISOMUX example
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class Test implements Runnable, LogSource {
    Logger logger;
    String realm;
    ThreadPool pool;
    Configuration cfg;
    ISOMUX mux;

    public static final String CFG_PORT = "simplemux.clientside.port";

    public Test (Configuration cfg, Logger logger, String realm) 
	throws ISOException
    {
	super();
	this.cfg = cfg;
	setLogger (logger, realm);
	pool = new ThreadPool (1, 32);	// max 32 threads
	mux = ISOFactory.newMUX (cfg, "simplemux.serverside", logger, realm);
	mux.setISORequestListener (new DummyRequestListener (this));
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
        ISOChannel channel;
        protected Session(ISOChannel channel) {
            this.channel = channel;
        }
        public void run() {
	    Logger.log (new LogEvent (this, "SessionStart"));
            try {
                for (;;) {
                    ISOMsg m = channel.receive();
		    ISORequest request = new ISORequest (m);
		    mux.queue (request);
		    ISOMsg r = request.getResponse (60*1000);
		    if (r != null) 
			channel.send(r);
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
        try {
	    int port          = cfg.getInt (CFG_PORT);
            ServerSocket serverSocket = new ServerSocket(port);
	    Logger.log (
	       new LogEvent (this, "message", "listening on port "+port)
	    );
            for (;;) {
		channel = (ServerChannel) ISOFactory.newChannel 
		    (cfg, "simplemux.clientside", logger, realm);
                channel.accept(serverSocket);
		pool.execute (new Session (channel));
            }
        } catch (ISOException e) {
	    Logger.log (new LogEvent (this, "mainrun", e));
        } catch (IOException e) {
	    Logger.log (new LogEvent (this, "mainrun", e));
        }
    }
    public static void main(String args[]) {
	String cfgFile    = System.getProperties().getProperty("jpos.config");
	try {
	    Configuration cfg = new SimpleConfiguration (cfgFile);
	    Logger logger = new Logger();
	    logger.addListener (new SimpleLogListener (System.out));
	    new Thread(new Test(cfg, logger, "SimpleMUX")).start();
	} catch (ISOException e) {
	    e.dump (System.out, "");
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
}

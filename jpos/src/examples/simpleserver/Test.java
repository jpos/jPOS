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

package simpleserver;

import java.io.IOException;
import java.net.ServerSocket;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.RawChannel;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOUtil;
import org.jpos.util.LogProducer;
import org.jpos.util.LogEvent;
import org.jpos.util.SimpleLogListener;
import org.jpos.util.Logger;
import org.jpos.util.LogProducer;
import org.jpos.core.Configuration;
import org.jpos.core.SimpleConfiguration;

public class Test implements Runnable, LogProducer {
    Logger logger;
    String realm;

    public static final String CFG_PORT     = "simpleserver.port";
    public static final String CFG_CHANNEL  = "simpleserver.channel";
    public static final String CFG_PACKAGER = "simpleserver.packager";
    public static final String CFG_HEADER   = "simpleserver.header";

    public Test (Logger logger, String realm) {
	super();
	setLogger (logger, realm);
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
    protected class Session implements Runnable, LogProducer {
        ISOChannel channel;
        protected Session(ISOChannel channel) {
            this.channel = channel;
        }
        public void run() {
	    Logger.log (new LogEvent (this, "SessionStart"));
            try {
                for (;;) {
                    ISOMsg m = channel.receive();
		    m.setResponseMTI();
                    m.set (new ISOField(39, "00"));
                    channel.send(m);
                }
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

    private ISOChannel createChannel (Configuration cfg) {
	String channelName  = cfg.get (CFG_CHANNEL);
	String packagerName = cfg.get (CFG_PACKAGER);
	String header       = cfg.get (CFG_HEADER);
        ISOChannel channel  = null;
        try {
            Class c = Class.forName(channelName);
            Class p = Class.forName(packagerName);
            if (c != null && p != null) {
		ISOPackager packager = (ISOPackager) p.newInstance();
                channel = (ISOChannel) c.newInstance();
                channel.setPackager(packager);
		channel.setLogger (logger, realm + ".channel");
		if (channel instanceof RawChannel && header != null) 
		    ((RawChannel)channel).setTPDU (
			ISOUtil.str2bcd(header, false)
		    );
            }
        } catch (Exception ex) {
	    LogEvent evt = new LogEvent (this, "createChannel");
	    evt.addMessage ("<channel>"+channelName+"</channel>");
	    evt.addMessage ("<packager>"+packagerName+"</packager>");
	    evt.addMessage (ex);
	    Logger.log (evt);
        }
        return channel;
    }

    public void run() {
        ISOChannel  channel;
	String cfgFile    = System.getProperties().getProperty("jpos.config");
        try {
	    Configuration cfg = new SimpleConfiguration (cfgFile);
	    int port          = Integer.parseInt(cfg.get (CFG_PORT));
            ServerSocket serverSocket = new ServerSocket(port);
	    Logger.log (
	       new LogEvent (this, "message", "listening on port "+port)
	    );
            for (;;) {
                channel = createChannel (cfg);
                channel.accept(serverSocket);
                Thread t = new Thread (new Session(channel));
                t.setDaemon(true);
                t.start();
            }
        } catch (IOException e) {
	    Logger.log (new LogEvent (this, "mainrun", e));
        }
    }
    public static void main(String args[]) {
	Logger logger = new Logger();
	logger.addListener (new SimpleLogListener (System.out));
        new Thread(new Test(logger, "SimpleServer")).start();
    }
}

/* $Id: */

package simpleclient;

import java.util.Date;
import java.io.IOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.RawChannel;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.util.LogProducer;
import org.jpos.util.LogEvent;
import org.jpos.util.SimpleLogListener;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogProducer;
import org.jpos.core.Configuration;
import org.jpos.core.Sequencer;
import org.jpos.core.VolatileSequencer;
import org.jpos.core.SimpleConfiguration;

public class Test extends SimpleLogProducer {
    public static final String CFG_PORT     = "simpleserver.port";
    public static final String CFG_CHANNEL  = "simpleserver.channel";
    public static final String CFG_PACKAGER = "simpleserver.packager";
    public static final String CFG_HEADER   = "simpleserver.header";

    private ISOChannel channel;
    private Sequencer seq;

    public Test (Configuration cfg, Logger logger, String realm) {
	super();
	setLogger (logger, realm);
	channel = createChannel (cfg);
	seq     = new VolatileSequencer();
    }

    private ISOChannel createChannel (Configuration cfg) {
	String channelName  = cfg.get (CFG_CHANNEL);
	String packagerName = cfg.get (CFG_PACKAGER);
	String header       = cfg.get (CFG_HEADER);
	int    port         = cfg.getInt (CFG_PORT);
        ISOChannel channel  = null;
        try {
            Class c = Class.forName(channelName);
            Class p = Class.forName(packagerName);
            if (c != null && p != null) {
		ISOPackager packager = (ISOPackager) p.newInstance();
                channel = (ISOChannel) c.newInstance();
		channel.setHost ("localhost", port);
                channel.setPackager(packager);
		channel.setLogger (logger, realm + ".channel");
		if (channel instanceof RawChannel && header != null) 
		    ((RawChannel)channel).setTPDU (
			ISOUtil.str2bcd(header, false)
		    );
		channel.connect();
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
    public void send (String mti) {
	try {
	    Date d = new Date();
	    ISOMsg m = new ISOMsg();
	    m.setMTI (mti);
	    m.set (new ISOField (3, "000000")); // dummy field
	    m.set (new ISOField (7,ISODate.getDateTime(d)));

            m.set (new ISOField (11,
		ISOUtil.zeropad(
		    new Integer(seq.get ("test.counter")).toString(),6)
		)
	    );

	    m.set (new ISOField(12,ISODate.getTime(d)));
            m.set (new ISOField(13,ISODate.getDate(d))); 

	    channel.send (m);
	    ISOMsg response = channel.receive();	
	    // ...
	    // ...
	    // here you are supposed to analize response, etc.
	    // ...
	    // ...
	} catch (ISOException e) {
	    Logger.log (new LogEvent (this, "send", e));
	} catch (IOException e) {
	    Logger.log (new LogEvent (this, "send", e));
	}
    }
    public static void main (String args[]) {
	Logger logger = new Logger();
	logger.addListener (new SimpleLogListener (System.out));

	String cfgFile    = System.getProperties().getProperty("jpos.config");
        try {
	    Configuration cfg = new SimpleConfiguration (cfgFile);
	    int port          = Integer.parseInt(cfg.get (CFG_PORT));

	    Test t = new Test (cfg, logger, "Test");
	    t.send ("0100");
	    t.send ("0101");
	    t.send ("0200");
	    t.send ("0800");
	    System.out.println ("[terminating]");
	} catch (IOException e) {
	    e.printStackTrace();
	} 
    }
}

/* $Id: */

package simpleclient;

import java.util.Date;
import java.io.IOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOFactory;
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
    private ISOChannel channel;
    private Sequencer seq;

    public Test (Configuration cfg, Logger logger, String realm) 
	throws ISOException, IOException
    {
	super();
	setLogger (logger, realm);
	channel = ISOFactory.newChannel 
	    (cfg, "simpleclient", logger, realm);
	seq     = new VolatileSequencer();
	channel.connect();
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
	    Test t = new Test (cfg, logger, "Test");
	    t.send ("0100");
	    t.send ("0101");
	    t.send ("0200");
	    t.send ("0800");
	    System.out.println ("[terminating]");
	} catch (ISOException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	} 
    }
}

/* $Id$ */

package xsltfilter;

import java.util.Date;
import java.io.IOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOFilter;
import org.jpos.iso.FilteredChannel;
import org.jpos.iso.ISOFilter.VetoException;
import org.jpos.iso.ISOFactory;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.iso.filter.XSLTFilter;
import org.jpos.util.LogProducer;
import org.jpos.util.LogEvent;
import org.jpos.util.SimpleLogListener;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogProducer;
import org.jpos.core.Configuration;
import org.jpos.core.Sequencer;
import org.jpos.core.VolatileSequencer;
import org.jpos.core.SimpleConfiguration;
import org.xml.sax.SAXException;

public class Test extends SimpleLogProducer {
    private FilteredChannel channel;
    private Sequencer seq;
    private static final String COUNTERNAME = "test.counter";

    public Test 
	(Configuration cfg, Logger logger, String realm, String cfgPrefix)
	throws ISOException, IOException, SAXException
    {
	super();
	setLogger (logger, realm);
	channel = (FilteredChannel) 
	    ISOFactory.newChannel (cfg, cfgPrefix, logger, realm);
	seq     = new VolatileSequencer();

	seq.set (COUNTERNAME, (int) (System.currentTimeMillis() % 1000000));
	channel.addIncomingFilter (
	    new XSLTFilter (cfg.get ("xsltfilter.incoming"), true)
	);
	channel.addOutgoingFilter (
	    new XSLTFilter (cfg.get ("xsltfilter.outgoing"), true)
	);
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
		    new Integer(seq.get (COUNTERNAME)).toString(),6)
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
    public void disconnect () throws IOException {
	channel.disconnect();
    }
    public static void main (String args[]) {
	int n = 1;  // number of iterations
	String cfgPrefix = "simpleclient";
	if (args.length > 0 && args[0].equals ("-testmux"))
	    cfgPrefix = cfgPrefix + args[0];
	if (args.length > 1)
	    n = Integer.parseInt (args[1]);

	Logger logger = new Logger();
	logger.addListener (new SimpleLogListener (System.out));

	String cfgFile    = System.getProperties().getProperty("jpos.config");
        try {
	    Configuration cfg = new SimpleConfiguration (cfgFile);
	    Test t = new Test (cfg, logger, "Test", cfgPrefix);
	    for (int i=0; i<n; i++) {
		t.send ("0800");
		try {
		    if (i<n)
			Thread.sleep (1000); // time to play with XSLT file
		} catch (InterruptedException e) { }
	    }
	    t.disconnect();
	} catch (ISOException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (SAXException e) {
	    e.printStackTrace();
	}
    }
}

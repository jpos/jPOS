/* $Id: */

package europay;

import java.util.Date;
import java.io.PrintStream;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.SimpleLogProducer;
import org.jpos.util.SimpleLogListener;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOException;
import org.jpos.iso.EuroPackager;

public class Test extends SimpleLogProducer {
    public Test (Logger logger, String realm) {
	setLogger (logger, realm);
    }
    public void simpleMessage () {
	LogEvent evt = new LogEvent (this, "europay");
	Date d = new Date();

	ISOPackager packager = new EuroPackager(); 
	packager.setLogger (getLogger(), "Packager");

	try {
	    String hex = 
		 "3038303080200000800100000400"
		+"00001000000030313635353930363637"
		+"39333939303238202020203034303231"
		+"30303430333330303430373031303430"
		+"3832353030313036303033303330";

	    byte[] b = ISOUtil.hex2byte 
	    	(hex.getBytes(), 0, hex.length()/2);


	    ISOMsg m = new ISOMsg();
	    m.setPackager (packager);
	    m.unpack (b);
	    evt.addMessage (m);
	    byte[] bp = m.pack();
	} catch (ISOException e) {
	    evt.addMessage (e);
	}
	Logger.log (evt);
    }
    public static void main (String args[]) {
	Logger logger = new Logger();
	logger.addListener (new SimpleLogListener (System.out));

	Test t = new Test (logger, "Test");
	t.simpleMessage();
    }
}

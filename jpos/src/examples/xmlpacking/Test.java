/* $Id: */

package xmlpacking;

import java.util.Date;
import java.io.PrintStream;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.SimpleLogSource;
import org.jpos.util.SimpleLogListener;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOBinaryField;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOException;
import org.jpos.iso.packager.XMLPackager;

public class Test extends SimpleLogSource {
    public Test (Logger logger, String realm) {
	setLogger (logger, realm);
    }
    public void testXMLMessage () {
	LogEvent evt = new LogEvent (this, "XMLMessage");
	Date d = new Date();

	try {
	    ISOPackager packager = new XMLPackager(); 
	    packager.setLogger (getLogger(), "XMLPackager");

	    ISOMsg m = new ISOMsg();
	    m.setPackager (packager);
	    m.set (new ISOField (0,  "0800"));
	    m.set (new ISOField (3,  "000000"));
	    m.set (new ISOField (11, "000001"));
	    m.set (new ISOField (7,  ISODate.getDateTime(d)));
	    m.set (new ISOField (12, ISODate.getTime(d)));
	    m.set (new ISOField (13, ISODate.getDate(d)));
	    m.set 
		(new ISOField (48, "Less than '<' and greater than '>'"));
	    m.set (new ISOBinaryField (127, "BINARY FIELD".getBytes()));

	    // add inner message
	    ISOMsg inner = new ISOMsg (126);
	    inner.set (new ISOField (0,  "INNER-0"));
	    inner.set (new ISOField (1,  "INNER-1"));
	    inner.set (new ISOField (2,  "INNER-2"));
	    inner.set (new ISOBinaryField (3,  "INNER-BINARY".getBytes()));
	    m.set (inner);

	    byte[] b = m.pack();
	    evt.addMessage (m);

	    ISOMsg m1 = new ISOMsg();
	    m1.setPackager (packager);
	    m1.unpack (b);
	    evt.addMessage (m1);
	} catch (ISOException e) {
	    evt.addMessage (e);
	}
	Logger.log (evt);
    }
    public static void main (String args[]) {
	Logger logger = new Logger();
	logger.addListener (new SimpleLogListener (System.out));

	Test t = new Test (logger, "Test");
	t.testXMLMessage();
    }
}

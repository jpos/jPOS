/* $Id$ */

package simplepacking;

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
import org.jpos.iso.packager.ISO93BPackager;

public class Test extends SimpleLogSource {
    public Test (Logger logger, String realm) {
	setLogger (logger, realm);
    }
    public void simpleMessage () {
	LogEvent evt = new LogEvent (this, "SimpleMessage");
	Date d = new Date();

	ISOPackager packager = new ISO93BPackager();  // 1) Create packager
	// comment the following line if you don't want to debug packager
	packager.setLogger (getLogger(), "Packager");

	ISOMsg m = new ISOMsg();                      // 2) create ISOMsg

	m.setPackager (packager);                     // 3) assign packager
	try {
	    // 4) populate ISOMsg
	    m.set (new ISOField (0,  "1800"));
	    m.set (new ISOField (3,  "000000"));
	    m.set (new ISOField (11, "000001"));
	    m.set (new ISOField (7,  ISODate.getDateTime(d)));
	    m.set (new ISOField (12, ISODate.getTime(d)));
	    m.set (new ISOField (13, ISODate.getDate(d)));
	    m.set (new ISOBinaryField (128, "AAAAAAAA".getBytes()));
	    byte[] b = m.pack();                      // 5) packit

	    evt.addMessage (m);
	    evt.addMessage (
		"<packed>"+ISOUtil.hexString(b)+"</packed>");

	    // Unpacking 'byte[] b' image into ISOMsg m1
	    ISOMsg m1 = new ISOMsg();
	    m1.setPackager (packager);
	    m1.unpack (b);

	    // Logging
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
	t.simpleMessage();
    }
}

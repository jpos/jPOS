/* $Id: */

package isomsg;

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
import org.jpos.iso.ISOException;

public class Test extends SimpleLogProducer {
    int[] mask  = { 0, 3, 7 };
    int[] mask1 = { 0, 3, 7, 41, 42 };

    public Test (Logger logger, String realm) {
	setLogger (logger, realm);
    }
    private ISOMsg createMessage(int[] mask) throws ISOException 
    {
	Date d = new Date();
	ISOMsg m = new ISOMsg();
	m.set (new ISOField (0,  "0800"));
	m.set (new ISOField (3,  "000000"));
	m.set (new ISOField (7,  ISODate.getDateTime(d)));
	m.set (new ISOField (11, "000001"));
	m.set (new ISOField (12, ISODate.getTime(d)));
	m.set (new ISOField (13, ISODate.getDate(d)));
	return mask == null ? m : (ISOMsg) m.clone(mask);
    }
    public void test() {
	LogEvent evt = new LogEvent (this, "Test");
	try {
	    ISOMsg a = createMessage(null);
	    evt.addMessage ("Original Message A (full):");
	    evt.addMessage (a);

	    ISOMsg b = (ISOMsg) a.clone();
	    b.set (new ISOField (41, "12345678"));
	    b.set (new ISOField (42, "123456789012345"));
	    evt.addMessage 
		("Message B is message A's clone plus fields 41 and 42");
	    evt.addMessage (b);

	    ISOMsg c = createMessage (mask);
	    evt.addMessage ("Message C just fields 0, 3 and 7");
	    evt.addMessage (c);

	    ISOMsg d = (ISOMsg) c.clone();
	    d.merge (b);
	    evt.addMessage ("Message D is C merged with B");
	    evt.addMessage (d);

	    ISOMsg e = (ISOMsg) d.clone(mask1);
	    evt.addMessage (
		"Message E == message (D and mask1) (clone(int[])");
	    evt.addMessage (e);
	} catch (ISOException ex) {
	    evt.addMessage (ex);
	}
	Logger.log (evt);
    }
    public void testNested() {
	LogEvent evt = new LogEvent (this, "TestNested");
	try {
	    ISOMsg m      = createMessage(null);
	    ISOMsg inner  = new ISOMsg (127); // goes at outter field 127
	    inner.set (new ISOField (0,"001"));
	    inner.set (new ISOField (2,"002"));
	    inner.set (new ISOField (2,"003"));
	    m.set (inner);
	    evt.addMessage (m);
	} catch (ISOException ex) {
	    evt.addMessage (ex);
	}
	Logger.log (evt);
    }
    public static void main (String args[]) {
	Logger logger = new Logger();
	logger.addListener (new SimpleLogListener (System.out));

	Test t = new Test (logger, "Test");
	t.test();
	t.testNested();
    }
}

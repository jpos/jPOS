/* $Id: */

package logger;

import java.io.PrintStream;
import org.jpos.iso.ISOException;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.Loggeable;
import org.jpos.util.SimpleLogProducer;
import org.jpos.util.SimpleLogListener;

public class Test extends SimpleLogProducer implements Loggeable {
    public Test (Logger logger, String realm) {
	setLogger (logger, realm);
    }
    public void testSimpleEvent() {
	LogEvent evt = new LogEvent (this, "SimpleEvent");
	evt.addMessage ("This is testSimpleEvent running");
	Logger.log (evt);
    }
    public void testLoggeable () {
	LogEvent evt = new LogEvent (this, "Loggeable");
	evt.addMessage (this);
	Logger.log (evt);
    }
    public void testException() {
	LogEvent evt = new LogEvent (this, "ExceptionDemo");
	evt.addMessage (
	    new Exception ("This is a sample exception - not an error")
	);
	Logger.log (evt);
    }
    public void testMulti() {
	LogEvent evt = new LogEvent (this, "MultipleMessages");
	evt.addMessage ("This is a multipart LogEvent");
	evt.addMessage ("We a this (Loggeable) and also an exception");
	evt.addMessage (this);
	evt.addMessage (
	    new Exception ("This is not an error either")
	);
	Logger.log (evt);
    }
    public void dump (PrintStream p, String indent) {
	String inner = indent + "  ";
	p.println (indent + "<loggeable>");
	p.println (inner  + "This class implements Loggeable");
	p.println (indent + "</loggeable>");
    }
    public void testISOException() {
	LogEvent evt = new LogEvent (this, "ExceptionDemo");
	evt.addMessage (
	    new Exception ("This is a simple exception - not an error")
	);
	evt.addMessage (
	    new ISOException ("This is a simple ISOException")
	);
	Exception simple    = new Exception ("Simple Exception");
	ISOException inner  = new ISOException ("some detail message", simple);
	ISOException outter  = new ISOException (inner);
	evt.addMessage (outter);
	Logger.log (evt);
    }

    public static void main (String args[]) {
	Logger logger = new Logger();
	logger.addListener (new SimpleLogListener (System.out));

	Test t = new Test (logger, "Test");
	t.testSimpleEvent();
	t.testLoggeable();
	t.testException();
	t.testMulti();
	t.testISOException();
    }
}

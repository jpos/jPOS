/* $Id: */

package dirpoll;

import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.SimpleLogSource;
import org.jpos.util.SimpleLogListener;
import org.jpos.util.DirPoll;

public class Test extends SimpleLogSource implements DirPoll.Processor {
    public Test () {
	super();
    }
    public byte[] process (String name, byte[] b) {
	return ("request: " + name + " content="+ new String (b)).getBytes();
    }
    public void dirpoll (String basePath) {
	DirPoll dp = new DirPoll();
	dp.setLogger (getLogger(), "dirpoll-test");
	dp.setPath (basePath);
	dp.createDirs ();
	dp.setProcessor (this);
	// dp.addPriority (".A");
	// dp.addPriority (".B");
	// dp.addPriority (".C");
	new Thread (dp).start();
    }
    public static void main (String args[]) {
	if (args.length < 1) {
	    System.out.println ("Usage: example dirpoll <basepath>");
	    System.exit(1);
	}
	Logger logger = new Logger();
	logger.addListener (new SimpleLogListener (System.out));

	Test t = new Test ();
	t.setLogger (logger, "test");
	t.dirpoll (args[0]);
    }
}

package tpl;

import java.io.*;
import java.sql.*;
import java.util.*;
import org.jpos.util.*;
import org.jpos.core.*;
import org.jpos.tpl.*;

public class Test {
    public static void main (String args[]) {
	try {
	    Logger logger = new Logger();
	    logger.addListener (new SimpleLogListener (System.out));
	    Configuration cfg = new SimpleConfiguration 
		("src/examples/tpl/test.cfg");
	    PersistentEngine engine = 
		new PersistentEngine (cfg, logger, "persistent-engine");

	    HCPeer peer = new HCPeer (engine);
	    engine.create (new HC("6000000000000001"));
	    engine.create (new HC("6000000000000002"));
	    engine.create (new HC("6000000000000003"));
	    engine.create (new HC("6000000000000004"));
	    engine.create (new HC("6000000000000005"));
	    engine.create (new HC("6000000000000006"));
	    engine.create (new HC("6000000000000007"));
	    engine.create (new HC("6000000000000008"));
	    engine.create (new HC("6000000000000009"));
	    engine.create (new HC("6000000000000010"));
	    engine.remove (new HC("6000000000000005"));

	    Iterator iter = 
		peer.findByRange ("6000000000000002", "6000000000000008")
		    .iterator();
	    while (iter.hasNext()) {
		HC nf = (HC) iter.next();
		System.out.println ("> " +nf.getPan());
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}

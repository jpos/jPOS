package isoserver;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOServer;
import org.jpos.iso.ISOSource;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ServerChannel;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.channel.ASCIIChannel;
import org.jpos.iso.channel.LoopbackChannel;
import org.jpos.iso.packager.ISO87APackager;

import org.jpos.util.Logger;
import org.jpos.util.LogSource;
import org.jpos.util.SimpleLogListener;
import org.jpos.util.ThreadPool;

public class Test implements ISORequestListener {

    public boolean process (ISOSource source, ISOMsg m) {
	try {
	    m.setResponseMTI();
	    m.set (new ISOField(39, "99"));
	    source.send(m);
	} catch (Exception e) {
	    return false;
	}
	return true;
    }

    public static void main(String args[]) {
	try {
	    Logger logger = new Logger();
	    logger.addListener (new SimpleLogListener (System.out));

	    ISOChannel clientSideChannel = new ASCIIChannel 
		(new ISO87APackager()); // see jpos.cfg
	    ISOChannel serverSideChannel = new LoopbackChannel();
	    if (serverSideChannel instanceof LogSource) 
		((LogSource)serverSideChannel).
		    setLogger (logger, "iso-server.server-side");

	    ThreadPool pool = new ThreadPool (5, 30);
	    pool.setLogger (logger, "iso-server-pool");

	    ISOServer server = 
		new ISOServer (
		    8000, (ServerChannel) clientSideChannel, pool);

	    server.setLogger (logger, "iso-server");
	    server.addISORequestListener (new Test());
	    new Thread (server).start();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}

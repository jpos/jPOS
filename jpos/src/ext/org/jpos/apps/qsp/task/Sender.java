package org.jpos.apps.qsp.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOMUX;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISORequest;
import org.jpos.iso.packager.XMLPackager;
import org.jpos.util.SimpleLogSource;
import org.jpos.util.NameRegistrar;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.core.Configuration;
import org.jpos.core.Configurable;
import org.jpos.core.ConfigurationException;

/**
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class Sender 
    extends SimpleLogSource
    implements Runnable, Configurable
{
    ISOMUX mux;
    File message;
    long initialDelay;
    long delay;
    int  waitForResponse;
    Logger logger;
    String realm;
    ISOPackager packager;

    public Sender() {
	super();
    }

    public void setConfiguration (Configuration cfg) 
	throws ConfigurationException
    {
	try {
	    mux = ISOMUX.getMUX (cfg.get ("mux"));
	    message = new File (cfg.get ("message"));
	    initialDelay    = cfg.getLong ("initial-delay");
	    waitForResponse = cfg.getInt  ("wait-for-response");
	    delay           = cfg.getLong ("delay");
	    packager        = new XMLPackager();
	} catch (NameRegistrar.NotFoundException e) {
	    throw new ConfigurationException (e);
	} catch (ISOException e) {
	    throw new ConfigurationException (e);
	}
    }

    public void run () {
	if (initialDelay > 0)
	    try {
		Thread.sleep (initialDelay);
	    } catch (InterruptedException e) { }

	for (;;) {
	    LogEvent evt = new LogEvent (this, "sender-run");
	    try {
		sendOne (evt);
	    } catch (Throwable e) {
		evt.addMessage (e);
	    } finally  {
		Logger.log (evt);
	    }
	    if (delay > 0)
		try {
		    Thread.sleep (delay);
		} catch (InterruptedException e) { }
	}
    }

    private void sendOne(LogEvent evt) throws IOException {
	FileInputStream fis = new FileInputStream (message);
	try {
	    byte[] b = new byte[fis.available()];
	    fis.read (b);
	    ISOMsg m = new ISOMsg ();
	    m.setPackager (packager);
	    m.unpack (b);
	    evt.addMessage (m);
	    ISORequest req = new ISORequest (m);
	    mux.queue (req);
	    if (waitForResponse > 0) {
		ISOMsg resp = req.getResponse (waitForResponse);
		if (resp != null)
		    evt.addMessage (resp);
	    }
	} catch (ISOException e) {
	    evt.addMessage (e);
    	} finally {
	    fis.close();
	}
    }
}

package qsp.dummydirpoll;

import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.SimpleLogSource;
import org.jpos.util.DirPoll;
import org.jpos.util.DirPoll.Processor;
import org.jpos.util.DirPoll.DirPollException;
import org.jpos.core.ReConfigurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;

/**
 * Dummy DirPoll.Processor
 * @author apr@cs.com.uy
 * @version $Revision$ $Date$
 */
public class DummyProcessor extends SimpleLogSource 
    implements Processor, ReConfigurable
{
    public byte[] process(String name, byte[] request) throws DirPollException
    {
	Logger.log (new LogEvent (this, "dummy-processor", name));
	return ("response: "+ new String(request)).getBytes();
    }
    public void setConfiguration (Configuration cfg) 
	throws ConfigurationException
    {
	// dummy ReConfigurable (just to check re-config requests from QSP)
	Logger.log (
	    new LogEvent (this, "dummy-processor","setConfiguration called")
	);
    }
}

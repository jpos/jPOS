package simplemux;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.util.Logger;
import org.jpos.util.LogSource;
import org.jpos.util.LogEvent;
import java.util.Observable;

/**
 * DummyRequestListener receives ISOMUX's unmatched requests,
 * generate a LogEvent and discard them.
 *
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class DummyRequestListener extends Observable implements ISORequestListener
{
    LogSource logSource;
    public DummyRequestListener (LogSource logSource) {
	this.logSource = logSource;
    }
    public void process (ISOMsg m) {
	setChanged();
	notifyObservers();
	Logger.log (
	    new LogEvent (logSource, "dummy-request-listener", m)
	);
    }
}

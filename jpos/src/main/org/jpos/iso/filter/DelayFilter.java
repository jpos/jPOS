package org.jpos.iso.filter;

import org.jpos.iso.ISOFilter;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOChannel;
import org.jpos.util.LogEvent;
import org.jpos.core.ReConfigurable;
import org.jpos.core.Configuration;


/**
 * DelayFilter can be used in order to
 * slow down an ISOChannel. Usefull while
 * debugging an application or simulating a server
 */
public class DelayFilter implements ISOFilter, ReConfigurable {
    int delay;
    public DelayFilter() {
	super();
	delay = 0;
    }
   /**
    * @param delay desired delay, expressed in milliseconds
    */
    public DelayFilter(int delay) {
	super();
	this.delay = delay;
    }
   /**
    * @param cfg
    * <ul>
    * <li>delay (expressed in milliseconds)
    * </ul>
    */
    public void setConfiguration (Configuration cfg) {
	delay = cfg.getInt ("delay");
    }

    public ISOMsg filter (ISOChannel channel, ISOMsg m, LogEvent evt) 
    {
	evt.addMessage ("<delay-filter delay=\""+delay+"\"/>");
	if (delay > 0) {
	    try {
		Thread.sleep (delay);
	    } catch (InterruptedException e) { }
	}
	return m;
    }
}


package uy.com.cs.jpos.util;

/*
 * $Log$
 * Revision 1.2  2000/02/03 00:41:55  apr
 * .
 *
 * Revision 1.1  2000/01/30 23:32:53  apr
 * pre-Alpha - CVS sync
 *
 */

/**
 * @author apr@cs.com.uy
 * @since jPOS 1.1
 * @version $Id$
 */

import java.lang.Math;
import java.util.Map;
import java.util.List;
import java.util.Hashtable;
import java.util.Vector;
import uy.com.cs.jpos.util.LockManager.Ticket;

public class SimpleLockManager implements LockManager {
    Map locks;

    public SimpleLockManager () {
	locks = new Hashtable();
    }

    public class SimpleTicket implements Ticket {
	String resourceName;
	long expiration;
	public SimpleTicket (String resourceName, long duration) {
	    this.resourceName = resourceName;
	    this.expiration = System.currentTimeMillis() + duration;
	}
	public boolean renew (long duration) {
	    if (!isExpired()) {
		this.expiration = System.currentTimeMillis() + duration;
		return true;
	    }
	    return false;
	}
	public long getExpiration() {
	    return expiration;
	}
	public boolean isExpired() {
	    return System.currentTimeMillis() > expiration;
	}
	public String getResourceName () {
	    return resourceName;
	}
	public void cancel() {
	    expiration = 0;
	    locks.remove (resourceName);
	    synchronized (this) {
		notify();
	    }
	}
	public String toString() {
	    return super.toString() 
		+ "[" + resourceName + "/" +isExpired() + "/"
		+ (expiration - System.currentTimeMillis()) + "ms left]";
	}
    }
    public Ticket lock (String resourceName, long duration, long wait)
    {
	long maxWait = System.currentTimeMillis() + wait;

	while (System.currentTimeMillis() < maxWait) {
	    Ticket t = null;
	    synchronized (this) {
		t = (Ticket) locks.get (resourceName);
		if (t == null) {
		    t = new SimpleTicket (resourceName, duration);
		    locks.put (resourceName, t);
		    return t;
		} 
		else if (t.isExpired()) {
		    t.cancel();
		    continue;
		}
	    }
	    synchronized (t) {
		try {
		    t.wait (Math.min (1000, wait));
		} catch (InterruptedException e) { }
	    }
	}
	return null;
    }
}

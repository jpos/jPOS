package org.jpos.util;

/*
 * $Log$
 * Revision 1.2  2000/03/01 14:44:45  apr
 * Changed package name to org.jpos
 *
 * Revision 1.1  2000/01/30 23:32:52  apr
 * pre-Alpha - CVS sync
 *
 */

/**
 * @author apr@cs.com.uy
 * @since jPOS 1.1
 * @version $Id$
 */
public interface LockManager {
    public interface Ticket {
	public boolean renew (long duration);
	public long getExpiration();
	public boolean isExpired();
	public void cancel();
    }
    public Ticket lock (String resourceName, long duration, long wait);
}
    

package uy.com.cs.jpos.util;

/*
 * $Log$
 * Revision 1.1  2000/02/03 00:49:47  apr
 * Added DefaultLockManager support/minor changes
 *
 */

/**
 * @author apr@cs.com.uy
 * @since jPOS 1.1
 * @version $Id$
 * Singleton that enable access to a DefaultLockManager
 * (current implementation defaults to SimpleLockManager)
 */
public class DefaultLockManager {
    private static LockManager instance;
    private DefaultLockManager() { }
    public static synchronized LockManager getInstance() {
	if (instance == null)
	    instance = new SimpleLockManager();
	return instance;
    }
    public static void setLockManager (LockManager mgr) {
	instance = mgr;
    }
}


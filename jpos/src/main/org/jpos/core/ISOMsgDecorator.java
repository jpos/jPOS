/*
 * $Log$
 * Revision 1.1  1999/10/08 12:53:59  apr
 * Devel intermediate version - CVS sync
 *
 */

package uy.com.cs.jpos.core;


import uy.com.cs.jpos.iso.ISOMsg;

/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @since jPOS 1.1
 *
 * CardAgent uses Decorator to build a suitable ISOMsg for
 * the specified transaction.
 *
 * @see CardAgent
 * @see CardTransaction
 */
public interface ISOMsgDecorator {
    /**
     * CardAgent calls a list of ISOMsgDecorators
     * @return true means &quot;call me later&quot;
     */
    public boolean decorate (ISOMsg m, CardTransaction t);
}

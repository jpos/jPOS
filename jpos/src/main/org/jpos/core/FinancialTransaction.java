/*
 * $Log$
 * Revision 1.4  2000/03/01 14:44:38  apr
 * Changed package name to org.jpos
 *
 * Revision 1.3  2000/01/26 21:48:50  apr
 * CVS sync
 *
 * Revision 1.2  2000/01/23 16:12:10  apr
 * CVS devel sync
 *
 * Revision 1.1  2000/01/20 23:02:46  apr
 * Adding FinancialTransaction support - CVS sync
 *
 */

package org.jpos.core;

import java.io.*;
import java.math.*;
import java.util.*;
import org.jpos.util.Loggeable;

/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @since jPOS 1.1
 *
 * @see CardTransaction
 * @see CardAgentLookup
 * @see CardHolder
 */
public class FinancialTransaction extends AuthorizationTransaction
{
    public FinancialTransaction() {
	super();
    }
    public FinancialTransaction (String action) {
	setAction (action);
    }
    protected String getTagName() {
	return "FinancialTransaction";
    }
}

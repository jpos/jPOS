/*
 * $Log$
 * Revision 1.4  1999/11/26 12:16:48  apr
 * CVS devel snapshot
 *
 * Revision 1.3  1999/10/08 12:53:58  apr
 * Devel intermediate version - CVS sync
 *
 * Revision 1.2  1999/09/26 22:31:58  apr
 * CVS sync
 *
 * Revision 1.1  1999/09/26 19:54:06  apr
 * jPOS core 0.0.1 - setting up artifacts
 *
 */

package uy.com.cs.jpos.core;

import java.io.*;
import java.util.*;

/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @since jPOS 1.1
 *
 * @see CardAgent
 * @see CardAgentLookup
 * @see CardHolder
 */
public interface CardTransaction extends Serializable {
    public String getAction();
}

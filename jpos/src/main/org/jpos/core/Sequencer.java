
/*
 * $Log$
 * Revision 1.2  2000/03/01 14:44:38  apr
 * Changed package name to org.jpos
 *
 * Revision 1.1  1999/09/26 22:32:01  apr
 * CVS sync
 *
 */

package org.jpos.core;

import java.io.*;
import java.util.*;

/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @since jPOS 1.1
 *
 * Multipurpose sequencer.<br>
 * CardAgents requires persistent sequence number<br>
 * Sequencer interface isolate from particular DB implementations
 */
public interface Sequencer {
    public int get (String counterName);
    public int get (String counterName, int add);
    public int set (String counterName, int value);
}


/*
 * $Log$
 * Revision 1.1  1999/09/26 22:32:01  apr
 * CVS sync
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
 * Multipurpose sequencer.<br>
 * CardAgents requires persistent sequence number<br>
 * Sequencer interface isolate from particular DB implementations
 */
public interface Sequencer {
    public int get (String counterName);
    public int get (String counterName, int add);
    public int set (String counterName, int value);
}

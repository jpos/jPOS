/*
 * $Log$
 * Revision 1.1  2000/01/23 16:12:10  apr
 * CVS devel sync
 *
 */

package uy.com.cs.jpos.core;

import java.io.*;
import java.util.*;

/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @since jPOS 1.1
 */
public interface BatchManager {
    public void fetch (CardTransaction t) throws IOException;
    public void sync  (CardTransaction t) throws IOException;
}

package org.jpos.util;


import java.io.*;
import java.util.*;

/**
 * @author apr@cs.com.uy
 * @version $Id$
 */
public interface LogListener extends EventListener {
    public void log (LogEvent ev);
}


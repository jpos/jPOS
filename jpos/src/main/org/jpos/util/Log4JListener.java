/*
 * Copyright (c) 2000 jPOS.org.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the jPOS project 
 *    (http://www.jpos.org/)". Alternately, this acknowledgment may 
 *    appear in the software itself, if and wherever such third-party 
 *    acknowledgments normally appear.
 *
 * 4. The names "jPOS" and "jPOS.org" must not be used to endorse 
 *    or promote products derived from this software without prior 
 *    written permission. For written permission, please contact 
 *    license@jpos.org.
 *
 * 5. Products derived from this software may not be called "jPOS",
 *    nor may "jPOS" appear in their name, without prior written
 *    permission of the jPOS project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  
 * IN NO EVENT SHALL THE JPOS PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS 
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the jPOS Project.  For more
 * information please see <http://www.jpos.org/>.
 */

package org.jpos.util;

import java.io.*;
import java.util.*;
import org.jpos.core.Configuration;
import org.jpos.core.ReConfigurable;
import org.jpos.core.ConfigurationException;

import org.apache.log4j.*;
import org.apache.log4j.xml.*;

/**
 * @author Eoin P. FLood</a>
 * @version $Revision$ $Date$
 * @see org.jpos.core.LogListener
 * @see org.jpos.core.LogEvent
 * @since jPOS 1.3
 *
 * This class acts as a simple bridge between jPOS's logging system
 * and log4j.
 * The jPOS <code>realm</code> is used as the log4j <code>Category</code>
 * and messages are by default logged with the DEBUG priority. This can
 * be changed by calling <code>setPriority</code>
 */

public class Log4JListener implements LogListener, ReConfigurable
{
    private Priority _priority;

    /** 
     * Create a new Log4JLisener with DEBUG priority.
     */
    public Log4JListener ()
    {
    	setPriority (Priority.DEBUG_INT);
    }

    public void setPriority (int priority)
    {
    	_priority = Priority.toPriority (priority);
    }

    public void setPriority (String priority)
    {
    	_priority = Priority.toPriority (priority);
    }

    public void close() 
    {
    }

    /**
     * Expects the following properties:
     * <ul>
     *  <li>config   - Configuration file path
     *  <li>priority - Log4J priority (debug, info, warn, error)
     *  <li>watch    - interval (in ms) to monitor XML config file for changes 
     * </ul>
     */
    public void setConfiguration (Configuration cfg) 
        throws ConfigurationException
    {
        DOMConfigurator.configureAndWatch (
            cfg.get ("config"), cfg.getLong ("watch")
        );
        setPriority (cfg.get ("priority"));
    }

    public synchronized void log (LogEvent ev) 
    {
	Category cat = Category.getInstance(ev.getRealm());
    	if (cat.isEnabledFor(_priority))
	{
	    ByteArrayOutputStream w = new ByteArrayOutputStream();
	    PrintStream p = new PrintStream (w);
	    ev.dump (p, "");
	    cat.log (_priority, w.toString());
	}
    }
}

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

import java.io.PrintStream;
import java.util.Date;
import java.util.Hashtable;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;


/**
 * A specific log listener that filters logs based on
 * their priorities,
 * priorities are ordered as follows: TRACE < DEBUG < INFO < WARN < ERROR < FATAL
 * default priority is Log.INFO
 * @author <a href="mailto:taherkordy@dpi2.dpi.net.ir">Alireza Taherkordi</a>
 * @version $Revision$ $Date$
 */
public class FilterLogListener implements LogListener,Configurable
{

    private static Hashtable levels;

    static{
            levels = new Hashtable();
            levels.put(Log.TRACE, new Integer(1));
            levels.put(Log.DEBUG, new Integer(2));
            levels.put(Log.INFO, new Integer(3));
            levels.put(Log.WARN, new Integer(4));
            levels.put(Log.ERROR, new Integer(5));
            levels.put(Log.FATAL, new Integer(6));
    }

    private String priority = Log.INFO;
    PrintStream p;

    public FilterLogListener() {
        super();
        p = System.out;
    }

    public FilterLogListener(PrintStream p) {
        super();
        setPrintStream(p);
    }

    public void setConfiguration (Configuration cfg)
        throws ConfigurationException
    {
        try {
            String log_priority = cfg.get("priority");
            if ( (log_priority != null) && (!log_priority.trim().equals("")) )
            {
                if (levels.containsKey(log_priority))
                    priority = log_priority;
            }
        } catch (Exception e) {
            throw new ConfigurationException (e);
        }
    }



    public synchronized void setPrintStream(PrintStream p) {
        this.p = p;
    }

    public synchronized void close() {
        if (p != null) {
            p.close();
            p = null;
        }
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public boolean permitLogging(String tagLevel)
    {
        Integer I = (Integer)levels.get(tagLevel);

        if (I == null)
            I = (Integer)levels.get(Log.INFO);

        Integer J = (Integer)levels.get(priority);

        return ( I.intValue() >= J.intValue() );
    }

    public synchronized LogEvent log(LogEvent ev) {
        if (p != null) {
            if (permitLogging(ev.tag))
            {
                Date d = new Date();
                p.println(
                        "<log realm=\"" + ev.getRealm() + "\" at=\"" + d.toString()
                        + "." + d.getTime() % 1000 + "\">"
                );
                ev.dump(p, "  ");
                p.println("</log>");
                p.flush();
            }
        }
        return ev;
    }
}

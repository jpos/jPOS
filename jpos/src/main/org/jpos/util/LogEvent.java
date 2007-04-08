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

import java.io.IOException;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.util.EventObject;
import java.util.Iterator;
import java.util.Vector;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @serial
 */
public class LogEvent {
   /**
    * @serial
    */
    LogSource source;
   /**
    * @serial
    */
    public String tag;
   /**
    * @serial
    */
    public Vector payLoad;
    
    public LogEvent () {
        super();
        this.tag = "info";
        this.payLoad = new Vector(1);
    }
    public LogEvent (String tag) {
        super();
        this.tag = tag;
        this.payLoad = new Vector(1);
    }
    public LogEvent (String tag, Object msg) {
        super();
        this.source  = source;
        this.tag     = tag;
        this.payLoad = new Vector(1);
        addMessage(msg);
    }
    public LogEvent (LogSource source, String tag) {
        super ();
        this.source  = source;
        this.tag     = tag;
        this.payLoad = new Vector();
    }
    public LogEvent (LogSource source, String tag, Object msg) {
        super ();
        this.source  = source;
        this.tag     = tag;
        this.payLoad = new Vector(1);
        addMessage(msg);
    }
    public void addMessage (Object msg) {
        this.payLoad.addElement(msg);
    }
    public void addMessage (String tagname, String message) {
        this.payLoad.addElement("<"+tagname+">"+message+"</"+tagname+">");
    }
    /**
     * @return log source (may be null)
     */
    public LogSource getSource() {
        return source;
    }
    /**
     * @param source a LogSource
     */
    public void setSource(LogSource source) {
        this.source = source;
    }
    public void dump (PrintStream p, String indent) {
        if (payLoad.size() == 0) 
            p.println (indent + "<" + tag + "/>");
        else {
            p.println (indent + "<" + tag + ">");
            String newIndent = indent + "  ";
            Iterator i = payLoad.iterator();
            while (i.hasNext()) {
                Object o = i.next();
                if (o instanceof Loggeable) 
                    ((Loggeable)o).dump (p, newIndent);
                else if (o instanceof SQLException) {
                    SQLException e = (SQLException) o;
                    p.println (newIndent + "<SQLException>"
                                +e.getMessage() + "</SQLException>");
                    p.println (newIndent + "<SQLState>"
                                +e.getSQLState() + "</SQLState>");
                    p.println (newIndent + "<VendorError>"
                                +e.getErrorCode() + "</VendorError>");
                    ((Throwable)o).printStackTrace (p);
                }
                else if (o instanceof Throwable) {
                    p.println (newIndent + "<exception name=\""
                        + ((Throwable)o).getMessage() + "\">");
                    p.print (newIndent);
                    ((Throwable)o).printStackTrace (p);
                    p.println (newIndent + "</exception>");
                }
                else if (o instanceof Object[]) {
                    Object [] oa = (Object[]) o;
                    p.print (newIndent + "[");
                    for (int j=0; j<oa.length; j++) {
                        if (j>0)
                            p.print (",");
                        p.print (oa[j].toString());
                    }
                    p.println ("]");
                } 
                else if (o instanceof Element) {
                    p.println ("");
                    p.println (newIndent + "<![CDATA[");
                    XMLOutputter out = new XMLOutputter (" ", true);
                    try {
                        out.output ((Element) o, p);
                    } catch (IOException ex) {
                        ex.printStackTrace (p);
                    }
                    p.println ("");
                    p.println (newIndent + "]]>");
                }
                else if (o != null) {
                    p.println (newIndent + o.toString());
                } else {
                    p.println (newIndent + "null");
                }
            }
            p.println (indent + "</" + tag + ">");
        }
    }
    public String getRealm() {
        return source.getRealm();
    }
    public Vector getPayLoad() {
        return payLoad;
    }
    public String toString() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream p = new PrintStream (baos);
        dump (p, "");
        return baos.toString();
    }
}


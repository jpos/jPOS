/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2008 Alejandro P. Revilla
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Date;
import java.util.Vector;

import org.jdom.Element;
import org.jdom.output.Format;
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
    protected String dumpHeader (PrintStream p, String indent) {
        Date d = new Date();
        p.println (indent + "<log realm=\"" +getRealm()+ "\" at=\""+d.toString()
           +"." + d.getTime() % 1000 + "\">"
        );
        return indent + "  ";
    }
    protected void dumpTrailer (PrintStream p, String indent) {
        p.println (indent + "</log>");
    }
    public void dump (PrintStream p, String outer) {
        String indent = dumpHeader (p, outer);
        if (payLoad.size() == 0) {
            if (tag != null)
                p.println (indent + "<" + tag + "/>");
        }
        else {
            String newIndent;
            if (tag != null) {
                p.println (indent + "<" + tag + ">");
                newIndent = indent + "  ";
            } else
                newIndent = "";
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
                    XMLOutputter out = new XMLOutputter (Format.getPrettyFormat ());
                    out.getFormat().setLineSeparator ("\n");
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
            if (tag != null)
                p.println (indent + "</" + tag + ">");
        }
        dumpTrailer (p, outer);
    }
    public String getRealm() {
        return source != null ? source.getRealm() : "";
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


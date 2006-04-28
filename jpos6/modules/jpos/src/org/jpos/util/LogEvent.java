/*
 * Copyright (c) 2006 jPOS.org
 *
 * See terms of license at http://jpos.org/license.html
 *
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


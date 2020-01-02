/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
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

package org.jpos.iso;

import org.jpos.util.Loggeable;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Signals that an ISO exception of some sort has occurred. 
 *
 * @author  <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class ISOException extends Exception implements Loggeable {

    private static final long serialVersionUID = -777216335204861186L;
    /**
     * @serial
     */
    Throwable nested = null;

    /**
     * Constructs an <code>ISOException</code> with no detail message. 
     */
    public ISOException() {
        super();
    }

    /**
     * Constructs an <code>ISOException</code> with the specified detail 
     * message. 
     *
     * @param   s   the detail message.
     */
    public ISOException(String s) {
        super(s);
    }

    /**
     * Constructs an <code>ISOException</code> with a nested
     * exception
     * @param nested another exception
     */
    public ISOException (Throwable nested) {
        super(nested.toString());
        this.nested = nested;
    }

    /**
     * Constructs an <code>ISOException</code> with a detail Message nested
     * exception
     * @param   s   the detail message.
     * @param nested another exception
     */
    public ISOException (String s, Throwable nested) {
        super(s);
        this.nested = nested;
    }

    /**
     * @return nested exception (may be null)
     */
    public Throwable getNested() {
        return nested;
    }

    protected String getTagName() {
        return "iso-exception";
    }
    public void dump (PrintStream p, String indent) {
        String inner = indent + "  ";
        p.println (indent + "<"+getTagName()+">");
        p.println (inner  + getMessage());
        if (nested != null) {
            if (nested instanceof ISOException) 
                ((ISOException)nested).dump (p, inner);
            else {
                p.println (inner + "<nested-exception>");
                p.print   (inner);
                nested.printStackTrace (p);
                p.println (inner + "</nested-exception>");
            }
        }
        p.print (inner);
        printStackTrace (p);
        p.println (indent + "</"+getTagName()+">");
    }
    public String toString() {
        StringBuilder buf = new StringBuilder (super.toString());
        if (nested != null) {
            buf.append (" (");
            buf.append (nested.toString());
            buf.append (")");
        }
        return buf.toString();
    }

    public void printStackTrace() {
        super.printStackTrace();
        if (nested != null) {
            System.err.print("Nested:");
            nested.printStackTrace();
        }
    }

    public void printStackTrace(PrintStream ps) {
        super.printStackTrace(ps);
        if (nested != null) {
            ps.print("Nested:");
            nested.printStackTrace(ps);
        }
    }

    public void printStackTrace(PrintWriter pw) {
        super.printStackTrace(pw);
        if (nested != null) {
            pw.print("Nested:");
            nested.printStackTrace(pw);
        }
    }
}

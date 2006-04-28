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

package org.jpos.iso;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.jpos.util.Loggeable;

/**
 * Signals that an ISO exception of some sort has occurred. 
 *
 * @author  <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class ISOException extends Exception implements Loggeable {
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
        StringBuffer buf = new StringBuffer (super.toString());
        if (nested != null)
            buf.append (" (" + nested.toString() +")");
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

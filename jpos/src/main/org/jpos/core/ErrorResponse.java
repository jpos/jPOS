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

/*
 * $Log$
 * Revision 1.10  2003/10/13 10:46:15  apr
 * tabs expanded to spaces
 *
 * Revision 1.9  2003/05/16 04:07:35  alwyns
 * Import cleanups.
 *
 * Revision 1.8  2000/11/02 12:09:18  apr
 * Added license to every source file
 *
 * Revision 1.7  2000/08/26 21:50:06  apr
 * Changed dump
 *
 * Revision 1.6  2000/03/02 12:31:01  apr
 * Get rid of javadoc warnings - done
 *
 * Revision 1.5  2000/03/01 14:44:38  apr
 * Changed package name to org.jpos
 *
 * Revision 1.4  2000/01/30 23:33:51  apr
 * CVS sync/backup - intermediate version
 *
 * Revision 1.3  2000/01/20 23:02:46  apr
 * Adding FinancialTransaction support - CVS sync
 *
 * Revision 1.2  1999/12/20 10:46:26  apr
 * Added setAuthoritative
 *
 * Revision 1.1  1999/11/26 12:16:49  apr
 * CVS devel snapshot
 *
 */

package org.jpos.core;

import java.io.PrintStream;

import org.jpos.util.Loggeable;

/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @since jPOS 1.1
 *
 * @see CardTransaction
 * @see CardTransactionResponse
 */
public class ErrorResponse implements CardTransactionResponse, Loggeable {
   /**
    * @serial
    */
    String code;

   /**
    * @serial
    */
    String message;

   /**
    * @serial
    */
    boolean authoritative = true;

   /**
    * @serial
    */
    boolean canContinue   = false;

    public ErrorResponse() {
        super();
    }
    public ErrorResponse (String code, String message) {
        super();
        this.code = code;
        this.message = message;
    }
    public ErrorResponse 
        (String code, String message, boolean authoritative, 
        boolean canContinue)
    {
        super();
        this.code = code;
        this.message = message;
        this.authoritative = authoritative;
        this.canContinue   = canContinue;
    }
    public byte[] getImage() throws CardAgentException {
        return null;
    }
    public String  getAutCode() {
        return code;
    }
    public String  getMessage() {
        return message;
    }
    public String  getAutNumber() {
        return null;
    }
    public boolean isApproved() {
        return false;
    }
    public boolean canContinue() {
        return canContinue;
    }
    public boolean isAuthoritative() {
        return authoritative;
    }
    public void setAuthoritative (boolean authoritative) {
        this.authoritative = authoritative;
    }
    public void setContinue (boolean canContinue) {
        this.canContinue = canContinue;
    }
    public void setAutCode (String code) {
        this.code = code;
    }
    public void setMessage (String message) {
        this.message = message;
    }
    public void setAutCode (String code, String message) {
        this.code = code;
        this.message = message;
    }
    public String getBatchName () {
        return null;
    }

    public void dump (PrintStream p, String indent) {
        String inner = indent + "  ";
        p.print (indent + "<error-response");
        if (isApproved())
            p.print (" approved=\"true\"");
        if (canContinue())
            p.print (" can-continue=\"true\"");
        if (isAuthoritative())
            p.print (" authoritative=\"true\"");
        p.println (">");

        p.println (inner  + "<aut-code>"+getAutCode()+"</aut-code>");
        String autNumber = getAutNumber();
        if (autNumber != null)
            p.println (inner  + "<aut-number>"+autNumber+"</aut-number>");
        String message = getMessage();
        if (message != null)
            p.println (inner  + "<aut-message>"+message+"</aut-message>");

        p.println (indent + "</error-response>");
    }
}

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

package org.jpos.core;

import org.jpos.util.Loggeable;

import java.io.PrintStream;

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

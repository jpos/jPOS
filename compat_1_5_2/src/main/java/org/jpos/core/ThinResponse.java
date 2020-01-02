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
 * Many Agents will provide custom CardTransactionResponse 
 * implementations that may contain not serializable fields
 * or although serializables there are cases were there's no 
 * need to increase message size with a big payload. 
 * <br>
 * ThinResponse can be initialized from such an object
 * providing remote clients with information regarding
 * this particular CardTransactionResponse
 * (suitable for remote POS systems using a proxy to connect
 * to jPOS Agents)
 */
public class ThinResponse implements CardTransactionResponse, Loggeable {

   /**
    * @serial
    */
    public String code;

   /**
    * @serial
    */
    public String message;

   /**
    * @serial
    */
    public String autNumber;

   /**
    * @serial
    */
    public boolean authoritative;

   /**
    * @serial
    */
    public boolean canContinue;

   /**
    * @serial
    */
    public boolean approved;

    public ThinResponse(CardTransactionResponse c) {
        super();
        code          = c.getAutCode();
        message       = c.getMessage();
        autNumber     = c.getAutNumber();
        authoritative = c.isAuthoritative();
        canContinue   = c.canContinue();
        approved      = c.isApproved();
    }
    public ThinResponse () {
        super();
    }
    public byte[] getImage() throws CardAgentException {
        return null;
    }
    public void setAutCode (String code) {
        this.code = code;
    }
    public String  getAutCode() {
        return code;
    }
    public void setMessage (String message) {
        this.message = message;
    }
    public String  getMessage() {
        return message;
    }
    public void setAutNumber (String autNumber) {
        this.autNumber = autNumber;
    }
    public String  getAutNumber() {
        return autNumber;
    }
    public void setApproved (boolean approved) {
        this.approved = approved;
    }
    public boolean isApproved() {
        return approved;
    }
    public void setContinue (boolean canContinue) {
        this.canContinue = canContinue;
    }
    public boolean canContinue() {
        return canContinue;
    }
    public void setAuthoritative (boolean authoritative) {
        this.authoritative = authoritative;
    }
    public boolean isAuthoritative() {
        return authoritative;
    }
    public void dump (PrintStream p, String indent) {
        String inner = indent + "  ";
        p.print (indent + "<thin-response");
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
        p.println (inner  + "<aut-message>"+getMessage()+"</aut-message>");
        p.println (indent + "</thin-response>");
    }
    public String getBatchName () {
        return null;
    }
}

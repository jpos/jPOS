/*
 * $Log$
 * Revision 1.2  1999/12/17 18:05:21  apr
 * BugFix: setAuthoritative param
 *
 * Revision 1.1  1999/11/26 12:16:51  apr
 * CVS devel snapshot
 *
 */

package uy.com.cs.jpos.core;

import java.io.*;
import java.util.*;

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
public class ThinResponse implements CardTransactionResponse {
    public String code;
    public String message;
    public String autNumber;
    public boolean authoritative;
    public boolean canContinue;
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
}

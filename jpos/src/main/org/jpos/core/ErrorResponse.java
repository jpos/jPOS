/*
 * $Log$
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

import java.io.*;
import java.util.*;
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
	p.print (indent + "<ErrorResponse");
	if (isApproved())
	    p.print (" APPROVED");
	if (canContinue())
	    p.print (" CANCONTINUE");
	if (isAuthoritative())
	    p.print (" AUTHORITATIVE");
	p.println (">");

	p.println (inner  + "<autCode>"+getAutCode()+"</autCode>");
	String autNumber = getAutNumber();
	if (autNumber != null)
	    p.println (inner  + "<autNumber>"+autNumber+"</autNumber>");
	p.println (inner  + "<msg>"+getMessage()+"</msg>");
	p.println (indent + "</ErrorResponse>");
    }
}

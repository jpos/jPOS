/*
 * $Log$
 * Revision 1.2  1999/12/20 10:46:26  apr
 * Added setAuthoritative
 *
 * Revision 1.1  1999/11/26 12:16:49  apr
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
 * @see CardTransaction, CardTransactionResponse
 */
public class ErrorResponse implements CardTransactionResponse {
    String code;
    String message;
    boolean authoritative = true;
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
}

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
 * Revision 1.10  2000/11/02 12:09:18  apr
 * Added license to every source file
 *
 * Revision 1.9  2000/04/16 23:53:06  apr
 * LogProducer renamed to LogSource
 *
 * Revision 1.8  2000/03/02 12:31:01  apr
 * Get rid of javadoc warnings - done
 *
 * Revision 1.7  2000/03/01 14:44:38  apr
 * Changed package name to org.jpos
 *
 * Revision 1.6  2000/01/26 21:48:47  apr
 * CVS sync
 *
 * Revision 1.5  2000/01/20 23:02:44  apr
 * Adding FinancialTransaction support - CVS sync
 *
 * Revision 1.4  2000/01/11 01:24:39  apr
 * moved non ISO-8583 related classes from jpos.iso to jpos.util package
 * (AntiHog LeasedLineModem LogEvent LogListener LogSource
 *  Loggeable Logger Modem RotateLogListener SimpleAntiHog SimpleDialupModem
 *  SimpleLogListener SimpleLogSource SystemMonitor V24)
 *
 * Revision 1.3  1999/12/14 00:46:03  apr
 * Added purchasePlan/numberOfPayments support
 *
 * Revision 1.2  1999/11/26 12:16:43  apr
 * CVS devel snapshot
 *
 * Revision 1.1  1999/10/08 12:53:55  apr
 * Devel intermediate version - CVS sync
 *
 * Revision 1.2  1999/09/26 22:31:58  apr
 * CVS sync
 *
 * Revision 1.1  1999/09/26 19:54:06  apr
 * jPOS core 0.0.1 - setting up artifacts
 *
 */

package org.jpos.core;

import java.io.*;
import java.math.*;
import java.util.*;
import org.jpos.util.Loggeable;

/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @since jPOS 1.1
 *
 * @see CardTransaction
 * @see CardAgentLookup
 * @see CardHolder
 * @serial
 */
public class AuthorizationTransaction
    implements CardTransaction, Loggeable
{
    /**
     * @serial
     */
    public CardHolder cardHolder;
    /**
     * @serial
     */
    public BigDecimal amount;
    /**
     * @serial
     */
    public Integer currency;
    /**
     * @serial
     */
    public String rrn;
    /**
     * @serial
     */
    public String terminal;
    /**
     * @serial
     */
    public String purchasePlan;
    /**
     * @serial
     */
    public byte numberOfPayments;
    /**
     * @serial
     */
    protected String action;
    /**
     * @serial
     */
    protected String[] args = { "" };

    private transient CardTransactionResponse response;

    public AuthorizationTransaction() {
	cardHolder = null;
	amount     = null;
	rrn        = null;
	terminal   = null;
	currency   = null;
	purchasePlan = null;
	numberOfPayments = 1;
	action     = "authorize";
    }
    public void setCardHolder (CardHolder cardHolder) {
	this.cardHolder = cardHolder;
    }
    public CardHolder getCardHolder() {
	return cardHolder;
    }
    public void setAmount (BigDecimal amount) {
	this.amount = amount;
    }
    public BigDecimal getAmount() {
	return amount;
    }
    public void setCurrency (Integer currency) {
	this.currency = currency;
    }
    public void setCurrency (int m) {
	this.currency = new Integer (m);
    }
    public Integer getCurrency() {
	return currency;
    }
    public void setRRN (String rrn) {
	this.rrn = rrn;
    }
    public String getRRN() {
	return rrn;
    }
    public void setTerminal (String terminal) {
	this.terminal = terminal;
    }
    public String getTerminal () {
	return terminal;
    }
    public void setAction (String action) {
	this.action = action;
    }
    public void setArgs (String[] args) {
	this.args = args;
    }
    public String getAction() {
	return action;
    }
    public String[] getArgs() {
	return args;
    }
    public void setPurchasePlan(String purchasePlan) {
	this.purchasePlan = purchasePlan;
    }
    public String getPurchasePlan () {
	return purchasePlan;
    }
    public void setNumberOfPayments(int n) {
	this.numberOfPayments = (byte) n;
    }
    public int getNumberOfPayments() {
	return (int) numberOfPayments;
    }
    public void dump (PrintStream p, String indent) {
	String inner = indent + "  ";
	p.println (indent + "<" + getTagName() + ">");
	p.println (inner  + "<action>" + action + "</action>");
	getCardHolder().dump (p, inner);
	p.println (inner  + "<amount>"      + amount      + "</amount>");
	p.println (inner  + "<currency>"    + currency     + "</currency>");
	p.println (inner  + "<terminal>"    + terminal     + "</terminal>");
	p.println (inner  + "<purchasePlan>"+ purchasePlan + "</purchasePlan>");
	p.println (inner  + "<numberOfPayments>" + numberOfPayments + 
			   "</numberOfPayments>"
	);
	if (response != null && response instanceof Loggeable)
	    ((Loggeable)response).dump (p, inner);
	p.println (indent + "</" + getTagName() + ">");
    }
    protected String getTagName() {
	return "AuthorizationTransaction";
    }
    public void setResponse (CardTransactionResponse response) {
	this.response = response;
    }
    public CardTransactionResponse getResponse () {
	return response;
    }
}

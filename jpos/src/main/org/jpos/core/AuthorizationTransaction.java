/*
 * $Log$
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
 * (AntiHog LeasedLineModem LogEvent LogListener LogProducer
 *  Loggeable Logger Modem RotateLogListener SimpleAntiHog SimpleDialupModem
 *  SimpleLogListener SimpleLogProducer SystemMonitor V24)
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
 */
public class AuthorizationTransaction
    implements CardTransaction, Loggeable
{
    public CardHolder cardHolder;
    public BigDecimal amount;
    public Integer currency;
    public String rrn;
    public String terminal;
    public String purchasePlan;
    public byte numberOfPayments;

    protected String action;
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

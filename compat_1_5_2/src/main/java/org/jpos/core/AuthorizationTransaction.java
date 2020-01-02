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
import java.math.BigDecimal;

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

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

package org.jpos.iso.packager;

import org.jpos.iso.*;

/**
 * ISO 8583 v1993 Binary Packager<br>
 * <b>WARNING UNTESTED</b>
 *
 * @author <a href="mailto:u_arunkumar@yahoo.com">Arun Kumar U</a>
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Id$
 * @see ISOPackager
 * @see ISOBasePackager
 * @see ISOComponent
 */
public class ISO93BPackager extends ISOBasePackager {
    private static final boolean pad = false;
    protected ISOFieldPackager fld[] = {
    /*000*/ new IFB_NUMERIC (  4, "Message Type Indicator", pad),
    /*001*/ new IFB_BITMAP  ( 16, "Bitmap"),
    /*002*/ new IFB_LLNUM   ( 19, "Primary Account number", pad),
    /*003*/ new IFB_NUMERIC (  6, "Processing Code", pad),
    /*004*/ new IFB_NUMERIC ( 12, "Amount, Transaction", pad),
    /*005*/ new IFB_NUMERIC ( 12, "Amount, Reconciliation", pad),
    /*006*/ new IFB_NUMERIC ( 12, "Amount, Cardholder billing", pad),
    /*007*/ new IFB_NUMERIC ( 10, "Date and time, transmission", pad),
    /*008*/ new IFB_NUMERIC (  8, "Amount, Cardholder billing fee", pad),
    /*009*/ new IFB_NUMERIC (  8, "Conversion rate, Reconciliation", pad),
    /*010*/ new IFB_NUMERIC (  8, "Conversion rate, Cardholder billing", pad),
    /*011*/ new IFB_NUMERIC (  6, "Systems trace audit number", pad),
    /*012*/ new IFB_NUMERIC ( 12, "Date and time, Local transaction", pad),
    /*013*/ new IFB_NUMERIC (  4, "Date, Effective", pad),
    /*014*/ new IFB_NUMERIC (  4, "Date, Expiration", pad),
    /*015*/ new IFB_NUMERIC (  6, "Date, Settlement", pad),
    /*016*/ new IFB_NUMERIC (  4, "Date, Conversion", pad),
    /*017*/ new IFB_NUMERIC (  4, "Date, Capture", pad),
    /*018*/ new IFB_NUMERIC (  4, "Merchant type", pad),
    /*019*/ new IFB_NUMERIC (  3, "Country code, Acquiring institution", pad),
    /*020*/ new IFB_NUMERIC (  3, "Country code, Primary account number", pad),
    /*021*/ new IFB_NUMERIC (  3, "Country code, Forwarding institution", pad),
    /*022*/ new IF_CHAR     ( 12, "Point of service data code"),
    /*023*/ new IFB_NUMERIC (  3, "Card sequence number", pad),
    /*024*/ new IFB_NUMERIC (  3, "Function code", pad),
    /*025*/ new IFB_NUMERIC (  4, "Message reason code", pad),
    /*026*/ new IFB_NUMERIC (  4, "Card acceptor business code", pad),
    /*027*/ new IFB_NUMERIC (  1, "Approval code length", pad),
    /*028*/ new IFB_NUMERIC (  6, "Date, Reconciliation", pad),
    /*029*/ new IFB_NUMERIC (  3, "Reconciliation indicator", pad),
    /*030*/ new IFB_NUMERIC ( 24, "Amounts, original", pad),
    /*031*/ new IFB_LLCHAR  ( 99, "Acquirer reference data"),
    /*032*/ new IFB_LLNUM   ( 11, "Acquirer institution ident code", pad),
    /*033*/ new IFB_LLNUM   ( 11, "Forwarding institution ident code", pad),
    /*034*/ new IFB_LLCHAR  ( 28, "Primary account number, extended"),
    /*035*/ new IFB_LLCHAR  ( 37, "Track 2 data"),
    /*036*/ new IFB_LLLCHAR (104, "Track 3 data"),
    /*037*/ new IF_CHAR     ( 12, "Retrieval reference number"),
    /*038*/ new IF_CHAR     (  6, "Approval code"),
    /*039*/ new IFB_NUMERIC (  3, "Action code", pad),
    /*040*/ new IFB_NUMERIC (  3, "Service code", pad),
    /*041*/ new IF_CHAR     (  8, "Card acceptor terminal identification"),
    /*042*/ new IF_CHAR     ( 15, "Card acceptor identification code"),
    /*043*/ new IFB_LLCHAR  ( 99, "Card acceptor name/location"),
    /*044*/ new IFB_LLCHAR  ( 99, "Additional response data"),
    /*045*/ new IFB_LLCHAR  ( 76, "Track 1 data"),
    /*046*/ new IFB_LLLCHAR (204, "Amounts, Fees"),
    /*047*/ new IFB_LLLCHAR (999, "Additional data - national"),
    /*048*/ new IFB_LLLCHAR (999, "Additional data - private"),
    /*049*/ new IF_CHAR     (  3, "Currency code, Transaction"),
    /*050*/ new IF_CHAR     (  3, "Currency code, Reconciliation"),
    /*051*/ new IF_CHAR     (  3, "Currency code, Cardholder billing"),
    /*052*/ new IFB_BINARY  (  8, "Personal identification number (PIN) data"),
    /*053*/ new IFB_LLBINARY( 48, "Security related control information"),
    /*054*/ new IFB_LLLCHAR (120, "Amounts, additional"),
    /*055*/ new IFB_LLLBINARY(255,"IC card system related data"),
    /*056*/ new IFB_LLNUM   ( 35, "Original data elements", pad),
    /*057*/ new IFB_NUMERIC (  3, "Authorization life cycle code", pad),
    /*058*/ new IFB_LLNUM   ( 11, "Authorizing agent institution Id Code", pad),
    /*059*/ new IFB_LLLCHAR (999, "Transport data"),
    /*060*/ new IFB_LLLCHAR (999, "Reserved for national use"),
    /*061*/ new IFB_LLLCHAR (999, "Reserved for national use"),
    /*062*/ new IFB_LLLCHAR (999, "Reserved for private use"),
    /*063*/ new IFB_LLLCHAR (999, "Reserved for private use"),
    /*064*/ new IFB_BINARY  (  8, "Message authentication code field"),
    /*065*/ new IFB_BINARY  (  8, "Reserved for ISO use"),
    /*066*/ new IFB_LLLCHAR (204, "Amounts, original fees"),
    /*067*/ new IFB_NUMERIC (  2, "Extended payment data", pad),
    /*068*/ new IFB_NUMERIC (  3, "Country code, receiving institution", pad),
    /*069*/ new IFB_NUMERIC (  3, "Country code, settlement institution", pad),
    /*070*/ new IFB_NUMERIC (  3, "Country code, authorizing agent Inst.", pad),
    /*071*/ new IFB_NUMERIC (  8, "Message number", pad),
    /*072*/ new IFB_LLLCHAR (999, "Data record"),
    /*073*/ new IFB_NUMERIC (  6, "Date, action", pad),
    /*074*/ new IFB_NUMERIC ( 10, "Credits, number", pad),
    /*075*/ new IFB_NUMERIC ( 10, "Credits, reversal number", pad),
    /*076*/ new IFB_NUMERIC ( 10, "Debits, number", pad),
    /*077*/ new IFB_NUMERIC ( 10, "Debits, reversal number", pad),
    /*078*/ new IFB_NUMERIC ( 10, "Transfer, number", pad),
    /*079*/ new IFB_NUMERIC ( 10, "Transfer, reversal number", pad),
    /*080*/ new IFB_NUMERIC ( 10, "Inquiries, number", pad),
    /*081*/ new IFB_NUMERIC ( 10, "Authorizations, number", pad),
    /*082*/ new IFB_NUMERIC ( 10, "Inquiries, reversal number", pad),
    /*083*/ new IFB_NUMERIC ( 10, "Payments, number", pad),
    /*084*/ new IFB_NUMERIC ( 10, "Payments, reversal number", pad),
    /*085*/ new IFB_NUMERIC ( 10, "Fee collections, number", pad),
    /*086*/ new IFB_NUMERIC ( 16, "Credits, amount", pad),
    /*087*/ new IFB_NUMERIC ( 16, "Credits, reversal amount", pad),
    /*088*/ new IFB_NUMERIC ( 16, "Debits, amount", pad),
    /*089*/ new IFB_NUMERIC ( 16, "Debits, reversal amount", pad),
    /*090*/ new IFB_NUMERIC ( 10, "Authorizations, reversal number", pad),
    /*091*/ new IFB_NUMERIC (  3, "Country code, transaction Dest. Inst.", pad),
    /*092*/ new IFB_NUMERIC (  3, "Country code, transaction Orig. Inst.", pad),
    /*093*/ new IFB_LLNUM   ( 11, "Transaction Dest. Inst. Id code", pad),
    /*094*/ new IFB_LLNUM   ( 11, "Transaction Orig. Inst. Id code", pad),
    /*095*/ new IFB_LLCHAR  ( 99, "Card issuer reference data"),
    /*096*/ new IFB_LLLBINARY(999,"Key management data"),
    /*097*/ new IFB_AMOUNT  (1+16,"Amount, Net reconciliation", pad),
    /*098*/ new IF_CHAR     ( 25, "Payee"),
    /*099*/ new IFB_LLCHAR  ( 11, "Settlement institution Id code"),
    /*100*/ new IFB_LLNUM   ( 11, "Receiving institution Id code", pad),
    /*101*/ new IFB_LLCHAR  ( 17, "File name"),
    /*102*/ new IFB_LLCHAR  ( 28, "Account identification 1"),
    /*103*/ new IFB_LLCHAR  ( 28, "Account identification 2"),
    /*104*/ new IFB_LLLCHAR (100, "Transaction description"),
    /*105*/ new IFB_NUMERIC ( 16, "Credits, Chargeback amount", pad),
    /*106*/ new IFB_NUMERIC ( 16, "Debits, Chargeback amount", pad),
    /*107*/ new IFB_NUMERIC ( 10, "Credits, Chargeback number", pad),
    /*108*/ new IFB_NUMERIC ( 10, "Debits, Chargeback number", pad),
    /*109*/ new IFB_LLCHAR  ( 84, "Credits, Fee amounts"),
    /*110*/ new IFB_LLCHAR  ( 84, "Debits, Fee amounts"),
    /*111*/ new IFB_LLLCHAR (999, "Reserved for ISO use"),
    /*112*/ new IFB_LLLCHAR (999, "Reserved for ISO use"),
    /*113*/ new IFB_LLLCHAR (999, "Reserved for ISO use"),
    /*114*/ new IFB_LLLCHAR (999, "Reserved for ISO use"),
    /*115*/ new IFB_LLLCHAR (999, "Reserved for ISO use"),
    /*116*/ new IFB_LLLCHAR (999, "Reserved for national use"),
    /*117*/ new IFB_LLLCHAR (999, "Reserved for national use"),
    /*118*/ new IFB_LLLCHAR (999, "Reserved for national use"),
    /*119*/ new IFB_LLLCHAR (999, "Reserved for national use"),
    /*120*/ new IFB_LLLCHAR (999, "Reserved for national use"),
    /*121*/ new IFB_LLLCHAR (999, "Reserved for national use"),
    /*122*/ new IFB_LLLCHAR (999, "Reserved for national use"),
    /*123*/ new IFB_LLLCHAR (999, "Reserved for private use"),
    /*124*/ new IFB_LLLCHAR (999, "Reserved for private use"),
    /*125*/ new IFB_LLLCHAR (999, "Reserved for private use"),
    /*126*/ new IFB_LLLCHAR (999, "Reserved for private use"),
    /*127*/ new IFB_LLLCHAR (999, "Reserved for private use"),
    /*128*/ new IFB_BINARY  (  8, "Message authentication code field")
    };
    public ISO93BPackager() {
        super();
        setFieldPackager(fld);
    }
}

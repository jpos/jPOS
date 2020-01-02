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
 * ISO 8583 v1987 ASCII Packager but using Binary Bitmap.
 *
 * All fields ascii except binary bitmap.
 *
 * @author Alwyn Schoeman`
 * @version $Revision$ $Date$
 * @see ISOPackager
 * @see ISOBasePackager
 * @see ISOComponent
 */
public class ISO87APackagerBBitmap extends ISOBasePackager {
    protected ISOFieldPackager fld[] = {
    /*000*/ new IFA_NUMERIC (  4, "MESSAGE TYPE INDICATOR"),
    /*001*/ new IFB_BITMAP  ( 16, "BIT MAP"),
    /*002*/ new IFA_LLNUM   ( 19, "PAN - PRIMARY ACCOUNT NUMBER"),
    /*003*/ new IFA_NUMERIC (  6, "PROCESSING CODE"),
    /*004*/ new IFA_NUMERIC ( 12, "AMOUNT, TRANSACTION"),
    /*005*/ new IFA_NUMERIC ( 12, "AMOUNT, SETTLEMENT"),
    /*006*/ new IFA_NUMERIC ( 12, "AMOUNT, CARDHOLDER BILLING"),
    /*007*/ new IFA_NUMERIC ( 10, "TRANSMISSION DATE AND TIME"),
    /*008*/ new IFA_NUMERIC (  8, "AMOUNT, CARDHOLDER BILLING FEE"),
    /*009*/ new IFA_NUMERIC (  8, "CONVERSION RATE, SETTLEMENT"),
    /*010*/ new IFA_NUMERIC (  8, "CONVERSION RATE, CARDHOLDER BILLING"),
    /*011*/ new IFA_NUMERIC (  6, "SYSTEM TRACE AUDIT NUMBER"),
    /*012*/ new IFA_NUMERIC (  6, "TIME, LOCAL TRANSACTION"),
    /*013*/ new IFA_NUMERIC (  4, "DATE, LOCAL TRANSACTION"),
    /*014*/ new IFA_NUMERIC (  4, "DATE, EXPIRATION"),
    /*015*/ new IFA_NUMERIC (  4, "DATE, SETTLEMENT"),
    /*016*/ new IFA_NUMERIC (  4, "DATE, CONVERSION"),
    /*017*/ new IFA_NUMERIC (  4, "DATE, CAPTURE"),
    /*018*/ new IFA_NUMERIC (  4, "MERCHANTS TYPE"),
    /*019*/ new IFA_NUMERIC (  3, "ACQUIRING INSTITUTION COUNTRY CODE"),
    /*020*/ new IFA_NUMERIC (  3, "PAN EXTENDED COUNTRY CODE"),
    /*021*/ new IFA_NUMERIC (  3, "FORWARDING INSTITUTION COUNTRY CODE"),
    /*022*/ new IFA_NUMERIC (  3, "POINT OF SERVICE ENTRY MODE"),
    /*023*/ new IFA_NUMERIC (  3, "CARD SEQUENCE NUMBER"),
    /*024*/ new IFA_NUMERIC (  3, "NETWORK INTERNATIONAL IDENTIFIEER"),
    /*025*/ new IFA_NUMERIC (  2, "POINT OF SERVICE CONDITION CODE"),
    /*026*/ new IFA_NUMERIC (  2, "POINT OF SERVICE PIN CAPTURE CODE"),
    /*027*/ new IFA_NUMERIC (  1, "AUTHORIZATION IDENTIFICATION RESP LEN"),
    /*028*/ new IFA_AMOUNT  (  9, "AMOUNT, TRANSACTION FEE"),
    /*029*/ new IFA_AMOUNT  (  9, "AMOUNT, SETTLEMENT FEE"),
    /*030*/ new IFA_AMOUNT  (  9, "AMOUNT, TRANSACTION PROCESSING FEE"),
    /*031*/ new IFA_AMOUNT  (  9, "AMOUNT, SETTLEMENT PROCESSING FEE"),
    /*032*/ new IFA_LLNUM   ( 11, "ACQUIRING INSTITUTION IDENT CODE"),
    /*033*/ new IFA_LLNUM   ( 11, "FORWARDING INSTITUTION IDENT CODE"),
    /*034*/ new IFA_LLCHAR  ( 28, "PAN EXTENDED"),
    /*035*/ new IFA_LLNUM   ( 37, "TRACK 2 DATA"),
    /*036*/ new IFA_LLLCHAR (104, "TRACK 3 DATA"),
    /*037*/ new IF_CHAR     ( 12, "RETRIEVAL REFERENCE NUMBER"),
    /*038*/ new IF_CHAR     (  6, "AUTHORIZATION IDENTIFICATION RESPONSE"),
    /*039*/ new IF_CHAR     (  2, "RESPONSE CODE"),
    /*040*/ new IF_CHAR     (  3, "SERVICE RESTRICTION CODE"),
    /*041*/ new IF_CHAR     (  8, "CARD ACCEPTOR TERMINAL IDENTIFICACION"),
    /*042*/ new IF_CHAR     ( 15, "CARD ACCEPTOR IDENTIFICATION CODE" ),
    /*043*/ new IF_CHAR     ( 40, "CARD ACCEPTOR NAME/LOCATION"),
    /*044*/ new IFA_LLCHAR  ( 25, "ADITIONAL RESPONSE DATA"),
    /*045*/ new IFA_LLCHAR  ( 76, "TRACK 1 DATA"),
    /*046*/ new IFA_LLLCHAR (999, "ADITIONAL DATA - ISO"),
    /*047*/ new IFA_LLLCHAR (999, "ADITIONAL DATA - NATIONAL"),
    /*048*/ new IFA_LLLCHAR (999, "ADITIONAL DATA - PRIVATE"),
    /*049*/ new IF_CHAR     (  3, "CURRENCY CODE, TRANSACTION"),
    /*050*/ new IF_CHAR     (  3, "CURRENCY CODE, SETTLEMENT"),
    /*051*/ new IF_CHAR     (  3, "CURRENCY CODE, CARDHOLDER BILLING"   ),
    /*052*/ new IFA_BINARY  (  8, "PIN DATA"   ),
    /*053*/ new IFA_NUMERIC ( 16, "SECURITY RELATED CONTROL INFORMATION"),
    /*054*/ new IFA_LLLCHAR (120, "ADDITIONAL AMOUNTS"),
    /*055*/ new IFA_LLLCHAR (999, "RESERVED ISO"),
    /*056*/ new IFA_LLLCHAR (999, "RESERVED ISO"),
    /*057*/ new IFA_LLLCHAR (999, "RESERVED NATIONAL"),
    /*058*/ new IFA_LLLCHAR (999, "RESERVED NATIONAL"),
    /*059*/ new IFA_LLLCHAR (999, "RESERVED NATIONAL"),
    /*060*/ new IFA_LLLCHAR (999, "RESERVED PRIVATE"),
    /*061*/ new IFA_LLLCHAR (999, "RESERVED PRIVATE"),
    /*062*/ new IFA_LLLCHAR (999, "RESERVED PRIVATE"),
    /*063*/ new IFA_LLLCHAR (999, "RESERVED PRIVATE"),
    /*064*/ new IFA_BINARY  (  8, "MESSAGE AUTHENTICATION CODE FIELD"),
    /*065*/ new IFA_BINARY  (  1, "BITMAP, EXTENDED"),
    /*066*/ new IFA_NUMERIC (  1, "SETTLEMENT CODE"),
    /*067*/ new IFA_NUMERIC (  2, "EXTENDED PAYMENT CODE"),
    /*068*/ new IFA_NUMERIC (  3, "RECEIVING INSTITUTION COUNTRY CODE"),
    /*069*/ new IFA_NUMERIC (  3, "SETTLEMENT INSTITUTION COUNTRY CODE"),
    /*070*/ new IFA_NUMERIC (  3, "NETWORK MANAGEMENT INFORMATION CODE"),
    /*071*/ new IFA_NUMERIC (  4, "MESSAGE NUMBER"),
    /*072*/ new IFA_NUMERIC (  4, "MESSAGE NUMBER LAST"),
    /*073*/ new IFA_NUMERIC (  6, "DATE ACTION"),
    /*074*/ new IFA_NUMERIC ( 10, "CREDITS NUMBER"),
    /*075*/ new IFA_NUMERIC ( 10, "CREDITS REVERSAL NUMBER"),
    /*076*/ new IFA_NUMERIC ( 10, "DEBITS NUMBER"),
    /*077*/ new IFA_NUMERIC ( 10, "DEBITS REVERSAL NUMBER"),
    /*078*/ new IFA_NUMERIC ( 10, "TRANSFER NUMBER"),
    /*079*/ new IFA_NUMERIC ( 10, "TRANSFER REVERSAL NUMBER"),
    /*080*/ new IFA_NUMERIC ( 10, "INQUIRIES NUMBER"),
    /*081*/ new IFA_NUMERIC ( 10, "AUTHORIZATION NUMBER"),
    /*082*/ new IFA_NUMERIC ( 12, "CREDITS, PROCESSING FEE AMOUNT"),
    /*083*/ new IFA_NUMERIC ( 12, "CREDITS, TRANSACTION FEE AMOUNT"),
    /*084*/ new IFA_NUMERIC ( 12, "DEBITS, PROCESSING FEE AMOUNT"),
    /*085*/ new IFA_NUMERIC ( 12, "DEBITS, TRANSACTION FEE AMOUNT"),
    /*086*/ new IFA_NUMERIC ( 16, "CREDITS, AMOUNT"),
    /*087*/ new IFA_NUMERIC ( 16, "CREDITS, REVERSAL AMOUNT"),
    /*088*/ new IFA_NUMERIC ( 16, "DEBITS, AMOUNT"),
    /*089*/ new IFA_NUMERIC ( 16, "DEBITS, REVERSAL AMOUNT"),
    /*090*/ new IFA_NUMERIC ( 42, "ORIGINAL DATA ELEMENTS"),
    /*091*/ new IF_CHAR     (  1, "FILE UPDATE CODE"),
    /*092*/ new IF_CHAR     (  2, "FILE SECURITY CODE"),
    /*093*/ new IF_CHAR     (  6, "RESPONSE INDICATOR"),
    /*094*/ new IF_CHAR     (  7, "SERVICE INDICATOR"),
    /*095*/ new IF_CHAR     ( 42, "REPLACEMENT AMOUNTS"),
    /*096*/ new IFA_BINARY  ( 16, "MESSAGE SECURITY CODE"),
    /*097*/ new IFA_AMOUNT  ( 17, "AMOUNT, NET SETTLEMENT"),
    /*098*/ new IF_CHAR     ( 25, "PAYEE"),
    /*099*/ new IFA_LLNUM   ( 11, "SETTLEMENT INSTITUTION IDENT CODE"),
    /*100*/ new IFA_LLNUM   ( 11, "RECEIVING INSTITUTION IDENT CODE"),
    /*101*/ new IFA_LLCHAR  ( 17, "FILE NAME"),
    /*102*/ new IFA_LLCHAR  ( 28, "ACCOUNT IDENTIFICATION 1"),
    /*103*/ new IFA_LLCHAR  ( 28, "ACCOUNT IDENTIFICATION 2"),
    /*104*/ new IFA_LLLCHAR (100, "TRANSACTION DESCRIPTION"),
    /*105*/ new IFA_LLLCHAR (999, "RESERVED ISO USE"), 
    /*106*/ new IFA_LLLCHAR (999, "RESERVED ISO USE"), 
    /*107*/ new IFA_LLLCHAR (999, "RESERVED ISO USE"), 
    /*108*/ new IFA_LLLCHAR (999, "RESERVED ISO USE"), 
    /*109*/ new IFA_LLLCHAR (999, "RESERVED ISO USE"), 
    /*110*/ new IFA_LLLCHAR (999, "RESERVED ISO USE"), 
    /*111*/ new IFA_LLLCHAR (999, "RESERVED ISO USE"), 
    /*112*/ new IFA_LLLCHAR (999, "RESERVED NATIONAL USE"),
    /*113*/ new IFA_LLLCHAR (999, "RESERVED NATIONAL USE"),
    /*114*/ new IFA_LLLCHAR (999, "RESERVED NATIONAL USE"   ),
    /*115*/ new IFA_LLLCHAR (999, "RESERVED NATIONAL USE"),
    /*116*/ new IFA_LLLCHAR (999, "RESERVED NATIONAL USE"  ),
    /*117*/ new IFA_LLLCHAR (999, "RESERVED NATIONAL USE"),
    /*118*/ new IFA_LLLCHAR (999, "RESERVED NATIONAL USE"),
    /*119*/ new IFA_LLLCHAR (999, "RESERVED NATIONAL USE"),
    /*120*/ new IFA_LLLCHAR (999, "RESERVED PRIVATE USE"),
    /*121*/ new IFA_LLLCHAR (999, "RESERVED PRIVATE USE"),
    /*122*/ new IFA_LLLCHAR (999, "RESERVED PRIVATE USE"),
    /*123*/ new IFA_LLLCHAR (999, "RESERVED PRIVATE USE"),
    /*124*/ new IFA_LLLCHAR (999, "RESERVED PRIVATE USE"),
    /*125*/ new IFA_LLLCHAR (999, "RESERVED PRIVATE USE"),
    /*126*/ new IFA_LLLCHAR (999, "RESERVED PRIVATE USE"),
    /*127*/ new IFA_LLLCHAR (999, "RESERVED PRIVATE USE"),
    /*128*/ new IFA_BINARY  (  8, "MAC 2"),
        };
    public ISO87APackagerBBitmap() {
        super();
        setFieldPackager(fld);
    }
}

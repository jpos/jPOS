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
 * ISO 8583 v1987 BINARY Packager 
 * customized for VISA's VAP Single Message
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @deprecated use Base1* stuff instead
 * @see ISOPackager
 * @see ISOBasePackager
 * @see ISOComponent
 */
public class VAPSMSPackager extends ISOBasePackager {
    private static final boolean pad = true;
    protected ISOFieldPackager fld[] = {
            new IFB_NUMERIC (  4, "MESSAGE TYPE INDICATOR", true),
            new IFB_BITMAP  ( 16, "BIT MAP"),
            new IFB_LLHNUM  ( 19, "PAN - PRIMARY ACCOUNT NUMBER", pad),
            new IFB_NUMERIC (  6, "PROCESSING CODE", true),
            new IFB_NUMERIC ( 12, "AMOUNT, TRANSACTION", true),
            new IFB_NUMERIC ( 12, "AMOUNT, SETTLEMENT", true),
            new IFB_NUMERIC ( 12, "AMOUNT, CARDHOLDER BILLING", true),
            new IFB_NUMERIC ( 10, "TRANSMISSION DATE AND TIME", true),
            new IFB_NUMERIC (  8, "AMOUNT, CARDHOLDER BILLING FEE", true),
            new IFB_NUMERIC (  8, "CONVERSION RATE, SETTLEMENT", true),
            new IFB_NUMERIC (  8, "CONVERSION RATE, CARDHOLDER BILLING", true),
            new IFB_NUMERIC (  6, "SYSTEM TRACE AUDIT NUMBER", true),
            new IFB_NUMERIC (  6, "TIME, LOCAL TRANSACTION", true),
            new IFB_NUMERIC (  4, "DATE, LOCAL TRANSACTION", true),
            new IFB_NUMERIC (  4, "DATE, EXPIRATION", true),
            new IFB_NUMERIC (  4, "DATE, SETTLEMENT", true),
            new IFB_NUMERIC (  4, "DATE, CONVERSION", true),
            new IFB_NUMERIC (  4, "DATE, CAPTURE", true),
            new IFB_NUMERIC (  4, "MERCHANTS TYPE", true),
            new IFB_NUMERIC (  3, "ACQUIRING INSTITUTION COUNTRY CODE", true),
            new IFB_NUMERIC (  3, "PAN EXTENDED COUNTRY CODE", true),
            new IFB_NUMERIC (  3, "FORWARDING INSTITUTION COUNTRY CODE", true),
            new IFB_NUMERIC (  4, "POINT OF SERVICE ENTRY MODE", true),
            new IFB_NUMERIC (  3, "CARD SEQUENCE NUMBER", true),
            new IFB_NUMERIC (  3, "NETWORK INTERNATIONAL IDENTIFIEER", true),
            new IFB_NUMERIC (  2, "POINT OF SERVICE CONDITION CODE", true),
            new IFB_NUMERIC (  2, "POINT OF SERVICE PIN CAPTURE CODE", true),
            new IFB_NUMERIC (  1, "AUTHORIZATION IDENTIFICATION RESP LEN",true),
            new IFB_AMOUNT  (  9, "AMOUNT, TRANSACTION FEE", true),
            new IFB_AMOUNT  (  9, "AMOUNT, SETTLEMENT FEE", true),
            new IFB_AMOUNT  (  9, "AMOUNT, TRANSACTION PROCESSING FEE", true),
            new IFB_AMOUNT  (  9, "AMOUNT, SETTLEMENT PROCESSING FEE", true),
            new IFB_LLHNUM  ( 11, "ACQUIRING INSTITUTION IDENT CODE", pad),
            new IFB_LLHNUM  ( 11, "FORWARDING INSTITUTION IDENT CODE", pad),
            new IFB_LLHNUM  ( 28, "PAN EXTENDED", pad),
            new IFB_LLHNUM  ( 37, "TRACK 2 DATA", pad),
            new IFB_LLLNUM  (104, "TRACK 3 DATA", pad),
            new IFE_CHAR    ( 12, "RETRIEVAL REFERENCE NUMBER"),
            new IFE_CHAR    (  6, "AUTHORIZATION IDENTIFICATION RESPONSE"),
            new IFE_CHAR    (  2, "RESPONSE CODE"),
            new IFE_CHAR    (  3, "SERVICE RESTRICTION CODE"),
            new IFE_CHAR    (  8, "CARD ACCEPTOR TERMINAL IDENTIFICACION"),
            new IFE_CHAR    ( 15, "CARD ACCEPTOR IDENTIFICATION CODE" ),
            new IFE_CHAR    ( 40, "CARD ACCEPTOR NAME/LOCATION"),
            new IFB_LLHECHAR( 25, "ADITIONAL RESPONSE DATA"),
            new IFB_LLCHAR  ( 76, "TRACK 1 DATA"),
            new IFB_LLCHAR  ( 99, "ADITIONAL DATA - ISO"),
            new IFB_LLCHAR  ( 99, "ADITIONAL DATA - NATIONAL"),
            new IFB_LLHCHAR ( 99, "ADITIONAL DATA - PRIVATE"),
            new IFB_NUMERIC (  3, "CURRENCY CODE, TRANSACTION", true),
            new IFB_NUMERIC (  3, "CURRENCY CODE, SETTLEMENT", true),
            new IFB_NUMERIC (  3, "CURRENCY CODE, CARDHOLDER BILLING", true),
            new IFB_BINARY  (  8, "PIN DATA"   ),
            new IFB_NUMERIC ( 16, "SECURITY RELATED CONTROL INFORMATION", true),
            new IFB_LLHECHAR(120, "ADDITIONAL AMOUNTS"),
            new IFB_LLHBINARY(255, "RESERVED ISO"),
            new IFB_LLCHAR  (255, "RESERVED ISO"),
            new IFB_LLCHAR  (255, "RESERVED NATIONAL"),
            new IFB_LLCHAR  (255, "RESERVED NATIONAL"),
            new IFB_LLHECHAR( 14, "NATIONAL POS GEOGRAPHIC DATA"),
            new IFB_LLHBINARY(  6, "RESERVED PRIVATE"),
            new IFB_LLHNUM  ( 36, "RESERVED PRIVATE", pad),
            new IFB_LLHBINARY( 59, "PAYMENT SERVICE FIELDS"),
            new IFB_LLHBINARY(255, "SMS PRIVATE-USE FIELDS"),
            new IFB_BINARY  (  8, "MESSAGE AUTHENTICATION CODE FIELD"),
            new IFB_BINARY  (  1, "BITMAP, EXTENDED"),
            new IFB_NUMERIC (  1, "SETTLEMENT CODE", true),
            new IFB_NUMERIC (  2, "EXTENDED PAYMENT CODE", true),
            new IFB_NUMERIC (  3, "RECEIVING INSTITUTION COUNTRY CODE", true),
            new IFB_NUMERIC (  3, "SETTLEMENT INSTITUTION COUNTRY CODE", true),
            new IFB_NUMERIC (  3, "NETWORK MANAGEMENT INFORMATION CODE", true),
            new IFB_NUMERIC (  4, "MESSAGE NUMBER", true),
            new IFB_NUMERIC (  4, "MESSAGE NUMBER LAST", true),
            new IFB_NUMERIC (  6, "DATE ACTION", true),
            new IFB_NUMERIC ( 10, "CREDITS NUMBER", true),
            new IFB_NUMERIC ( 10, "CREDITS REVERSAL NUMBER", true),
            new IFB_NUMERIC ( 10, "DEBITS NUMBER", true),
            new IFB_NUMERIC ( 10, "DEBITS REVERSAL NUMBER", true),
            new IFB_NUMERIC ( 10, "TRANSFER NUMBER", true),
            new IFB_NUMERIC ( 10, "TRANSFER REVERSAL NUMBER", true),
            new IFB_NUMERIC ( 10, "INQUIRIES NUMBER", true),
            new IFB_NUMERIC ( 10, "AUTHORIZATION NUMBER", true),
            new IFB_NUMERIC ( 12, "CREDITS, PROCESSING FEE AMOUNT", true),
            new IFB_NUMERIC ( 12, "CREDITS, TRANSACTION FEE AMOUNT", true),
            new IFB_NUMERIC ( 12, "DEBITS, PROCESSING FEE AMOUNT", true),
            new IFB_NUMERIC ( 12, "DEBITS, TRANSACTION FEE AMOUNT", true),
            new IFB_NUMERIC ( 16, "CREDITS, AMOUNT", true),
            new IFB_NUMERIC ( 16, "CREDITS, REVERSAL AMOUNT", true),
            new IFB_NUMERIC ( 16, "DEBITS, AMOUNT", true),
            new IFB_NUMERIC ( 16, "DEBITS, REVERSAL AMOUNT", true),
            new IFB_NUMERIC ( 42, "ORIGINAL DATA ELEMENTS", true),
            new IFE_CHAR    (  1, "FILE UPDATE CODE"),
            new IFE_CHAR    (  2, "FILE SECURITY CODE"),
            new IFE_CHAR    (  5, "RESPONSE INDICATOR"),
            new IFE_CHAR    (  7, "SERVICE INDICATOR"),
            new IFE_CHAR    ( 42, "REPLACEMENT AMOUNTS"),
            new IFB_BINARY  (  8, "MESSAGE SECURITY CODE"),
            new IFB_AMOUNT  ( 17, "AMOUNT, NET SETTLEMENT", pad),
            new IFE_CHAR    ( 25, "PAYEE"),
            new IFB_LLHNUM  ( 11, "SETTLEMENT INSTITUTION IDENT CODE", pad),
            new IFB_LLHNUM  ( 11, "RECEIVING INSTITUTION IDENT CODE", pad),
            new IFB_LLCHAR  ( 17, "FILE NAME"),
            new IFB_LLCHAR  ( 28, "ACCOUNT IDENTIFICATION 1"),
            new IFB_LLCHAR  ( 28, "ACCOUNT IDENTIFICATION 2"),
            new IFB_LLCHAR  (100, "TRANSACTION DESCRIPTION"),
            new IFB_LLCHAR  ( 99, "RESERVED ISO USE"), 
            new IFB_LLCHAR  ( 99, "RESERVED ISO USE"), 
            new IFB_LLCHAR  ( 99, "RESERVED ISO USE"), 
            new IFB_LLCHAR  ( 99, "RESERVED ISO USE"), 
            new IFB_LLCHAR  ( 99, "RESERVED ISO USE"), 
            new IFB_LLCHAR  ( 99, "RESERVED ISO USE"), 
            new IFB_LLCHAR  ( 99, "RESERVED ISO USE"), 
            new IFB_LLCHAR  ( 99, "RESERVED NATIONAL USE"),
            new IFB_LLCHAR  ( 99, "RESERVED NATIONAL USE"),
            new IFB_LLCHAR  ( 99, "RESERVED NATIONAL USE"),
            new IFB_LLCHAR  ( 24, "ADDITIONAL TRACE DATA 1"),
            new IFB_LLCHAR  ( 99, "RESERVED NATIONAL USE"),
            new IFB_LLCHAR  ( 99, "RESERVED NATIONAL USE"),
            new IFB_LLHBINARY( 99, "INTRA-COUNTRY DATA"),
            new IFB_LLCHAR  ( 99, "RESERVED NATIONAL USE"),
            new IFB_LLHNUM  (  4, "RESERVED PRIVATE USE", pad),
            new IFB_LLCHAR  ( 11, "ISSUING INSTITUTION IDENT CODE"),
            new IFB_LLCHAR  ( 13, "REMAINING OPEN-TO-USE"),
            new IFB_LLCHAR  ( 29, "ADDRESS VERIFICATION DATA"),
            new IFB_LLCHAR  (135, "FREE-FORM TEXT-JAPAN"),
            new IFB_LLCHAR  ( 99, "SUPPORTING INFORMATION"),
            new IFB_LLCHAR  ( 99, "RESERVED PRIVATE USE"),
            new IFB_LLCHAR  ( 99, "RESERVED PRIVATE USE"),
            new IFB_BINARY  (  8, "MAC 2")
        };
    public VAPSMSPackager() {
        super();
        setFieldPackager(fld);
    }
}


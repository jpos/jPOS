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

package org.jpos.iso.packager;

import org.jpos.iso.*;
import org.jpos.util.*;
import java.util.*;

/**
 * ISO 8583 v1987 BINARY Packager 
 * customized for VISA's Base1
 * @author <a href="mailto:eoin.flood@orbiscom.com">Eoin Flood</a>
 * @version $Id$
 * @see ISOPackager
 * @see ISOBasePackager
 * @see ISOComponent
 */
public class Base1Packager extends ISOBasePackager 
{
    private static final boolean pad = true;
    protected ISOFieldPackager base1Fld[] = 
    {
/*000*/ new IFB_NUMERIC (  4, "MESSAGE TYPE INDICATOR", true),
/*001*/ new IFB_BITMAP  ( 16, "BIT MAP"),
/*002*/ new IFB_LLHNUM  ( 19, "PAN - PRIMARY ACCOUNT NUMBER", pad),
/*003*/ new IFB_NUMERIC (  6, "PROCESSING CODE", true),
/*004*/ new IFB_NUMERIC ( 12, "AMOUNT, TRANSACTION", true),
/*005*/ new IFB_NUMERIC ( 12, "AMOUNT, SETTLEMENT", true),
/*006*/ new IFB_NUMERIC ( 12, "AMOUNT, CARDHOLDER BILLING", true),
/*007*/ new IFB_NUMERIC ( 10, "TRANSMISSION DATE AND TIME", true),
/*008*/ new IFB_NUMERIC (  8, "AMOUNT, CARDHOLDER BILLING FEE", true),
/*009*/ new IFB_NUMERIC (  8, "CONVERSION RATE, SETTLEMENT", true),
/*010*/ new IFB_NUMERIC (  8, "CONVERSION RATE, CARDHOLDER BILLING", true),
/*011*/ new IFB_NUMERIC (  6, "SYSTEM TRACE AUDIT NUMBER", true),
/*012*/ new IFB_NUMERIC (  6, "TIME, LOCAL TRANSACTION", true),
/*013*/ new IFB_NUMERIC (  4, "DATE, LOCAL TRANSACTION", true),
/*014*/ new IFB_NUMERIC (  4, "DATE, EXPIRATION", true),
/*015*/ new IFB_NUMERIC (  4, "DATE, SETTLEMENT", true),
/*016*/ new IFB_NUMERIC (  4, "DATE, CONVERSION", true),
/*017*/ new IFB_NUMERIC (  4, "DATE, CAPTURE", true),
/*018*/ new IFB_NUMERIC (  4, "MERCHANTS TYPE", true),
/*019*/ new IFB_NUMERIC (  3, "ACQUIRING INSTITUTION COUNTRY CODE", true),
/*020*/ new IFB_NUMERIC (  3, "PAN EXTENDED COUNTRY CODE", true),
/*021*/ new IFB_NUMERIC (  3, "FORWARDING INSTITUTION COUNTRY CODE", true),
/*022*/ new IFB_NUMERIC (  4, "POINT OF SERVICE ENTRY MODE", true),
/*023*/ new IFB_NUMERIC (  3, "CARD SEQUENCE NUMBER", true),
/*024*/ new IFB_NUMERIC (  3, "NETWORK INTERNATIONAL IDENTIFIEER", true),
/*025*/ new IFB_NUMERIC (  2, "POINT OF SERVICE CONDITION CODE", true),
/*026*/ new IFB_NUMERIC (  2, "POINT OF SERVICE PIN CAPTURE CODE", true),
/*027*/ new IFB_NUMERIC (  1, "AUTHORIZATION IDENTIFICATION RESP LEN",true),
/*028*/ new IFB_AMOUNT  (  9, "AMOUNT, TRANSACTION FEE", true),
/*029*/ new IFB_AMOUNT  (  9, "AMOUNT, SETTLEMENT FEE", true),
/*030*/ new IFB_AMOUNT  (  9, "AMOUNT, TRANSACTION PROCESSING FEE", true),
/*031*/ new IFB_AMOUNT  (  9, "AMOUNT, SETTLEMENT PROCESSING FEE", true),
/*032*/ new IFB_LLHNUM  ( 11, "ACQUIRING INSTITUTION IDENT CODE", pad),
/*033*/ new IFB_LLHNUM  ( 11, "FORWARDING INSTITUTION IDENT CODE", pad),
/*034*/ new IFB_LLHNUM  ( 28, "PAN EXTENDED", pad),
/*035*/ new IFB_LLHNUM  ( 37, "TRACK 2 DATA", pad),
/*036*/ new IFB_LLLNUM  (104, "TRACK 3 DATA", pad),
/*037*/ new IF_ECHAR    ( 12, "RETRIEVAL REFERENCE NUMBER"),
/*038*/ new IF_ECHAR    (  6, "AUTHORIZATION IDENTIFICATION RESPONSE"),
/*039*/ new IF_ECHAR    (  2, "RESPONSE CODE"),
/*040*/ new IF_ECHAR    (  3, "SERVICE RESTRICTION CODE"),
/*041*/ new IF_ECHAR    (  8, "CARD ACCEPTOR TERMINAL IDENTIFICACION"),
/*042*/ new IF_ECHAR    ( 15, "CARD ACCEPTOR IDENTIFICATION CODE" ),
/*043*/ new IF_ECHAR    ( 40, "CARD ACCEPTOR NAME/LOCATION"),
/*044*/ new IFB_LLHECHAR( 25, "ADITIONAL RESPONSE DATA"),
/*045*/ new IFB_LLCHAR  ( 76, "TRACK 1 DATA"),
/*046*/ new IFB_LLCHAR  ( 99, "ADITIONAL DATA - ISO"),
/*047*/ new IFB_LLCHAR  ( 99, "ADITIONAL DATA - NATIONAL"),
/*048*/ new IFB_LLHBINARY ( 99, "ADITIONAL DATA - PRIVATE"),
/*049*/ new IFB_NUMERIC (  3, "CURRENCY CODE, TRANSACTION", true),
/*050*/ new IFB_NUMERIC (  3, "CURRENCY CODE, SETTLEMENT", true),
/*051*/ new IFB_NUMERIC (  3, "CURRENCY CODE, CARDHOLDER BILLING", true),
/*052*/ new IFB_BINARY  (  8, "PIN DATA"   ),
/*053*/ new IFB_NUMERIC ( 16, "SECURITY RELATED CONTROL INFORMATION", true),
/*054*/ new IFB_LLHECHAR(120, "ADDITIONAL AMOUNTS"),
/*055*/ new IFB_LLHBINARY(255, "RESERVED ISO"),
/*056*/ new IFB_LLCHAR  (255, "RESERVED ISO"),
/*057*/ new IFB_LLCHAR  (255, "RESERVED NATIONAL"),
/*058*/ new IFB_LLCHAR  (255, "RESERVED NATIONAL"),
/*059*/ new IFB_LLHECHAR ( 14, "NATIONAL POS GEOGRAPHIC DATA"),
/*060*/ new IFB_LLHBINARY(  6, "RESERVED PRIVATE"),
/*061*/ new IFB_LLHNUM  ( 36, "RESERVED PRIVATE", pad),
/*062*/ new IFB_LLHBINARY( 59, "PAYMENT SERVICE FIELDS"),
/*063*/ new IFB_LLHBINARY(255, "SMS PRIVATE-USE FIELDS"),
/*064*/ new IFB_BINARY  (  8, "MESSAGE AUTHENTICATION CODE FIELD"),
/*065*/ new IFB_BINARY  (  1, "BITMAP, EXTENDED"),
/*066*/ new IFB_NUMERIC (  1, "SETTLEMENT CODE", true),
/*067*/ new IFB_NUMERIC (  2, "EXTENDED PAYMENT CODE", true),
/*068*/ new IFB_NUMERIC (  3, "RECEIVING INSTITUTION COUNTRY CODE", true),
/*069*/ new IFB_NUMERIC (  3, "SETTLEMENT INSTITUTION COUNTRY CODE", true),
/*070*/ new IFB_NUMERIC (  3, "NETWORK MANAGEMENT INFORMATION CODE", true),
/*071*/ new IFB_NUMERIC (  4, "MESSAGE NUMBER", true),
/*072*/ new IFB_NUMERIC (  4, "MESSAGE NUMBER LAST", true),
/*073*/ new IFB_NUMERIC (  6, "DATE ACTION", true),
/*074*/ new IFB_NUMERIC ( 10, "CREDITS NUMBER", true),
/*075*/ new IFB_NUMERIC ( 10, "CREDITS REVERSAL NUMBER", true),
/*076*/ new IFB_NUMERIC ( 10, "DEBITS NUMBER", true),
/*077*/ new IFB_NUMERIC ( 10, "DEBITS REVERSAL NUMBER", true),
/*078*/ new IFB_NUMERIC ( 10, "TRANSFER NUMBER", true),
/*079*/ new IFB_NUMERIC ( 10, "TRANSFER REVERSAL NUMBER", true),
/*080*/ new IFB_NUMERIC ( 10, "INQUIRIES NUMBER", true),
/*081*/ new IFB_NUMERIC ( 10, "AUTHORIZATION NUMBER", true),
/*082*/ new IFB_NUMERIC ( 12, "CREDITS, PROCESSING FEE AMOUNT", true),
/*083*/ new IFB_NUMERIC ( 12, "CREDITS, TRANSACTION FEE AMOUNT", true),
/*084*/ new IFB_NUMERIC ( 12, "DEBITS, PROCESSING FEE AMOUNT", true),
/*085*/ new IFB_NUMERIC ( 12, "DEBITS, TRANSACTION FEE AMOUNT", true),
/*086*/ new IFB_NUMERIC ( 16, "CREDITS, AMOUNT", true),
/*087*/ new IFB_NUMERIC ( 16, "CREDITS, REVERSAL AMOUNT", true),
/*088*/ new IFB_NUMERIC ( 16, "DEBITS, AMOUNT", true),
/*089*/ new IFB_NUMERIC ( 16, "DEBITS, REVERSAL AMOUNT", true),
/*090*/ new IFB_NUMERIC ( 42, "ORIGINAL DATA ELEMENTS", true),
/*091*/ new IF_ECHAR    (  1, "FILE UPDATE CODE"),
/*092*/ new IF_ECHAR    (  2, "FILE SECURITY CODE"),
/*093*/ new IF_ECHAR    (  5, "RESPONSE INDICATOR"),
/*094*/ new IF_ECHAR    (  7, "SERVICE INDICATOR"),
/*095*/ new IF_ECHAR    ( 42, "REPLACEMENT AMOUNTS"),
/*096*/ new IFB_BINARY  (  8, "MESSAGE SECURITY CODE"),
/*097*/ new IFB_AMOUNT  ( 17, "AMOUNT, NET SETTLEMENT", pad),
/*098*/ new IF_ECHAR    ( 25, "PAYEE"),
/*099*/ new IFB_LLHNUM  ( 11, "SETTLEMENT INSTITUTION IDENT CODE", pad),
/*100*/ new IFB_LLHNUM  ( 11, "RECEIVING INSTITUTION IDENT CODE", pad),
/*101*/ new IFB_LLHECHAR( 17, "FILE NAME"),
/*102*/ new IFB_LLCHAR  ( 28, "ACCOUNT IDENTIFICATION 1"),
/*103*/ new IFB_LLCHAR  ( 28, "ACCOUNT IDENTIFICATION 2"),
/*104*/ new IFB_LLCHAR  (100, "TRANSACTION DESCRIPTION"),
/*105*/ new IFB_LLCHAR  ( 99, "RESERVED ISO USE"), 
/*106*/ new IFB_LLCHAR  ( 99, "RESERVED ISO USE"), 
/*107*/ new IFB_LLCHAR  ( 99, "RESERVED ISO USE"), 
/*108*/ new IFB_LLCHAR  ( 99, "RESERVED ISO USE"), 
/*109*/ new IFB_LLCHAR  ( 99, "RESERVED ISO USE"), 
/*110*/ new IFB_LLCHAR  ( 99, "RESERVED ISO USE"), 
/*111*/ new IFB_LLCHAR  ( 99, "RESERVED ISO USE"), 
/*112*/ new IFB_LLCHAR  ( 99, "RESERVED NATIONAL USE"),
/*113*/ new IFB_LLCHAR  ( 99, "RESERVED NATIONAL USE"),
/*114*/ new IFB_LLCHAR  ( 99, "RESERVED NATIONAL USE"),
/*115*/ new IFB_LLCHAR  ( 24, "ADDITIONAL TRACE DATA 1"),
/*116*/ new IFB_LLCHAR  ( 99, "RESERVED NATIONAL USE"),
/*117*/ new IFB_LLCHAR  ( 99, "RESERVED NATIONAL USE"),
/*118*/ new IFB_LLHBINARY( 99, "INTRA-COUNTRY DATA"),
/*119*/ new IFB_LLCHAR  ( 99, "RESERVED NATIONAL USE"),
/*120*/ new IFB_LLHNUM  (  4, "RESERVED PRIVATE USE", pad),
/*121*/ new IFB_LLCHAR  ( 11, "ISSUING INSTITUTION IDENT CODE"),
/*122*/ new IFB_LLCHAR  ( 13, "REMAINING OPEN-TO-USE"),
/*123*/ new IFB_LLCHAR  ( 29, "ADDRESS VERIFICATION DATA"),
/*124*/ new IFB_LLCHAR  (135, "FREE-FORM TEXT-JAPAN"),
/*125*/ new IFB_LLCHAR  ( 99, "SUPPORTING INFORMATION"),
/*126*/ new ISOMsgFieldPackager(
            new IFB_LLHBINARY (255, "Field 126"),
            new F126Packager()),
/*127*/ new ISOMsgFieldPackager(
            new IFB_LLHBINARY (255, "FILE RECORD(S) ACTION/DATA"),
            new F127Packager()),
/*128*/ new IFB_BINARY  (  8, "MAC 2")
    };

    protected class F126Packager extends Base1SubFieldPackager
    {
        protected ISOFieldPackager fld126[] =
        {
            new Base1_BITMAP126(16, "Bit Map"),
            new IF_ECHAR     (25, "Customer Name"),
            new IF_ECHAR     (57, "Customer Address"),
            new IF_ECHAR     (57, "Biller Address"),
            new IF_ECHAR     (18, "Biller Telephone Number"),
            new IF_ECHAR     (6,  "Process By Date"),
            new IFB_LLNUM    (17, "Cardholder Cert Serial Number", true),
            new IFB_LLNUM    (17, "Merchant Cert Serial Number", true),
            new IFB_NUMERIC  (40, "Transaction ID", true),
            new IFB_NUMERIC  (40, "TransStain", true),
            new IF_ECHAR     (6,  "CVV2 Request Data"),
        };

        protected F126Packager ()
        {
            super();
            setFieldPackager(fld126);
        }
    }

    protected class F127Packager extends ISOBasePackager 
    {
        protected ISOFieldPackager fld127[] = 
        {
            new IF_ECHAR    (1,   "FILE UPDATE COD"),
            new IFB_LLHNUM  (19,  "ACCOUNT NUMBER", true),
            new IFB_NUMERIC (4,   "PURGE DATE", true),
            new IF_ECHAR    (2,   "ACTION CODE"),
            new IF_ECHAR    (9,   "REGION CODING"),
            new IFB_NUMERIC (4,   "FILLER", true),
        };
        protected F127Packager () 
        {
            super();
            setFieldPackager(fld127);
        }
    }

    public Base1Packager() 
    {
        super();
        setFieldPackager(base1Fld);
    }
}


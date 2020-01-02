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
import org.jpos.util.Logger;

/*
 * $Log$
 * Revision 1.6  2005/12/19 22:47:50  apr
 * Applied changes suggested by Mladen in http://groups-beta.google.com/group/jpos-users/browse_thread/thread/e60807e917c8c170/e29a681592d63a2b#e29a681592d63a2b and confirmed by Murtuza and Jeff
 *
 * Revision 1.5  2003/10/13 10:34:16  apr
 * Tabs expanded to 8 spaces
 *
 * Revision 1.4  2003/05/16 04:15:14  alwyns
 * Import cleanups.
 *
 * Revision 1.3  2001/01/13 19:26:09  victor
 * changed bitmap size to 16
 *
 * Revision 1.2  2000/11/02 12:09:18  apr
 * Added license to every source file
 *
 * Revision 1.1  2000/04/16 22:12:33  apr
 * New packagers location org.jpos.iso.packager
 *
 * Revision 1.6  2000/03/01 14:44:45  apr
 * Changed package name to org.jpos
 *
 * Revision 1.5  2000/02/28 11:11:37  apr
 * Added inner ISOMsg logging
 *
 * Revision 1.4  2000/02/27 14:28:05  apr
 * PostPackager compatible with new jPOS ISOMsgFieldPackager
 * Support for new Postillion version [Victor]
 *
 * Revision 1.3  1999/09/20 12:33:03  apr
 * Removed external PostPrivatePackager - now Victor uses ISOMsgFieldPackager
 * with inner PostPrivatePackager
 *
 */

/**
 * ISO 8583 v1987 Packager for Postilion
 *
 * @author Victor A. Salaman <salaman@teknos.com>
 * @version Id: PostPackager.java,v 1.9 1999/09/17 12:08:02 salaman Exp 
 * @see ISOPackager
 * @see ISOBasePackager
 * @see ISOComponent
 */

public class PostPackager extends ISOBasePackager {
    protected PostPrivatePackager p127 = new PostPrivatePackager();
    protected ISOFieldPackager fld[] = {
            new IFA_NUMERIC (  4, "MESSAGE TYPE INDICATOR"),
            new IFB_BITMAP  ( 16, "BIT MAP"),
            new IFA_LLNUM   ( 19, "PAN - PRIMARY ACCOUNT NUMBER"),
            new IFA_NUMERIC (  6, "PROCESSING CODE"),
            new IFA_NUMERIC ( 12, "AMOUNT, TRANSACTION"),
            new IFA_NUMERIC ( 12, "AMOUNT, SETTLEMENT"),
            new IFA_NUMERIC ( 12, "AMOUNT, CARDHOLDER BILLING"),
            new IFA_NUMERIC ( 10, "TRANSMISSION DATE AND TIME"),
            new IFA_NUMERIC (  8, "AMOUNT, CARDHOLDER BILLING FEE"),
            new IFA_NUMERIC (  8, "CONVERSION RATE, SETTLEMENT"),
            new IFA_NUMERIC (  8, "CONVERSION RATE, CARDHOLDER BILLING"),
            new IFA_NUMERIC (  6, "SYSTEM TRACE AUDIT NUMBER"),
            new IFA_NUMERIC (  6, "TIME, LOCAL TRANSACTION"),
            new IFA_NUMERIC (  4, "DATE, LOCAL TRANSACTION"),
            new IFA_NUMERIC (  4, "DATE, EXPIRATION"),
            new IFA_NUMERIC (  4, "DATE, SETTLEMENT"),
            new IFA_NUMERIC (  4, "DATE, CONVERSION"),
            new IFA_NUMERIC (  4, "DATE, CAPTURE"),
            new IFA_NUMERIC (  4, "MERCHANTS TYPE"),
            new IFA_NUMERIC (  3, "ACQUIRING INSTITUTION COUNTRY CODE"),
            new IFA_NUMERIC (  3, "PAN EXTENDED COUNTRY CODE"),
            new IFA_NUMERIC (  3, "FORWARDING INSTITUTION COUNTRY CODE"),
            new IFA_NUMERIC (  3, "POINT OF SERVICE ENTRY MODE"),
            new IFA_NUMERIC (  3, "CARD SEQUENCE NUMBER"),
            new IFA_NUMERIC (  3, "NETWORK INTERNATIONAL IDENTIFIEER"),
            new IFA_NUMERIC (  2, "POINT OF SERVICE CONDITION CODE"),
            new IFA_NUMERIC (  2, "POINT OF SERVICE PIN CAPTURE CODE"),
            new IFA_NUMERIC (  1, "AUTHORIZATION IDENTIFICATION RESP LEN"),
            new IFA_AMOUNT  (  9, "AMOUNT, TRANSACTION FEE"),
            new IFA_AMOUNT  (  9, "AMOUNT, SETTLEMENT FEE"),
            new IFA_AMOUNT  (  9, "AMOUNT, TRANSACTION PROCESSING FEE"),
            new IFA_AMOUNT  (  9, "AMOUNT, SETTLEMENT PROCESSING FEE"),
            new IFA_LLNUM   ( 11, "ACQUIRING INSTITUTION IDENT CODE"),
            new IFA_LLNUM   ( 11, "FORWARDING INSTITUTION IDENT CODE"),
            new IFA_LLCHAR  ( 28, "PAN EXTENDED"),
            new IFA_LLNUM   ( 37, "TRACK 2 DATA"),
            new IFA_LLLCHAR (104, "TRACK 3 DATA"),
            new IF_CHAR     ( 12, "RETRIEVAL REFERENCE NUMBER"),
            new IF_CHAR     (  6, "AUTHORIZATION IDENTIFICATION RESPONSE"),
            new IF_CHAR     (  2, "RESPONSE CODE"),
            new IF_CHAR     (  3, "SERVICE RESTRICTION CODE"),
            new IF_CHAR     (  8, "CARD ACCEPTOR TERMINAL IDENTIFICACION"),
            new IF_CHAR     ( 15, "CARD ACCEPTOR IDENTIFICATION CODE" ),
            new IF_CHAR     ( 40, "CARD ACCEPTOR NAME/LOCATION"),
            new IFA_LLCHAR  ( 25, "ADITIONAL RESPONSE DATA"),
            new IFA_LLCHAR  ( 76, "TRACK 1 DATA"),
            new IFA_LLLCHAR (999, "ADITIONAL DATA - ISO"),
            new IFA_LLLCHAR (999, "ADITIONAL DATA - NATIONAL"),
            new IFA_LLLCHAR (999, "ADITIONAL DATA - PRIVATE"),
            new IF_CHAR     (  3, "CURRENCY CODE, TRANSACTION"),
            new IF_CHAR     (  3, "CURRENCY CODE, SETTLEMENT"),
            new IF_CHAR     (  3, "CURRENCY CODE, CARDHOLDER BILLING"   ),
            new IFB_BINARY  (  8, "PIN DATA"   ),
            new IFB_BINARY  ( 48, "SECURITY RELATED CONTROL INFORMATION"),
            new IFA_LLLCHAR (120, "ADDITIONAL AMOUNTS"),
            new IFA_LLLCHAR (999, "RESERVED ISO"),
            new IFA_LLLCHAR (999, "RESERVED ISO"),
            new IFA_LLLCHAR (999, "RESERVED NATIONAL"),
            new IFA_LLLCHAR (999, "RESERVED NATIONAL"),
            new IFA_LLLCHAR (999, "RESERVED NATIONAL"),
            new IFA_LLLCHAR (999, "RESERVED PRIVATE"),
            new IFA_LLLCHAR (999, "RESERVED PRIVATE"),
            new IFA_LLLCHAR (999, "RESERVED PRIVATE"),
            new IFA_LLLCHAR (999, "RESERVED PRIVATE"),
            new IFA_BINARY  (  8, "MESSAGE AUTHENTICATION CODE FIELD"),
            new IFA_BINARY  (  8, "BITMAP, EXTENDED"),
            new IFA_NUMERIC (  1, "SETTLEMENT CODE"),
            new IFA_NUMERIC (  2, "EXTENDED PAYMENT CODE"),
            new IFA_NUMERIC (  3, "RECEIVING INSTITUTION COUNTRY CODE"),
            new IFA_NUMERIC (  3, "SETTLEMENT INSTITUTION COUNTRY CODE"),
            new IFA_NUMERIC (  3, "NETWORK MANAGEMENT INFORMATION CODE"),
            new IFA_NUMERIC (  4, "MESSAGE NUMBER"),
            new IFA_NUMERIC (  4, "MESSAGE NUMBER LAST"),
            new IFA_NUMERIC (  6, "DATE ACTION"),
            new IFA_NUMERIC ( 10, "CREDITS NUMBER"),
            new IFA_NUMERIC ( 10, "CREDITS REVERSAL NUMBER"),
            new IFA_NUMERIC ( 10, "DEBITS NUMBER"),
            new IFA_NUMERIC ( 10, "DEBITS REVERSAL NUMBER"),
            new IFA_NUMERIC ( 10, "TRANSFER NUMBER"),
            new IFA_NUMERIC ( 10, "TRANSFER REVERSAL NUMBER"),
            new IFA_NUMERIC ( 10, "INQUIRIES NUMBER"),
            new IFA_NUMERIC ( 10, "AUTHORIZATION NUMBER"),
            new IFA_NUMERIC ( 12, "CREDITS, PROCESSING FEE AMOUNT"),
            new IFA_NUMERIC ( 12, "CREDITS, TRANSACTION FEE AMOUNT"),
            new IFA_NUMERIC ( 12, "DEBITS, PROCESSING FEE AMOUNT"),
            new IFA_NUMERIC ( 12, "DEBITS, TRANSACTION FEE AMOUNT"),
            new IFA_NUMERIC ( 16, "CREDITS, AMOUNT"),
            new IFA_NUMERIC ( 16, "CREDITS, REVERSAL AMOUNT"),
            new IFA_NUMERIC ( 16, "DEBITS, AMOUNT"),
            new IFA_NUMERIC ( 16, "DEBITS, REVERSAL AMOUNT"),
            new IFA_NUMERIC ( 42, "ORIGINAL DATA ELEMENTS"),
            new IF_CHAR     (  1, "FILE UPDATE CODE"),
            new IF_CHAR     (  2, "FILE SECURITY CODE"),
            new IF_CHAR     (  5, "RESPONSE INDICATOR"),
            new IF_CHAR     (  7, "SERVICE INDICATOR"),
            new IF_CHAR     ( 42, "REPLACEMENT AMOUNTS"),
            new IFA_BINARY  ( 8, "MESSAGE SECURITY CODE"),
            new IFA_AMOUNT  ( 17, "AMOUNT, NET SETTLEMENT"),
            new IF_CHAR     ( 25, "PAYEE"),
            new IFA_LLNUM   ( 11, "SETTLEMENT INSTITUTION IDENT CODE"),
            new IFA_LLNUM   ( 11, "RECEIVING INSTITUTION IDENT CODE"),
            new IFA_LLCHAR  ( 17, "FILE NAME"),
            new IFA_LLCHAR  ( 28, "ACCOUNT IDENTIFICATION 1"),
            new IFA_LLCHAR  ( 28, "ACCOUNT IDENTIFICATION 2"),
            new IFA_LLLCHAR (100, "TRANSACTION DESCRIPTION"),
            new IFA_LLLCHAR (999, "RESERVED ISO USE"), 
            new IFA_LLLCHAR (999, "RESERVED ISO USE"), 
            new IFA_LLLCHAR (999, "RESERVED ISO USE"), 
            new IFA_LLLCHAR (999, "RESERVED ISO USE"), 
            new IFA_LLLCHAR (999, "RESERVED ISO USE"), 
            new IFA_LLLCHAR (999, "RESERVED ISO USE"), 
            new IFA_LLLCHAR (999, "RESERVED ISO USE"), 
            new IFA_LLLCHAR (999, "RESERVED NATIONAL USE"),
            new IFA_LLLCHAR (999, "RESERVED NATIONAL USE"),
            new IFA_LLLCHAR (999, "RESERVED NATIONAL USE"   ),
            new IFA_LLLCHAR (999, "RESERVED NATIONAL USE"),
            new IFA_LLLCHAR (999, "RESERVED NATIONAL USE"  ),
            new IFA_LLLCHAR (999, "RESERVED NATIONAL USE"),
            new IFA_LLLCHAR (999, "RESERVED NATIONAL USE"),
            new IFA_LLLCHAR (999, "RESERVED NATIONAL USE"),
            new IFA_LLLCHAR (999, "RESERVED PRIVATE USE"),
            new IFA_LLLCHAR (999, "RESERVED PRIVATE USE"),
            new IFA_LLLCHAR (999, "RESERVED PRIVATE USE"),
            new IFA_LLLCHAR (999, "RESERVED PRIVATE USE"),
            new IFA_LLLCHAR (999, "RESERVED PRIVATE USE"),
            new IFA_LLLCHAR (999, "RESERVED PRIVATE USE"),
            new IFA_LLLCHAR (999, "RESERVED PRIVATE USE"),
            new ISOMsgFieldPackager(
                new IFA_LLLLLLBINARY (99999, "RESERVED PRIVATE USE"),
                p127
            ),
            new IFA_LLLCHAR (999, "MAC 2")
        };
        protected static class PostPrivatePackager extends ISOBasePackager
        { 
                protected ISOFieldPackager fld127[] = 
                {
                        new IF_CHAR             (0,   "PLACEHOLDER"),
                        new IFB_BITMAP  (8,       "BITMAP"),
                        new IFA_LLCHAR  (32,  "SWITCH KEY"),
                    new IF_CHAR     (48,  "ROUTING INFORMATION"),
                    new IF_CHAR     (22,  "POS DATA"),
                    new IF_CHAR     (73,  "SERVICE STATION DATA"),
                    new IFA_NUMERIC (2,   "AUTHORIZATION PROFILE"),
                    new IFA_LLCHAR  (50,  "CHECK DATA"),
                    new IFA_LLLCHAR (128, "RETENTION DATA"),
                    new IFA_LLLCHAR (255, "ADDITIONAL NODE DATA"),
                    new IFA_NUMERIC (3,   "CVV2"),
                    new IFA_LLCHAR  (32,  "ORIGINAL KEY"),
                    new IFA_LLCHAR  (25,  "TERMINAL OWNDER"),
                    new IF_CHAR     (17,  "POS GEOGRAPHIC DATA"),
                    new IF_CHAR     (8,   "SPONSOR BANK"),
                    new IFA_LLCHAR  (29,  "AVS REQUEST"),
                    new IF_CHAR     (1,   "AVS RESPONSE"),
                    new IFA_LLCHAR  (50,  "CARDHOLDER INFORMATION"),
                    new IFA_LLCHAR  (50,  "VALIDATION DATA"),
                    new IF_CHAR     (45,  "BANK DETAILS"), 
                    new IFA_NUMERIC (8,   "AUTHORIZER DATE SETTLEMENT"), 
                    new IFA_LLCHAR  (12,  "RECORD IDENTIFICATION"), 
                    new IFA_LLLLLCHAR  (99999,  "STRUCTURED DATA"), 
                    new IF_CHAR     (253,  "PAYEE NAME AND ADDRESS"), 
                    new IFA_LLCHAR  (28,  "PAYER ACCOUNT INFORMATION"), 
                    new IFA_LLLLCHAR(8000,  "ICC DATA")
                };  

        protected PostPrivatePackager()
        {   super();
            setFieldPackager(fld127);
        }
    }
    public PostPackager() {
        super();
        setFieldPackager(fld);
    }
    public void setLogger (Logger logger, String realm) {
        super.setLogger (logger, realm);
        p127.setLogger (logger, realm + ".PostPrivatePackager");
    }
}

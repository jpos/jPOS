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

import java.util.*;
import org.jpos.iso.*;

/**
 * ANSI X9.2 Packager
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOPackager
 * @see ISOBasePackager
 * @see ISOComponent
 */
public class X92Packager extends ISOBasePackager {
    protected ISOFieldPackager bitMapPackager = 
        new X92_BITMAP (16, "X9.2 BIT MAP");

    protected ISOFieldPackager fld[] = {
            new IFA_NUMERIC(     4, "MESSAGE TYPE"                          ),
            new IFA_FLLNUM (    19, "PAN - PRIMARY ACCOUNT NUMBER"          ),
            new IFA_NUMERIC(     6, "PROCESSING CODE"                       ),
            new IFA_NUMERIC(    12, "AMOUNT, TRANSACTION"                   ),
            new IFA_NUMERIC(    12, "AMOUNT, SETTLEMENT"                    ),
            new IFA_NUMERIC(    12, "AMOUNT, CARDHOLDER BILLING"            ),
            new IFA_NUMERIC(     6, "ACQUIRING INSTITUTION TRACE NUMBER"    ),
            new IFA_NUMERIC(     6, "TRANSACTION DATE"                      ),
            new IFA_NUMERIC(     6, "ACQUIRING INSTITUTION POST DATE"       ),
            new IFA_NUMERIC(     6, "RESP ACQUIRING INSTITUTION POST DATE"  ),
            new IFA_NUMERIC(     6, "TRANSACTION TIME"                      ),
            new IFA_NUMERIC(    29, "ADDRESS VERIFICATION"                  ),
            new IFA_NUMERIC(     1, "ADDRESS VERIFICATION STATE"            ),
            new IFA_NUMERIC(    10, "ACQUIRING INSTITUTION IDENT CODE"      ),
            new IFA_NUMERIC(    10, "CARD ACCEPTOR IDENTIFICATION NUMBER"   ),
            new IFA_NUMERIC(     3, "TERMINAL COUNTRY CODE"                 ),
            new IFA_BINARY(      0, "UNUSED"                                ),
            new IFA_BINARY(      0, "UNUSED"                                ),
            new IFA_BINARY(      0, "UNUSED"                                ),
            new IFA_NUMERIC(    10, "ISSUER INSTITUTION IDENT NUMBER"       ),
            new IFA_NUMERIC(     2, "REVERSAL CODE"                         ),
            new IFA_BINARY(      0, "UNUSED"                                ),
            new IFA_BINARY(      0, "UNUSED"                                ),
            new IFA_NUMERIC(    14, "GROSS DEBITS, AMOUNT"                  ),
            new IFA_NUMERIC(    10, "GROSS DEBITS, COUNT"                   ),
            new IFA_NUMERIC(    14, "GROSS CREDITS, AMOUNT"                 ),
            new IFA_NUMERIC(    10, "GROSS CREDITS, COUNT"                  ),
            new IFA_NUMERIC(    14, "REVERSAL GROSS DEBITS, AMOUNT"         ),
            new IFA_NUMERIC(    10, "REVERSAL GROSS DEBITS, COUNT"          ),
            new IFA_NUMERIC(    14, "REVERSAL GROSS CREDITS, AMOUNT"        ),
            new IFA_NUMERIC(    10, "REVERSAL GROSS CREDITS, COUNT"         ),
            new IFA_NUMERIC(     2, "TRANSACTION SPECIFICATION"             ),
            new IFA_NUMERIC(     3, "NETWORK MANAGEMENT INFORMATION CODE"   ),
            new IFA_NUMERIC(    12, "RESPONSE CODE"                         ),
            new IF_CHAR(        25, "TERMINAL OWNER"                        ),
            new IF_CHAR(        15, "TERMINAL CITY/STATE"                   ),
            new IFA_NUMERIC(     6, "ORIGINAL DATA (ACQ POST DATE)"         ),
            new IFA_NUMERIC(     6, "ISSUER TRACE NUMBER"                   ),
            new IFA_BINARY(      0, "UNUSED"                                ),
            new IFA_BINARY(      8, "PIN"                                   ),
            new IF_CHAR(         8, "PASSWORD"                              ),
            new IFA_BINARY(      0, "UNUSED"                                ),
            new IFA_BINARY(      0, "UNUSED"                                ),
            new IFA_BINARY(      0, "UNUSED"                                ),
            new IFA_BINARY(      0, "UNUSED"                                ),
            new IFA_BINARY(      0, "UNUSED"                                ),
            new IFA_BINARY(      0, "UNUSED"                                ),
            new IFA_BINARY(      0, "UNUSED"                                ),
            new IFA_BINARY(      0, "UNUSED"                                ),
            new IFA_BINARY(      0, "UNUSED"                                ),
            new IFA_NUMERIC(     8, "APROVAL CODE"                          ),
            new IFA_NUMERIC(    25, "POST INFO"                             ),
            new IFA_NUMERIC(    30, "ISO ORIGINAL DATA"                     ),
            new IFA_NUMERIC(    19, "SHARING"                               ),
            new IFA_NUMERIC(    51, "TERMINAL INFORMATION"                  ),
            new IFA_NUMERIC(     2, "PIN SIZE"                              ),
            new IFA_NUMERIC(    16, "OPTIONS"                               ),
            new IFA_NUMERIC(     6, "ISSUER POST DATE"                      ),
            new IFA_NUMERIC(    38, "SOURCE/TARGET ACCOUNT NUMBERS"         ),
            new IFA_NUMERIC(    15, "PREAUTHORIZATION"                      ),
            new IFA_LLLCHAR(   382, "ADDITIONAL DATA"                       ),
            new IFA_BINARY(      0, "UNUSED"                                ),
            new IFA_FLLCHAR(    39, "TRACK II DATA"                         ),
            new IFA_BINARY(      0, "UNUSED"                                ),
            new IFA_BINARY(      0, "UNUSED"                                )
        };
            
    public X92Packager() {
        super();
        setFieldPackager(fld);
    }
    /**
     * @return suitable ISOFieldPackager for Bitmap
     */
    protected ISOFieldPackager getBitMapfieldPackager() {
        return bitMapPackager;
    }
    /**
     * Although field 1 is not a Bitmap ANSI X9.2 do have
     * a Bitmap field that have to be packed/unpacked
     * @return true
     */
    protected boolean emitBitMap () {
	return true;
    }
}

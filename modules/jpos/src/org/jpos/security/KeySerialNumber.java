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

package  org.jpos.security;

import java.io.PrintStream;
import java.io.Serializable;

import org.jpos.util.Loggeable;


/**
 * Key Serial Number (also called Key Name in the ANSI X9.24).
 * Needed for deriving the Transaction Key when  DUKPT (Derived Unique Key Per
 * Transaction) method is used.<br>
 * Refer to ANSI X9.24 for more information about DUKPT
 * @author Hani S. Kirollos
 * @version $Revision$ $Date$
 * @see EncryptedPIN
 */
public class KeySerialNumber
        implements Serializable, Loggeable {
    /**
     * baseKeyID a HexString representing the BaseKeyID (also called KeySet ID)
     */
    String baseKeyID;
    /**
     * deviceID a HexString representing the Device ID (also called TRSM ID)
     */
    String deviceID;
    /**
     * transactionCounter a HexString representing the transaction counter
     */
    String transactionCounter;

    /**
     * Constructs a key serial number object
     */
    public KeySerialNumber () {
    }

    /**
     * Constructs a key serial number object
     * @param baseKeyID a HexString representing the BaseKeyID (also called KeySet ID)
     * @param deviceID a HexString representing the Device ID (also called TRSM ID)
     * @param transactionCounter a HexString representing the transaction counter
     */
    public KeySerialNumber (String baseKeyID, String deviceID, String transactionCounter) {
        setBaseKeyID(baseKeyID);
        setDeviceID(deviceID);
        setTransactionCounter(transactionCounter);
    }

    /**
     *
     * @param baseKeyID a HexString representing the BaseKeyID (also called KeySet ID)
     */
    public void setBaseKeyID (String baseKeyID) {
        this.baseKeyID = baseKeyID;
    }

    /**
     *
     * @return baseKeyID a HexString representing the BaseKeyID (also called KeySet ID)
     */
    public String getBaseKeyID () {
        return  baseKeyID;
    }

    /**
     *
     * @param deviceID a HexString representing the Device ID (also called TRSM ID)
     */
    public void setDeviceID (String deviceID) {
        this.deviceID = deviceID;
    }

    /**
     *
     * @return deviceID a HexString representing the Device ID (also called TRSM ID)
     */
    public String getDeviceID () {
        return  deviceID;
    }

    /**
     *
     * @param transactionCounter a HexString representing the transaction counter
     */
    public void setTransactionCounter (String transactionCounter) {
        this.transactionCounter = transactionCounter;
    }

    /**
     *
     * @return transactionCounter a HexString representing the transaction counter
     */
    public String getTransactionCounter () {
        return  transactionCounter;
    }

    /**
     * dumps Key Serial Number
     * @param p a PrintStream usually supplied by Logger
     * @param indent indention string, usually suppiled by Logger
     * @see org.jpos.util.Loggeable
     */
    public void dump (PrintStream p, String indent) {
        String inner = indent + "  ";
        p.println(indent + "<key-serial-number>");
        p.println(inner + "<base-key-id>" + getBaseKeyID() + "</base-key-id>");
        p.println(inner + "<device-id>" + getDeviceID() + "</device-id>");
        p.println(inner + "<transaction-counter>" + getTransactionCounter() + "</transaction-counter");
        p.println(indent + "</key-serial-number>");
    }
}




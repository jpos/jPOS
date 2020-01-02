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

package  org.jpos.security;

import org.jpos.util.Loggeable;

import java.io.PrintStream;
import java.io.Serializable;


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

    private static final long serialVersionUID = -8388775376202253082L;
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




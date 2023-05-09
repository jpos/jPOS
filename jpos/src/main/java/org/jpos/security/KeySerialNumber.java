/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2022 jPOS Software SRL
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

import org.jpos.iso.ISOUtil;
import org.jpos.util.Loggeable;

import java.io.PrintStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Objects;


/**
 * Key Serial Number (also called Key Name in the ANSI X9.24).
 * Needed for deriving the Transaction Key when  DUKPT (Derived Unique Key Per
 * Transaction) method is used.<br>
 * Refer to ANSI X9.24 for more information about DUKPT
 * @author Hani S. Kirollos
 * @see EncryptedPIN
 */
public class KeySerialNumber implements Serializable, Loggeable {
    private static final long serialVersionUID = 5588769944206835776L;
    private long baseId;
    private long deviceId;
    private int transactionCounter;

    /**
     * Constructs a key serial number object
     * @param baseKeyID a HexString representing the BaseKeyID (also called KeySet ID)
     * @param deviceID a HexString representing the Device ID (also called TRSM ID)
     * @param transactionCounter a HexString representing the transaction counter
     */
    public KeySerialNumber (String baseKeyID, String deviceID, String transactionCounter) {
        try {
            baseKeyID = ISOUtil.padleft(baseKeyID, 10, 'F');
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid baseKeyID.");
        }
        baseId = Long.parseLong(baseKeyID, 16);
        deviceId = Long.parseLong (deviceID, 16);
        this.transactionCounter = Integer.parseInt (transactionCounter, 16);
    }
    
    /**
     * Constructs a key serial number object from its binary representation.
     * @param ksn binary representation of the KSN.
     */
    public KeySerialNumber(byte[] ksn) {
        Objects.requireNonNull (ksn, "KSN cannot be null");
        if (ksn.length < 8 || ksn.length > 10) {
            throw new IllegalArgumentException("KSN must be 8 to 10 bytes long.");
        }
        parseKsn (ksn);
    }
    /**
     * Returns the base key ID as a hexadecimal string padded with leading zeros to a length of 10 characters.
     * 
     * @return a String representing the base key ID.
     */
    public String getBaseKeyID () {
        return  String.format ("%010X", baseId);
    }

    /**
     * Returns the base key ID as an array of bytes.
     * @return a 5 bytes array representing the base key ID.
     */
    public byte[] getBaseKeyIDBytes () {
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.putLong(baseId);
        buf.position(3);
        byte[] lastFive = new byte[5];
        buf.get(lastFive);
        return lastFive;
    }

    /**
     * Returns the device ID as a hexadecimal string padded with leading zeros to a length of 6 characters.
     * @return a String representing the device ID.
     */
    public String getDeviceID () {
        return  String.format ("%06X", deviceId);
    }

    /**
     * Returns the deviceID as an array of bytes.
     *
     * @ return a 3 bytes array representing the deviceID
     */
    public byte[] getDeviceIDBytes () {
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.putLong(deviceId);
        buf.position(5);
        byte[] lastThree = new byte[3];
        buf.get (lastThree);
        return lastThree;
    }

    /**
     * Returns the transaction counter as a hexadecimal string padded with leading zeros to a length of 6 characters.
     *
     * @return a String representing the transaction counter.
     */
    public String getTransactionCounter () {
        return  String.format ("%06X", transactionCounter);
    }

    /**
     * Returns the transaction counter as an array of bytes.
     *
     * @ return a 3 byte array representing the transaction counter.
     */
    public byte[] getTransactionCounterBytes () {
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(transactionCounter);
        buf.position(1);
        byte[] lastThree = new byte[3];
        buf.get (lastThree);
        return lastThree;
    }

    /**
     * Constructs a 10-byte Key Serial Number (KSN) array using the base key ID, device ID, and transaction counter.
     * The method first extracts the last 5 bytes from the base key ID and device ID (shifted and combined with the
     * transaction counter), and then combines them into a single ByteBuffer of size 10.
     *
     * @return A byte array containing the 10-byte Key Serial Number.
     */
    public byte[] getBytes() {
        ByteBuffer buf = ByteBuffer.allocate(10);
        buf.put (last5(baseId));
        buf.put (last5(deviceId >> 1 << 21 | transactionCounter));
        return buf.array();
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
        p.printf ("%s<image>%s</image>%n", inner, ISOUtil.hexString(getBytes()));
        p.println(inner + "<base-key-id>" + getBaseKeyID() + "</base-key-id>");
        p.println(inner + "<device-id>" + getDeviceID() + "</device-id>");
        p.println(inner + "<transaction-counter>" + getTransactionCounter() + "</transaction-counter");
        p.println(indent + "</key-serial-number>");
    }
    
    @Override
    public String toString() {
        return String.format(
          "KeySerialNumber{base=%X, device=%X, counter=%X}", baseId, deviceId, transactionCounter
        );
    }

    /**
     * Parses a Key Serial Number (KSN) into its base key ID, device ID, and transaction counter components.
     * The KSN is first padded to a length of 10 bytes, and then the base key ID, device ID, and transaction counter
     * are extracted.
     * The base key id has a fixed length of 5 bytes.
     * The sequence number has a fixed length of 19 bits.
     * The transaction counter has a fixed length of 21 bits per ANS X9.24 spec.
     *
     * It is important to mention that the device ID is a 19-bit value, which has been shifted one bit to the right
     * from its original hexadecimal representation. To facilitate readability and manipulation when reconstructing
     * the KSN byte image, the device ID is maintained in a left-shifted position by one bit.
     *
     * @param ksn        The input KSN byte array to be parsed.
     * @throws IllegalArgumentException If the base key ID length is smaller than 0 or greater than 8.
     */
    private void parseKsn(byte[] ksn) {
        ByteBuffer buf = padleft (ksn, 10, (byte) 0xFF);

        byte[] baseKeyIdBytes = new byte[5];
        buf.get(baseKeyIdBytes);
        baseId = padleft (baseKeyIdBytes, 8, (byte) 0x00).getLong();

        ByteBuffer sliceCopy = buf.slice().duplicate();
        ByteBuffer remaining = ByteBuffer.allocate(8);
        remaining.position(8 - sliceCopy.remaining());
        remaining.put(sliceCopy);
        remaining.flip();

        long l = remaining.getLong();

        int mask = (1 << 21) - 1;
        transactionCounter = (int) l & mask;
        deviceId = l >>> 21 << 1;
    }

    /**
     * Pads the input byte array with a specified padding byte on the left side to achieve a desired length.
     *
     * @param b       The input byte array to be padded.
     * @param len     The desired length of the resulting padded byte array.
     * @param padbyte The byte value used for padding the input byte array.
     * @return A ByteBuffer containing the padded byte array with the specified length.
     * @throws IllegalArgumentException If the desired length is smaller than the length of the input byte array.
     */
    private ByteBuffer padleft (byte[] b, int len, byte padbyte) {
        if (len < b.length) {
            throw new IllegalArgumentException("Desired length must be greater than or equal to the length of the input byte array.");
        }
        ByteBuffer buf = ByteBuffer.allocate(len);
        for (int i=0; i<len-b.length; i++)
            buf.put (padbyte);
        buf.put (b);
        buf.flip();
        return buf;
    }

    /**
     * Extracts the last 5 bytes from the 8-byte representation of the given long value.
     * The method first writes the long value into a ByteBuffer of size 8, and then
     * creates a new ByteBuffer containing the last 5 bytes of the original buffer.
     *
     * @param l The input long value to be converted and sliced.
     * @return A ByteBuffer containing the last 5 bytes of the 8-byte representation of the input long value.
     */
    private ByteBuffer last5 (long l) {
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.putLong(l);
        buf.position(3);
        return buf.slice();
    }
}

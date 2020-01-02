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

import org.jpos.iso.ISOUtil;
import org.jpos.util.Loggeable;
import org.jpos.iso.ISOException;

import java.io.PrintStream;
import java.io.Serializable;
import java.security.InvalidParameterException;


/**
 * <p>
 * The PIN (Personal Identification Number), is used to authenticate card
 * holders. A user enters his/her PIN on the pin-pad (also called pin entry device)
 * of a terminal (whether ATM or POS). The terminal forms the PIN Block, which
 * is a mix of the PIN and the account number.<br>
 * In a typical environment, the PIN Block (not the PIN) gets encrypted and sent
 * to the acquirer. This Encrypted PIN Block is the typical content of the
 * PIN Data ISO Field (Field 52).
 * This class represents an encrypted PIN, no matter by whom it is encrypted.
 * Typically a PIN is encrypted under one of these three:<br>
 * 1- Under a terminal PIN key (like TPK or DUKPT)<br>
 * 2- Under an Interchange PIN key (like ZPK)<br>
 * 3- Under the the security module itself (i.e. under LMK)<br>
 * This class knows nothing about, who encrypted it.
 * </p>
 * <p>
 * This class represents an encrypted PIN using:<br>
 * 1- The PIN Block (encrypted)<br>
 * 2- The account number (the 12 right-most digits of the account number excluding the check digit)<br>
 * 3- The PIN Block Format<br>
 * </p>
 * <p>
 * The PIN Block Format specifies how the clear pin (as entered by the card holder)
 * and the account number get mixed to form the PIN Block.<br>
 * </p>
 * @author Hani Samuel Kirollos
 * @version $Revision$ $Date$
 * @see SMAdapter
 */
public class EncryptedPIN
        implements Serializable, Loggeable {

    private static final long serialVersionUID = -9117335317030664867L;
    /**
     * Account Number (the 12 right-most digits of the account number excluding the check digit)
     */
    String accountNumber;
    /**
     * This is the ByteArray holding the PIN Block
     * The PIN Block can be either clear or encrypted
     * It is typically DES or 3DES encrypted, with length 8 bytes.
     */
    byte[] pinBlock;
    /**
     * The PIN Block Format
     * value -1 means no block format defined
     */
    byte pinBlockFormat;

    public EncryptedPIN () {
        super();
    }

    /**
     *
     * @param pinBlock
     * @param pinBlockFormat
     * @param accountNumber account number, including BIN and the check digit
     */
    public EncryptedPIN (byte[] pinBlock, byte pinBlockFormat, String accountNumber) {
        setPINBlock(pinBlock);
        setPINBlockFormat(pinBlockFormat);
        setAccountNumber(extractAccountNumberPart(accountNumber));
    }
    /**
     * @param pinBlock
     * @param pinBlockFormat
     * @param accountNumber if <code>extract</code> is false then account number, including BIN and the check digit
     *        or if parameter <code>extract</code> is true then 12 right-most digits of the account number, excluding the check digit
     * @param extract true to extract 12 right-most digits off the account number
     */
    public EncryptedPIN (byte[] pinBlock, byte pinBlockFormat, String accountNumber, boolean extract) {
        setPINBlock(pinBlock);
        setPINBlockFormat(pinBlockFormat);
        setAccountNumber(extract ? extractAccountNumberPart(accountNumber) : accountNumber);
    }


    /**
     * @param pinBlockHexString the PIN Block represented as a HexString instead of a byte[]
     * @param pinBlockFormat
     * @param accountNumber account number, including BIN and the check digit
     */
    public EncryptedPIN (String pinBlockHexString, byte pinBlockFormat, String accountNumber) {
        this(ISOUtil.hex2byte(pinBlockHexString), pinBlockFormat, accountNumber);
    }
    /**
     * @param pinBlockHexString the PIN Block represented as a HexString instead of a byte[]
     * @param pinBlockFormat
     * @param accountNumber if <code>extract</code> is false then account number, including BIN and the check digit
     *        or if parameter <code>extract</code> is true then 12 right-most digits of the account number, excluding the check digit
     * @param extract true to extract 12 right-most digits off the account number
     */
    public EncryptedPIN (String pinBlockHexString, byte pinBlockFormat, String accountNumber, boolean extract) {
        this(ISOUtil.hex2byte(pinBlockHexString), pinBlockFormat, accountNumber, extract);
    }

    /**
     * dumps PIN basic information
     * @param p a PrintStream usually supplied by Logger
     * @param indent indention string, usually suppiled by Logger
     * @see org.jpos.util.Loggeable
     */
    public void dump (PrintStream p, String indent) {
        String inner = indent + "  ";
        p.print(indent + "<encrypted-pin");
        p.print(" format=\"0" + getPINBlockFormat() + "\"");
        p.println(">");
        p.println(inner + "<pin-block>" + ISOUtil.hexString(getPINBlock()) + "</pin-block>");
        p.println(inner + "<account-number>" + getAccountNumber() + "</account-number>");
        p.println(indent + "</encrypted-pin>");
    }

    /**
     *
     * @param pinBlock
     */
    public void setPINBlock (byte[] pinBlock) {
        this.pinBlock = pinBlock;
    }

    /**
     *
     * @return pinBlock
     */
    public byte[] getPINBlock () {
        return  pinBlock;
    }

    /**
     *
     * @param pinBlockFormat
     */
    public void setPINBlockFormat (byte pinBlockFormat) {
        this.pinBlockFormat = pinBlockFormat;
    }

    /**
     *
     * @return PIN Block Format
     */
    public byte getPINBlockFormat () {
        return  this.pinBlockFormat;
    }

    /**
     * Sets the 12 right-most digits of the account number excluding the check digit
     * @param extractedAccountNumber  12 right-most digits of the account number, excluding the check digit.
     */
    public void setAccountNumber (String extractedAccountNumber) {
        if(extractedAccountNumber.length() != 12)
           throw new InvalidParameterException(
               "Extracted account number length should be 12, got '"
              +ISOUtil.protect(extractedAccountNumber) + "'"
           );
        this.accountNumber = extractedAccountNumber;
    }

    /**
     * @return accountNumber (the 12 right-most digits of the account number excluding the check digit)
     */
    public String getAccountNumber () {
        return  accountNumber;
    }

    /**
     * This method extracts the 12 right-most digits of the account number,
     * execluding the check digit.
     * @param accountNumber (PAN) consists of the BIN (Bank Identification Number), accountNumber
     * and a check digit.
     * @return the 12 right-most digits of the account number, execluding the check digit.
     *         In case if account number length is lower that 12 proper count of 0 digts is added
     *         on the left side for align to 12
     */
    public static String extractAccountNumberPart (String accountNumber) {
        String accountNumberPart = null;
        try {
            accountNumberPart = ISOUtil.takeLastN(accountNumber, 13);
            accountNumberPart = ISOUtil.takeFirstN(accountNumberPart, 12);
        } catch(ISOException ignored) {
            // NOPMD return original accountNumber
        }
        return  accountNumberPart;
    }


}




/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

package org.jpos.iso;

import org.jpos.iso.packager.XMLPackager;

import java.io.*;
import java.math.BigDecimal;

public class ISOAmount 
    extends ISOComponent 
    implements Cloneable, Externalizable
{
    static final long serialVersionUID = -6130248734056876225L;
    private int fieldNumber;
    private int currencyCode;
    private String value;
    private BigDecimal amount;

    public ISOAmount () {
        super();
        setFieldNumber (-1);
    }
    public ISOAmount (int fieldNumber) {
        super ();
        setFieldNumber (fieldNumber);
    }
    public ISOAmount (int fieldNumber, int currencyCode, BigDecimal amount) {
        super ();
        setFieldNumber (fieldNumber);
        this.amount = amount;
        this.currencyCode = currencyCode;
    }
    public Object getKey() {
        return fieldNumber;
    }
    public Object getValue() throws ISOException {
        if (value == null) {
            StringBuilder sb = new StringBuilder();
            sb.append (ISOUtil.zeropad (Integer.toString(currencyCode), 3));
            sb.append (Integer.toString (amount.scale()));
            sb.append (
                ISOUtil.zeropad (
                    amount.movePointRight(amount.scale()).toString(),12
                )
            );
            value = sb.toString();
        }
        return value;

    }
    public void setValue (Object obj) throws ISOException {
        if (obj instanceof String) {
            String s = (String) obj;
            if (s.length() < 12) {
                throw new ISOException (
                    "ISOAmount invalid length " + s.length()
                );
            }
            try {
                currencyCode = Integer.parseInt (s.substring(0,3));
                int dec = Integer.parseInt (s.substring(3,4));
                amount = new BigDecimal (s.substring(4)).movePointLeft (dec);
                value  = s;
            } catch (NumberFormatException e) {
                throw new ISOException (e.getMessage());
            }
        }
    }
    public void setFieldNumber (int fieldNumber) {
        this.fieldNumber = fieldNumber;
    }
    public BigDecimal getAmount () {
        return amount;
    }
    public int getScale() {
        return amount.scale() % 10;
    }
    public String getScaleAsString() {
        return Integer.toString(getScale());
    }
    public int getCurrencyCode() {
        return currencyCode;
    }
    public String getCurrencyCodeAsString() throws ISOException {
        return ISOUtil.zeropad(Integer.toString(currencyCode),3);
    }
    public String getAmountAsLegacyString() throws ISOException {
        return ISOUtil.zeropad (amount.unscaledValue().toString(), 12);
    }
    public String getAmountAsString() throws ISOException {
        StringBuffer sb = new StringBuffer(16);
        sb.append (ISOUtil.zeropad (Integer.toString (currencyCode),3));
        sb.append (Integer.toString(amount.scale() % 10));
        sb.append (ISOUtil.zeropad (amount.unscaledValue().toString(), 12));
        return sb.toString();
    }
    public byte[] pack() throws ISOException {
        throw new ISOException ("Not available");
    }
    public int unpack(byte[] b) throws ISOException {
        throw new ISOException ("Not available");
    }
    public void unpack(InputStream in) throws ISOException {
        throw new ISOException ("Not available");
    }
    public void dump (PrintStream p, String indent) {
        p.println (indent +"<"+XMLPackager.ISOFIELD_TAG + " " 
          +XMLPackager.ID_ATTR +"=\"" +fieldNumber +"\" "
          +"currency=\"" +ISOUtil.zeropad (currencyCode, 3)+"\" "
          +XMLPackager.TYPE_ATTR +"=\"amount\" "
          +XMLPackager.VALUE_ATTR+"=\"" + amount.toString() +"\"/>"
        );
    }
    public void writeExternal (ObjectOutput out) throws IOException {
        out.writeShort (fieldNumber);
        try {
            out.writeUTF ((String) getValue());
        } catch (ISOException e) {
            throw new IOException (e);
        }
    }
    public void readExternal  (ObjectInput in) 
        throws IOException, ClassNotFoundException
    {
        fieldNumber = in.readShort ();
        try {
            setValue(in.readUTF());
        } catch (ISOException e) {
            throw new IOException (e.getMessage());
        }
    }       
}


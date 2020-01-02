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

package org.jpos.iso;

import org.jpos.iso.packager.XMLPackager;

import java.io.*;

/**
 * implements <b>Leaf</b> for binary fields
 *
 * See the
 * <a href="API_users_guide.html">API User's Guide</a>
 * for details.
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */
public class ISOBinaryField 
    extends ISOComponent 
    implements Cloneable, Externalizable
{

    private static final long serialVersionUID = 7702505828876640372L;
    protected int fieldNumber;
    protected byte[] value;

    /**
     * No args constructor 
     * <font size="-1">(required by Externalizable support on ISOMsg)</font>
     */
    public ISOBinaryField () {
        fieldNumber = -1;
    }
    /**
     * @param n - the FieldNumber
     */
    public ISOBinaryField(int n) {
        fieldNumber = n;
    }
    /**
     * @param n - fieldNumber
     * @param v - fieldValue
     */
    public ISOBinaryField(int n, byte[] v) {
        fieldNumber = n;
        value = v;
    }
    /**
     * @param n - fieldNumber
     * @param v - fieldValue
     * @param offset - starting offset
     * @param len    - field length
     */
    public ISOBinaryField(int n, byte[] v, int offset, int len) {
        byte[] b = new byte[len];
        System.arraycopy (v, offset, b, 0, len);
        fieldNumber = n;
        value = b;
    }
    /**
     * changes this Component field number<br>
     * Use with care, this method does not change
     * any reference held by a Composite.
     * @param fieldNumber new field number
     */
    public void setFieldNumber (int fieldNumber) {
        this.fieldNumber = fieldNumber;
    }

    @Override
    public int getFieldNumber () {
        return fieldNumber;
    }

    /**
     * not available on Leaf - always throw ISOException
     * @exception ISOException
     */
    public byte[] pack() throws ISOException {
        throw new ISOException ("Not available on Leaf");
    }
    /**
     * not available on Leaf - always throw ISOException
     * @exception ISOException
     */
    public int unpack(byte[] b) throws ISOException {
        throw new ISOException ("Not available on Leaf");
    }
    /**
     * not available on Leaf - always throw ISOException
     * @exception ISOException
     */
    public void unpack(InputStream in) throws ISOException {
        throw new ISOException ("Not available on Leaf");
    }
    /**
     * @return Object representing this field number
     */
    public Object getKey() {
        return fieldNumber;
    }
    /**
     * @return Object representing this field value
     */
    public Object getValue() {
        return value;
    }
    /**
     * @param obj - Object representing this field value
     * @exception ISOException
     */
    public void setValue (Object obj) throws ISOException {
        if (obj instanceof String)
            value = ((String) obj).getBytes(ISOUtil.CHARSET);
        else
            value = (byte[]) obj;
    }
    /**
     * @return byte[] representing this field
     */
    public byte[] getBytes() {
        return value;
    }
    /**
     * dump this field to PrintStream. The output is sorta
     * XML, intended to be easily parsed.
     * @param p - print stream
     * @param indent - optional indent string
     */
    public void dump (PrintStream p, String indent) {
        p.println (indent +"<"+XMLPackager.ISOFIELD_TAG + " " +
            XMLPackager.ID_ATTR +"=\"" +fieldNumber +"\" "+
            XMLPackager.VALUE_ATTR +"=\"" +this.toString() + "\" " +
            XMLPackager.TYPE_ATTR +"=\"" + XMLPackager.TYPE_BINARY + "\"/>"
        );
    }
    public String toString() {
        return ISOUtil.hexString(value);
    }
    public void writeExternal (ObjectOutput out) throws IOException {
        out.writeShort (fieldNumber);
        out.writeShort (value.length);
        out.write (value);
    }
    public void readExternal  (ObjectInput in) 
        throws IOException, ClassNotFoundException
    {
        fieldNumber = in.readShort ();
        value = new byte[in.readShort()];
        in.readFully (value);
    }
}

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
 * implements <b>Leaf</b> for standard fields
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */
public class ISOField 
    extends ISOComponent 
    implements Cloneable, Externalizable
{

    private static final long serialVersionUID = -4053616930139887829L;
    protected int fieldNumber;
    protected String value;

    /**
     * No args constructor 
     * <font size="-1">(required by Externalizable support on ISOMsg)</font>
     */
    public ISOField () {
        fieldNumber = -1;
    }

    /**
     * @param n - the FieldNumber
     */
    public ISOField (int n) {
        fieldNumber = n;
    }
    /**
     * @param n - fieldNumber
     * @param v - fieldValue
     */
    public ISOField (int n, String v) {
        fieldNumber = n;
        value = v;
    }
    /**
     * not available on Leaf - always throw ISOException
     * @return never returns
     * @exception ISOException
     */
    @Override
    public byte[] pack() throws ISOException {
        throw new ISOException ("Not available on Leaf");
    }
    /**
     * not available on Leaf - always throw ISOException
     * @param b
     * @return never returns
     * @exception ISOException
     */
    @Override
    public int unpack(byte[] b) throws ISOException {
        throw new ISOException ("Not available on Leaf");
    }
    /**
     * not available on Leaf - always throw ISOException
     * @param in
     * @exception ISOException
     */
    @Override
    public void unpack(InputStream in) throws ISOException {
        throw new ISOException ("Not available on Leaf");
    }
    /**
     * @return Object representing this field number
     */
    @Override
    public Object getKey() {
        return fieldNumber;
    }
    /**
     * @return Object representing this field value
     */
    @Override
    public Object getValue() {
        return value;
    }
    /**
     * @param obj - Object representing this field value
     * @exception ISOException
     */
    @Override
    public void setValue(Object obj) throws ISOException {
        if (obj instanceof String)
            value = (String) obj;
        else
            value = obj.toString();
    }
    /**
     * @return byte[] representing this field
     */
    @Override
    public byte[] getBytes() {
        return value != null ? value.getBytes(ISOUtil.CHARSET) : new byte[] {};
    }
    /**
     * dump this field to PrintStream. The output is sorta
     * XML, intended to be easily parsed.
     * @param p - print stream
     * @param indent - optional indent string
     */
    @Override
    public void dump (PrintStream p, String indent) {
        if (value != null) {
            if (value.indexOf('<') >= 0) {
                p.print(indent + "<" + XMLPackager.ISOFIELD_TAG + " " +
                        XMLPackager.ID_ATTR + "=\"" + fieldNumber + "\"><![CDATA[");
                p.print(value);
                p.println("]]></" + XMLPackager.ISOFIELD_TAG + ">");
            } else if (value.startsWith("{")) {
                p.print(indent + "<" + XMLPackager.ISOFIELD_TAG + " " +
                        XMLPackager.ID_ATTR + "=\"" + fieldNumber + "\"><![CDATA[");
                p.print(value);
                p.println("]]></" + XMLPackager.ISOFIELD_TAG + ">");
            } else {
                p.println(indent + "<" + XMLPackager.ISOFIELD_TAG + " " +
                        XMLPackager.ID_ATTR + "=\"" + fieldNumber + "\" " +
                        XMLPackager.VALUE_ATTR
                        + "=\"" + ISOUtil.normalize(value) + "\"/>");
            }
        }
    }
    /**
     * changes this Component field number<br>
     * Use with care, this method does not change
     * any reference held by a Composite.
     * @param fieldNumber new field number
     */
    @Override
    public void setFieldNumber (int fieldNumber) {
        this.fieldNumber = fieldNumber;
    }

    @Override
    public int getFieldNumber () {
        return fieldNumber;
    }
    @Override
    public void writeExternal (ObjectOutput out) throws IOException {
        out.writeShort (fieldNumber);
        out.writeUTF (value);
    }
    @Override
    public void readExternal  (ObjectInput in) 
        throws IOException, ClassNotFoundException
    {
        fieldNumber = in.readShort ();
        value       = in.readUTF();
    }
}

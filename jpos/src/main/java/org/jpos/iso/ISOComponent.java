/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Map;

/**
 * implements a <b>Component</b>
 * within a <b>Composite pattern</b>
 *
 * See 
 * <a href="/doc/javadoc/overview-summary.html">Overview</a> for details.
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOMsg
 * @see ISOField
 * @see ISOException
 */
public abstract class ISOComponent implements Cloneable {
    /**
     * Set a field within this message
     * @param c - a component
     * @exception ISOException on ISO error
     */
    public void set (ISOComponent c) throws ISOException {
        throw new ISOException ("Can't add to Leaf");
    }
    /**
     * Unset a field
     * @param fldno - the field number
     * @exception ISOException on ISO error
     */
    public void unset (int fldno) throws ISOException {
        throw new ISOException ("Can't remove from Leaf");
    }
    /**
     * In order to interchange <b>Composites</b> and <b>Leafs</b> we use
     * getComposite(). A <b>Composite component</b> returns itself and
     * a Leaf returns null. The base class ISOComponent provides
     * <b>Leaf</b> functionality.
     *
     * @return ISOComponent
     */
    public ISOComponent getComposite() {
        return null;
    }
    /**
     * valid on Leafs only.
     * The value returned is used by ISOMsg as a key
     * to this field.
     *
     * @return object representing the field number
     * @exception ISOException on ISO error
     */
    public Object getKey() throws ISOException {
        throw new ISOException ("N/A in Composite");
    }
    /**
     * valid on Leafs only.
     * @return object representing the field value
     * @exception ISOException on ISO error
     */
    public Object getValue() throws ISOException {
        throw new ISOException ("N/A in Composite");
    }
    /**
     * get Value as bytes (when possible)
     * @return byte[] representing this field
     * @exception ISOException on ISO error
     */
    public byte[] getBytes() throws ISOException {
        throw new ISOException ("N/A in Composite");
    }
    /**
     * a Composite must override this function
     * @return the max field number associated with this message
     */
    public int getMaxField() {
        return 0;
    }
    /**
     * dummy behaviour - return empty map
     * @return children (in this case 0 children)
     */
    public Map getChildren() {
        return Collections.emptyMap();
    }
    /**
     * changes this Component field number<br>
     * Use with care, this method does not change
     * any reference held by a Composite.
     * @param fieldNumber new field number
     */
    public abstract void setFieldNumber (int fieldNumber);
    /**
     * Returns the field number of this component within its parent.
     * @return the field number
     */
    public abstract int getFieldNumber ();
    /**
     * Sets the value of this component.
     * @param obj the value to set
     * @throws ISOException if the value is invalid
     */
    public abstract void setValue(Object obj) throws ISOException;
    /**
     * Returns the packed byte representation of this component.
     * @return packed byte array
     * @throws ISOException on packing error
     */
    public abstract byte[] pack() throws ISOException;
    /**
     * Unpacks this component from the given byte array.
     * @param b the byte array to unpack from
     * @return number of bytes consumed
     * @throws ISOException on unpacking error
     */
    public abstract int unpack(byte[] b) throws ISOException;
    /**
     * Dumps this component to the given PrintStream.
     * @param p the PrintStream to write to
     * @param indent indentation prefix
     */
    public abstract void dump (PrintStream p, String indent);
    /**
     * Packs this component and writes the result to the given OutputStream.
     * @param out the OutputStream to write packed bytes to
     * @throws IOException on write error
     * @throws ISOException on packing error
     */
    public void pack (OutputStream out) throws IOException, ISOException {
        out.write (pack ());
    }
    /**
     * Unpacks this component from the given InputStream.
     * @param in the InputStream to read from
     * @throws IOException on read error
     * @throws ISOException on unpacking error
     */
    public abstract void unpack (InputStream in) throws IOException, ISOException;
}

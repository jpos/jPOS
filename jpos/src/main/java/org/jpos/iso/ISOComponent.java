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
    /** Default constructor; no instance state to initialise. */
    protected ISOComponent() {}
    /**
     * Set a field within this message
     * @param c - a component
     * @exception ISOException always thrown by leaves; composites override this
     */
    public void set (ISOComponent c) throws ISOException {
        throw new ISOException ("Can't add to Leaf");
    }
    /**
     * Unset a field
     * @param fldno - the field number
     * @exception ISOException always thrown by leaves; composites override this
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
     * @exception ISOException thrown by composites; leaves return their key
     */
    public Object getKey() throws ISOException {
        throw new ISOException ("N/A in Composite");
    }
    /**
     * valid on Leafs only.
     * @return object representing the field value
     * @exception ISOException thrown by composites; leaves return their value
     */
    public Object getValue() throws ISOException {
        throw new ISOException ("N/A in Composite");
    }
    /**
     * get Value as bytes (when possible)
     * @return byte[] representing this field
     * @exception ISOException thrown by composites or when the value cannot be rendered as bytes
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
     * Returns the field number this component occupies within its container.
     *
     * @return the field number
     */
    public abstract int getFieldNumber ();
    /**
     * Sets the value of this component.
     *
     * @param obj new value
     * @throws ISOException if the value is rejected by the component implementation
     */
    public abstract void setValue(Object obj) throws ISOException;
    /**
     * Packs this component into its on-wire byte representation.
     *
     * @return packed bytes
     * @throws ISOException if packing fails
     */
    public abstract byte[] pack() throws ISOException;
    /**
     * Unpacks this component from {@code b} starting at offset 0.
     *
     * @param b packed bytes
     * @return number of bytes consumed
     * @throws ISOException if unpacking fails
     */
    public abstract int unpack(byte[] b) throws ISOException;
    /**
     * Writes a human-readable dump of this component for diagnostics.
     *
     * @param p destination stream
     * @param indent prefix to apply to every emitted line
     */
    public abstract void dump (PrintStream p, String indent);
    /**
     * Packs this component and writes it to {@code out}.
     *
     * @param out destination stream
     * @throws IOException if writing fails
     * @throws ISOException if packing fails
     */
    public void pack (OutputStream out) throws IOException, ISOException {
        out.write (pack ());
    }
    /**
     * Unpacks this component by reading from {@code in}.
     *
     * @param in source stream
     * @throws IOException if reading fails
     * @throws ISOException if unpacking fails
     */
    public abstract void unpack (InputStream in) throws IOException, ISOException;
}

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

package org.jpos.iso;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Hashtable;

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
     * @exception ISOException
     */
    public void set (ISOComponent c) throws ISOException {
        throw new ISOException ("Can't add to Leaf");
    }
    /**
     * Unset a field
     * @param fldno - the field number
     * @exception ISOException
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
     * @exception ISOException
     */
    public Object getKey() throws ISOException {
        throw new ISOException ("N/A in Composite");
    }
    /**
     * valid on Leafs only.
     * @return object representing the field value
     * @exception ISOException
     */
    public Object getValue() throws ISOException {
        throw new ISOException ("N/A in Composite");
    }
    /**
     * get Value as bytes (when possible)
     * @return byte[] representing this field
     * @exception ISOException
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
     * dummy behaviour - return 0 elements Hashtable
     * @return children (in this case 0 children)
     */
    public Hashtable getChildren() {
        return new Hashtable();
    }
    /**
     * changes this Component field number<br>
     * Use with care, this method does not change
     * any reference held by a Composite.
     * @param fieldNumber new field number
     */
    public abstract void setFieldNumber (int fieldNumber);
    public abstract void setValue(Object obj) throws ISOException;
    public abstract byte[] pack() throws ISOException;
    public abstract int unpack(byte[] b) throws ISOException;
    public abstract void dump (PrintStream p, String indent);
    public void pack (OutputStream out) throws IOException, ISOException {
        out.write (pack ());
    }
    public abstract void unpack (InputStream in) throws IOException, ISOException;
}

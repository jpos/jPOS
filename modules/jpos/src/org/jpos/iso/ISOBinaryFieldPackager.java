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

/**
 * @author joconnor
 * @version $Revision$ $Date$
 */
public class ISOBinaryFieldPackager extends ISOFieldPackager
{
    private BinaryInterpreter interpreter;
    private Prefixer prefixer;

    /**
     * Constructs a default ISOBinaryFieldPackager. There is no length prefix and a
     * literal interpretation. The set methods must be called to make this
     * ISOBinaryFieldPackager useful.
     */
    public ISOBinaryFieldPackager()
    {
        super();
        this.interpreter = LiteralBinaryInterpreter.INSTANCE;
        this.prefixer = NullPrefixer.INSTANCE;
    }

    /**
     * Creates an ISOBinaryFieldPackager.
     * @param maxLength The maximum length of the field in characters or bytes depending on the datatype.
     * @param description The description of the field. For human readable output.
     * @param interpreter The interpreter used to encode the field.
     * @param prefixer The type of length prefixer used to encode this field.
     */
    public ISOBinaryFieldPackager(int maxLength, String description,
                                  BinaryInterpreter interpreter, Prefixer prefixer)
    {
        super(maxLength, description);
        this.interpreter = interpreter;
        this.prefixer = prefixer;
    }

    /**
     * Creates an ISOBinaryFieldPackager.
     * @param interpreter The interpreter used to encode the field.
     * @param prefixer The type of length prefixer used to encode this field.
     */
    public ISOBinaryFieldPackager(BinaryInterpreter interpreter, Prefixer prefixer)
    {
        super();
        this.interpreter = interpreter;
        this.prefixer = prefixer;
    }

    /**
     * Sets the Interpreter.
     * @param interpreter The interpreter to use in packing and unpacking.
     */
    public void setInterpreter(BinaryInterpreter interpreter)
    {
        this.interpreter = interpreter;
    }

    /**
     * Sets the length prefixer.
     * @param prefixer The length prefixer to use during packing and unpacking.
     */
    public void setPrefixer(Prefixer prefixer)
    {
        this.prefixer = prefixer;
    }

    /**
	 * @see org.jpos.iso.ISOFieldPackager#getMaxPackedLength()
	 */
    public int getMaxPackedLength()
    {
        return prefixer.getPackedLength() + interpreter.getPackedLength(getLength());
    }

    /**
	 * Convert the component into a byte[].
	 */
    public byte[] pack(ISOComponent c) throws ISOException
    {
        try
        {
            byte[] data = (byte[])c.getValue();
            int packedLength = prefixer.getPackedLength();
            if (packedLength == 0)
            {
                if (data.length != getLength())
                {
                    throw new ISOException("Binary data length not the same as the packager length (" + data.length + "/" + getLength() + ")");
                }
            }
            byte[] ret = new byte[interpreter.getPackedLength(data.length) + packedLength];
            prefixer.encodeLength(data.length, ret);
            interpreter.interpret(data, ret, packedLength);
            return ret;
        } catch(Exception e)
        {
            throw new ISOException(makeExceptionMessage(c, "packing"), e);
        }
    }

    /**
	 * @see org.jpos.iso.ISOFieldPackager#unpack(org.jpos.iso.ISOComponent,
	 *      byte[], int)
	 */
    public int unpack(ISOComponent c, byte[] b, int offset) throws ISOException
    {
        try
        {
            int len = prefixer.decodeLength(b, offset);
            if (len == -1)
            {
                // The prefixer doesn't know how long the field is, so use
    			// maxLength instead
                len = getLength();
            }
            int lenLen = prefixer.getPackedLength();
            byte[] unpacked = interpreter.uninterpret(b, offset + lenLen, len);
            c.setValue(unpacked);
            return lenLen + interpreter.getPackedLength(len);
        } catch(Exception e)
        {
            throw new ISOException(makeExceptionMessage(c, "unpacking"), e);
        }
    }

    /** Unpack from an input stream */
    public void unpack (ISOComponent c, InputStream in) 
        throws IOException, ISOException
    {
        try
        {
            int lenLen = prefixer.getPackedLength ();
            int len;
            if (lenLen == 0)
            {
                len = getLength();
            } else
            {
                len = prefixer.decodeLength (readBytes (in, lenLen), 0);
            }
            int packedLen = interpreter.getPackedLength(len);
            byte[] unpacked = interpreter.uninterpret(readBytes (in, packedLen), 0, len);
            c.setValue(unpacked);
        } catch(ISOException e)
        {
            throw new ISOException(makeExceptionMessage(c, "unpacking"), e);
        }
    }

    /**
     * component factory
     * @param fieldNumber - the field number
     * @return the newly created component
     */
    public ISOComponent createComponent(int fieldNumber) {
        return new ISOBinaryField (fieldNumber);
    }

    /** Create a nice readable message for errors */
    private String makeExceptionMessage(ISOComponent c, String operation) {
        Object fieldKey = "unknown";
        if (c != null)
        {
            try
            {
                fieldKey = c.getKey();
            } catch (Exception ignore)
            {
            }
        }
        return getClass().getName() + ": Problem " + operation + " field " + fieldKey;
    }


    /**
     * Checks the length of the data against the maximum, and throws an IllegalArgumentException.
     * This is designed to be called from field Packager constructors and the setLength()
     * method.
     * @param len The length of the data for this field packager.
     * @param maxLength The maximum length allowed for this type of field packager.
     *          This depends on the prefixer that is used.
     * @throws IllegalArgumentException If len > maxLength.
     */
    protected void checkLength(int len, int maxLength) throws IllegalArgumentException
    {
        if (len > maxLength)
        {
            throw new IllegalArgumentException("Length " + len + " too long for " + getClass().getName());
        }
    }
}

/*
 * Copyright (c) 2000 jPOS.org. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  3. The end-user documentation included with the redistribution, if any,
 * must include the following acknowledgment: "This product includes software
 * developed by the jPOS project (http://www.jpos.org/)". Alternately, this
 * acknowledgment may appear in the software itself, if and wherever such
 * third-party acknowledgments normally appear.
 *  4. The names "jPOS" and "jPOS.org" must not be used to endorse or promote
 * products derived from this software without prior written permission. For
 * written permission, please contact license@jpos.org.
 *  5. Products derived from this software may not be called "jPOS", nor may
 * "jPOS" appear in their name, without prior written permission of the jPOS
 * project.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE JPOS
 * PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the jPOS Project. For more information please see
 * <http://www.jpos.org/> .
 */

package org.jpos.iso;

import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOFieldPackager;

/**
 * @author joconnor
 * @version $Revision$ $Date$
 */
public class ISOBaseFieldPackager extends ISOFieldPackager
{
    public static final int DT_BINARY = 0;
    public static final int DT_STRING = 1;
    public static final int DT_NUMERIC = 2;
    public static final int DT_AMOUNT = 3;

    private int datatype;
    private Interpreter interpreter;
    private Padder padder;
    private Prefixer prefixer;

    /**
     * Creates an ISOBaseFieldPackager.
     * @param maxLength The maximum length of the field in characters or bytes depending on the datatype.
     * @param description The description of the field. For human readable output.
     * @param datatype The type of data stored in the field. One of DT_BINARY, DT_STRING, DT_NUMERIC or DT_AMOUNT.
     * @param interpreter The interpreter used to encode the field.
     * @param padder The type of padding used.
     * @param prefixer The type of length prefixer used to encode this field.
     */
    public ISOBaseFieldPackager(int maxLength, String description, int datatype, Interpreter interpreter,
            Padder padder, Prefixer prefixer)
    {
        super(maxLength, description);
        this.datatype = datatype;
        this.interpreter = interpreter;
        this.padder = padder;
        this.prefixer = prefixer;
    }

    /**
     * Constructs a default ISOBaseFieldPackager. The datatype is DT_STRING, no padding,
     * no length prefix and a literal interpretation. The set methods must be called to
     * make this ISOBaseFieldPackager useful.
     */
    public ISOBaseFieldPackager()
    {
        super();
        this.datatype = DT_STRING;
        this.padder = NullPadder.INSTANCE;
        this.interpreter = LiteralInterpreter.INSTANCE;
        this.prefixer = NullPrefixer.INSTANCE;
    }

    /**
     * Sets the data type.
     * @param dataType One of DT_BINARY, DT_STRING, DT_NUMERIC or DT_AMOUNT.
     */
    public void setDataType(int dataType)
    {
        this.datatype = dataType;
    }

    /**
     * Sets the Interpreter.
     * @param interpreter The interpreter to use in packing and unpacking.
     */
    public void setInterpreter(Interpreter interpreter)
    {
        this.interpreter = interpreter;
    }

    /**
     * Sets the Padder.
     * @param padder The padder to use during packing and unpacking.
     */
    public void setPadder(Padder padder)
    {
        this.padder = padder;
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
        return prefixer.getLengthInBytes() + interpreter.getPackedLength(getLength());
    }

    /**
	 * Convert the component into a byte[].
	 */
    public byte[] pack(ISOComponent c) throws ISOException
    {
        switch (datatype)
        {
            case DT_STRING :
            case DT_NUMERIC :
                return packString((String)c.getValue());
            case DT_AMOUNT :
                return packAmount((String)c.getValue());
            case DT_BINARY :
                return packBinary((byte[])c.getValue());
            default :
                throw new IllegalStateException("Invalid datatype = " + datatype);
        }
    }

    /**
     * Packs the string into a byte array.
     * @param data The string to pack.
     * @return A packed string.
     * @throws ISOException
     */
    private byte[] packString(String data) throws ISOException
    {
        String paddedData = padder.pad(data, getLength());
        byte[] rawData = new byte[prefixer.getLengthInBytes()
                + interpreter.getPackedLength(paddedData.length())];
        prefixer.encodeLength(paddedData.length(), rawData);
        interpreter.interpret(paddedData, rawData, prefixer.getLengthInBytes());
        return rawData;
    }

    /**
     * Packs the string into a byte array.
     * @param data The string to pack.
     * @return A packed string.
     * @throws ISOException
     */
    private byte[] packAmount(String data) throws ISOException
    {
        String sign = data.substring(0, 1);
        String amount = data.substring(1);
        String paddedData = padder.pad(amount, getLength()-1);
        int signLength = interpreter.getPackedLength(1);
        byte[] rawData = new byte[prefixer.getLengthInBytes()
                + signLength
                + interpreter.getPackedLength(paddedData.length())];
        prefixer.encodeLength(paddedData.length(), rawData);
        interpreter.interpret(sign, rawData, prefixer.getLengthInBytes());
        interpreter.interpret(paddedData, rawData, prefixer.getLengthInBytes() + signLength);
        return rawData;
    }

    /**
     * Packs the string into a byte array.
     * @param data The string to pack.
     * @return A packed string.
     * @throws ISOException
     */
    private byte[] packBinary(byte[] data) throws ISOException
    {
        // TODO: Figure out how to handle byte[] data.
        byte[] paddedData = padder.padBinary(data, getLength());
        return paddedData;
    }

    /**
	 * @see org.jpos.iso.ISOFieldPackager#unpack(org.jpos.iso.ISOComponent,
	 *      byte[], int)
	 */
    public int unpack(ISOComponent c, byte[] b, int offset) throws ISOException
    {
        int len = prefixer.decodeLength(b, offset);
        if (len == -1)
        {
            // The prefixer doesn't know how long the field is, so use
			// maxLength instead
            len = getLength();
        }
        int lenLen = prefixer.getLengthInBytes();
        String unpacked = interpreter.uninterpret(b, offset + lenLen, len);
        c.setValue(unpacked);
        return lenLen + interpreter.getPackedLength(len);
    }
}
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

import java.io.IOException;
import java.io.InputStream;

/**
 * Generic class for handling binary fields in Tag-Len-Value format
 * <code>
 * Format is assemblied by header formatter
 * Where
 *       TT is the n>0 digit field number (Tag)
 *       LL is the n>=0 digit field length (if n=0 it's means fixed length field with prefixer)
 *       .. is the field content
 * </code>
 * @author Mikolaj Sosna
 * @version $Revision: 2854 $ $Date: 2010-01-02 11:34:31 +0100 (sob) $
 */
public class ISOFormattableBinaryFieldPackager extends ISOFieldPackager
{
    private Prefixer tagPrefixer;
    private BinaryInterpreter interpreter;
    private Padder padder;
    private Prefixer prefixer;
    private IsoFieldHeaderFormatter headerFormatter;

    /**
     * Constructs a default ISOTagBinaryFieldPackager. There is ASCII tag L prefixer, no padding,
     * no length prefix and a literal binary interpretation. The set methods must be called to
     * make this ISOBaseFieldPackager useful.
     */
    public ISOFormattableBinaryFieldPackager() {
        super();
        this.tagPrefixer = AsciiPrefixer.L;
        this.interpreter = LiteralBinaryInterpreter.INSTANCE;
        this.padder = NullPadder.INSTANCE;
        this.prefixer = NullPrefixer.INSTANCE;
        this.headerFormatter = IsoFieldHeaderFormatter.TAG_FIRST;
    }

    /**
     * Constructs an ISOTagBinaryFieldPackager with a specific Padder, Interpreter and Prefixer.
     * The length and description should be set with setLength() and setDescription methods.
     * @param tagPrefixer The type of tag prefixer used to encode tag.
     * @param padder The type of padding used.
     * @param interpreter The interpreter used to encode the field.
     * @param prefixer The type of length prefixer used to encode this field.
     */
    public ISOFormattableBinaryFieldPackager(Prefixer tagPrefixer, Padder padder,
                                             BinaryInterpreter interpreter, Prefixer prefixer) {
        super();
        this.tagPrefixer = tagPrefixer;
        this.padder = padder;
        this.interpreter = interpreter;
        this.prefixer = prefixer;
        this.headerFormatter = IsoFieldHeaderFormatter.TAG_FIRST;
    }

/**
     * Constructs an ISOTagBinaryFieldPackager with a specific Padder, Interpreter and Prefixer.
     * The length and description should be set with setLength() and setDescription methods.
     * @param tagPrefixer The type of tag prefixer used to encode tag.
     * @param padder The type of padding used.
     * @param interpreter The interpreter used to encode the field.
     * @param lengthPrefixer The type of length prefixer used to encode this field.
     * @param headerFormatter The format of TAG TT and Length LL part
     */
    public ISOFormattableBinaryFieldPackager(Prefixer tagPrefixer, Padder padder,
                                             BinaryInterpreter interpreter, Prefixer lengthPrefixer,
                                             IsoFieldHeaderFormatter headerFormatter) {
        super();
        this.tagPrefixer = tagPrefixer;
        this.padder = padder;
        this.interpreter = interpreter;
        this.prefixer = lengthPrefixer;
        this.headerFormatter = headerFormatter;
    }

    /**
     * Creates an ISOTagBinaryFieldPackager.
     * @param maxLength The maximum length of the field in characters or bytes depending on the datatype.
     * @param description The description of the field. For human readable output.
     * @param tagPrefixer The type of tag prefixer used to encode tag.
     * @param interpreter The interpreter used to encode the field.
     * @param padder The type of padding used.
     * @param lengthPrefixer The type of length prefixer used to encode this field.
     * @param headerFormatter The format of TAG TT and Length LL part
     */
    public ISOFormattableBinaryFieldPackager(int maxLength, String description, Prefixer tagPrefixer,
                                             Padder padder, BinaryInterpreter interpreter, Prefixer lengthPrefixer,
                                             IsoFieldHeaderFormatter headerFormatter) {
        super(maxLength, description);
        this.tagPrefixer = tagPrefixer;
        this.padder = padder;
        this.interpreter = interpreter;
        this.prefixer = lengthPrefixer;
        this.headerFormatter = headerFormatter;
    }

    /**
     * Sets the Padder.
     * @param padder The padder to use during packing and unpacking.
     */
    public void setPadder(Padder padder) {
        this.padder = padder;
    }

    /**
     * Sets the Interpreter.
     * @param interpreter The interpreter to use in packing and unpacking.
     */
    public void setInterpreter(BinaryInterpreter interpreter) {
        this.interpreter = interpreter;
    }

    /**
     * Sets the length prefixer.
     * @param prefixer The length prefixer to use during packing and unpacking.
     */
    public void setPrefixer(Prefixer prefixer) {
        this.prefixer = prefixer;
    }

    /**
     * Gets the formatter, which assembles tag TT and length LL parts in required format
     * @return the formatter of the header part (length and tag parts)
     */
    public IsoFieldHeaderFormatter getHeaderFormatter() {
        return headerFormatter;
    }

    /**
     * Sets the formatter, which assembles tag TT and length LL parts in required format
     * @param headerFormatter the formatter of the header part (length and tag parts)
     */
    public void setHeaderFormatter(IsoFieldHeaderFormatter headerFormatter) {
        this.headerFormatter = headerFormatter;
    }

    /**
     * Returns the prefixer's packed length and the interpreter's packed length.
     * @see ISOFieldPackager#getMaxPackedLength()
     */
    @Override
    public int getMaxPackedLength() {
        return tagPrefixer.getPackedLength() + prefixer.getPackedLength() + interpreter.getPackedLength(getLength());
    }

    /**
     * Create a nice readable message for errors
     */
    private String makeExceptionMessage(ISOComponent c, String operation) {
        Object fieldKey = "unknown";
        if (c != null)
            try{
                fieldKey = c.getKey();
            } catch (Exception ignore){}
        return this.getClass().getName() + ": Problem " + operation + " field " + fieldKey;
    }

    /**
     * Convert the component into a byte[].
     */
    @Override
    public byte[] pack(ISOComponent c) throws ISOException {
        try{
            byte[] valueBytes = (byte[])c.getValue();
            if (valueBytes.length < 0 || valueBytes.length > getLength())
                throw new ISOException("Field length " + valueBytes.length + " too long. Max: " + getLength());

            int tag = (Integer)c.getKey();
            byte[] paddedValueBytes = valueBytes;

            if (!(padder instanceof NullPadder)) //for save few cycles
              paddedValueBytes = ISOUtil.hex2byte(padder.pad(ISOUtil.hexString(valueBytes), getLength()));

            byte[] rawData = new byte[tagPrefixer.getPackedLength() + prefixer.getPackedLength() + interpreter.getPackedLength(paddedValueBytes.length)];
            byte[] rawTagData = new byte[tagPrefixer.getPackedLength()];
            tagPrefixer.encodeLength(tag, rawTagData);

            byte[] rawLen = new byte[prefixer.getPackedLength()];
            prefixer.encodeLength(!headerFormatter.isTagFirst() ? paddedValueBytes.length + tagPrefixer.getPackedLength() : paddedValueBytes.length, rawLen);

            headerFormatter.format(tagPrefixer, prefixer, rawTagData, rawLen, rawData);

            interpreter.interpret(paddedValueBytes, rawData, headerFormatter.getTotalLength(tagPrefixer, prefixer));

            return rawData;
        } catch(Exception e) {
            throw new ISOException(makeExceptionMessage(c, "packing"), e);
        }
    }

    /**
     * Unpacks the byte array into the component.
     * @param c The component to unpack into.
     * @param b The byte array to unpack.
     * @param offset The index in the byte array to start unpacking from.
     * @return The number of bytes consumed unpacking the component.
     */
    @Override
    public int unpack(ISOComponent c, byte[] b, int offset) throws ISOException {
        try{
            int tagLen = tagPrefixer.getPackedLength();
            c.setFieldNumber(tagPrefixer.decodeLength(b, offset + headerFormatter.getTagIndex(prefixer)));
            int len = prefixer.decodeLength(b, offset + headerFormatter.getLengthIndex(tagPrefixer));
            if (!headerFormatter.isTagFirst()) {
                len -= tagPrefixer.getPackedLength();
            }
            if (len == -1) {
                // The prefixer doesn't know how long the field is, so use
                // maxLength instead
                len = getLength();
            } else if (getLength() > 0 && len > getLength()) {
                throw new ISOException("Field length " + len + " too long. Max: " + getLength());
            }
            int lenLen = prefixer.getPackedLength();
            byte[] unpacked = interpreter.uninterpret(b, offset + tagPrefixer.getPackedLength() + prefixer.getPackedLength(), len);

            byte[] paddedValueBytes = unpacked;
            if (!(padder instanceof NullPadder)) //for save few cycles
                paddedValueBytes = ISOUtil.hex2byte(padder.unpad(ISOUtil.hexString(unpacked)));

            c.setValue(paddedValueBytes);
            return tagLen + lenLen + interpreter.getPackedLength(len);
        } catch(Exception e){
            throw new ISOException(makeExceptionMessage(c, "unpacking"), e);
        }
    }

    /**
     * Unpack the input stream into the component.
     * @param c  The Component to unpack into.
     * @param in Input stream where the packed bytes come from.
     * @exception IOException Thrown if there's a problem reading the input stream.
     */
    @Override
    public void unpack (ISOComponent c, InputStream in)
        throws IOException, ISOException {

        try {
            int tagLen = tagPrefixer.getPackedLength();
            int lenLen = prefixer.getPackedLength() == 0 ? getLength() : prefixer.getPackedLength();
            int len = -1;
            if (headerFormatter.getTagIndex(prefixer) == 0) {
                c.setFieldNumber(tagPrefixer.decodeLength(readBytes(in, tagLen), 0));
                len = prefixer.decodeLength(readBytes(in, lenLen), 0);
            } else {
                len = prefixer.decodeLength(readBytes(in, lenLen), 0);
                c.setFieldNumber(tagPrefixer.decodeLength(readBytes(in, tagLen), 0));
            }
            if (getLength() > 0 && len > 0 && len > getLength()) {
                throw new ISOException("Field length " + len + " too long. Max: " + getLength());
            }
            int packedLen = interpreter.getPackedLength(len);
            byte[] unpacked = interpreter.uninterpret(readBytes (in, packedLen), 0, len);
            c.setValue(unpacked);
        } catch(ISOException e){
            throw new ISOException(makeExceptionMessage(c, "unpacking"), e);
        }
    }

}

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

package org.jpos.tlv.packager.bertlv;


import org.jpos.emv.EMVStandardTagType;
import org.jpos.emv.UnknownTagNumberException;
import org.jpos.iso.AsciiInterpreter;
import org.jpos.iso.BCDInterpreter;
import org.jpos.iso.BinaryInterpreter;
import org.jpos.iso.ISOBinaryField;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOFieldPackager;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.Interpreter;
import org.jpos.iso.LiteralInterpreter;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.tlv.ISOTaggedField;
import org.jpos.tlv.TLVDataFormat;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;


/**
 * Packager for ISO 8825 BER TLV values.
 *
 * @author Vishnu Pillai
 */

public abstract class BERTLVPackager extends GenericPackager {

    private static final int MAX_LENGTH_BYTES = 5;
    private static final int MAX_TAG_BYTES = 3;

    private static final LiteralInterpreter literalInterpreter = LiteralInterpreter.INSTANCE;
    private static final AsciiInterpreter asciiInterpreter = AsciiInterpreter.INSTANCE;
    private static final BCDInterpreter bcdInterpreterLeftPaddedZero = BCDInterpreter.LEFT_PADDED;
    private static final BCDInterpreter bcdInterpreterRightPaddedF = BCDInterpreter.RIGHT_PADDED_F;

    private final BinaryInterpreter tagInterpreter;
    private final BinaryInterpreter lengthInterpreter;
    private final BinaryInterpreter valueInterpreter;


    public BERTLVPackager() throws ISOException {
        super();
        tagInterpreter = getTagInterpreter();
        lengthInterpreter = getLengthInterpreter();
        valueInterpreter = getValueInterpreter();
    }

    protected abstract BinaryInterpreter getTagInterpreter();

    protected abstract BinaryInterpreter getLengthInterpreter();

    protected abstract BinaryInterpreter getValueInterpreter();

    protected abstract BERTLVFormatMapper getTagFormatMapper();

    /**
     * Pack the sub-field into a byte array
     */
    @Override
    public byte[] pack(ISOComponent m) throws ISOException {
        return pack(m, false, getFirstField(), m.getMaxField());
    }

    public byte[] pack(ISOComponent m, boolean nested, int startIdx, int endIdx)
            throws ISOException {
        LogEvent evt = new LogEvent(this, "pack");
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream(100)) {
            ISOComponent c;
            Map fields = m.getChildren();
            for (int i = startIdx; i <= endIdx; i++) {
                c = (ISOComponent) fields.get(i);
                if (c != null) {
                    try {
                        final byte[] b;
                        if (c instanceof ISOTaggedField) {
                            b = packTLV((ISOTaggedField) c);
                        } else {

                            if (c.getValue() == null) {
                                b = new byte[0];
                            } else if (!nested && (i == startIdx || i == endIdx) &&
                                    this.fld.length > i && this.fld[i] != null) {
                                b = this.fld[i].pack(c);
                            } else {
                                throw new ISOException(
                                        "Field: " +
                                                i +
                                                " of type: " +
                                                c.getClass() +
                                                " cannot be packed. Either the object should be of type ISOTagField" +
                                                " OR this should be the first or last sub-field and a packager" +
                                                " should be configured for the same");
                            }
                        }
                        bout.write(b);
                    } catch (Exception e) {
                        evt.addMessage("error packing sub-field " + i);
                        evt.addMessage(c);
                        evt.addMessage(e);
                        throw e;
                    }
                }
            }

            byte[] d = bout.toByteArray();
            if (logger != null) // save a few CPU cycle if no logger available
                evt.addMessage(ISOUtil.hexString(d));
            return d;
        } catch (ISOException e) {
            evt.addMessage(e);
            throw e;
        } catch (Exception e) {
            evt.addMessage(e);
            throw new ISOException(e);
        } finally {
            Logger.log(evt);
        }
    }

    private byte[] packTLV(ISOTaggedField c) throws ISOException {
        byte[] b;
        final byte[] rawValueBytes;

        try {
            rawValueBytes = packValue(c.getTag(), c);
        } catch (UnknownTagNumberException e) {
            throw new ISOException(e);
        }

        byte[] valueBytes = new byte[valueInterpreter.getPackedLength(rawValueBytes.length)];
        valueInterpreter.interpret(rawValueBytes, valueBytes, 0);

        byte[] tagBytes = packTag(c);
        byte[] lengthBytes = packLength(valueBytes);

        b = new byte[tagBytes.length + lengthBytes.length + valueBytes.length];
        System.arraycopy(tagBytes, 0, b, 0, tagBytes.length);
        System.arraycopy(lengthBytes, 0, b, tagBytes.length, lengthBytes.length);
        System.arraycopy(valueBytes, 0, b, tagBytes.length + lengthBytes.length, valueBytes.length);
        return b;
    }

    private byte[] packTag(final ISOTaggedField c) {
        final byte[] tagBytes;
        String tag = c.getTag();
        tagBytes = ISOUtil.hex2byte(tag);
        byte[] packedTagBytes = new byte[tagInterpreter.getPackedLength(tagBytes.length)];
        tagInterpreter.interpret(tagBytes, packedTagBytes, 0);
        return packedTagBytes;
    }

    private byte[] packLength(final byte[] valueBytes) {
        final byte[] lengthBytes;
        int length = valueBytes.length;
        if (length > 0x7F) {
            byte[] lengthBytesSuffix = ISOUtil.int2byte(length);
            lengthBytes = new byte[lengthBytesSuffix.length + 1];
            lengthBytes[0] = (byte) (0x80 | lengthBytesSuffix.length);
            System.arraycopy(lengthBytesSuffix, 0, lengthBytes, 1, lengthBytesSuffix.length);
        } else {
            lengthBytes = new byte[]{(byte) length};
        }
        byte[] packedLengthBytes = new byte[lengthInterpreter.getPackedLength(lengthBytes.length)];
        lengthInterpreter.interpret(lengthBytes, packedLengthBytes, 0);
        return packedLengthBytes;

    }

    @Override
    public int unpack(ISOComponent m, byte[] b) throws ISOException {
        try {
            return unpack(m, b, false);
        } catch (RuntimeException e) {
            throw new ISOException(e);
        }
    }

    public int unpack(ISOComponent m, byte[] b, boolean nested) throws ISOException {
        LogEvent evt = new LogEvent(this, "unpack");
        try {
            if (m.getComposite() == null)
                throw new ISOException("Can't call packager on non Composite");
            if (b.length == 0)
                return 0; // nothing to do
            if (logger != null) // save a few CPU cycle if no logger available
                evt.addMessage(ISOUtil.hexString(b));

            int tlvDataLength = b.length;

            int consumed = 0;
            int subFieldNumber = 1;
            if (!nested && fld.length > 1) {
                ISOFieldPackager packager = fld[1];
                if (packager != null) {
                    ISOComponent subField = packager.createComponent(1);
                    consumed = consumed + packager.unpack(subField, b, consumed);
                    m.set(subField);
                }
                subFieldNumber++;
            }

            while (consumed < tlvDataLength) {
                ISOFieldPackager packager;
                if (!nested && fld.length > 1 && (packager = fld[fld.length - 1]) != null &&
                        packager.getLength() == tlvDataLength - consumed) {
                    ISOComponent subField = packager.createComponent(fld.length - 1);
                    consumed = consumed + packager.unpack(subField, b, consumed);
                    m.set(subField);
                    subFieldNumber++;
                } else {
                    //Read the Tag per BER
                    UnpackResult tagUnpackResult = unpackTag(b, consumed);
                    consumed = consumed + tagUnpackResult.consumed;
                    final byte[] tagBytes = tagUnpackResult.value;
                    String tag = ISOUtil.byte2hex(tagBytes).toUpperCase();
                    UnpackResult lengthUnpackResult = unpackLength(b, consumed);
                    consumed = consumed + lengthUnpackResult.consumed;
                    int length = ISOUtil.byte2int(lengthUnpackResult.value);

                    final ISOComponent tlvSubFieldData;
                    byte[] value = new byte[length];

                    if (length > 0) {
                        System.arraycopy(b, consumed, value, 0, value.length);
                    }

                    int uninterpretLength = getUninterpretLength(length, valueInterpreter);
                    byte[] rawValueBytes =
                            valueInterpreter.uninterpret(value, 0, uninterpretLength);

                    tlvSubFieldData = unpackValue(tag, rawValueBytes, subFieldNumber, length);
                    consumed = consumed + length;
                    ISOTaggedField tlv = new ISOTaggedField(tag, tlvSubFieldData);
                    m.set(tlv);
                    subFieldNumber++;
                }
            }
            if (b.length != consumed) {
                evt.addMessage("WARNING: unpack len=" + b.length + " consumed=" + consumed);
            }
            return consumed;
        } catch (ISOException e) {
            evt.addMessage(e);
            throw e;
        } catch (Exception e) {
            evt.addMessage(e);
            throw new ISOException(e);
        } finally {
            Logger.log(evt);
        }
    }

    private UnpackResult unpackTag(final byte[] tlvData, final int offset) {
        byte[] tlvBytesHex =
                tagInterpreter.uninterpret(
                        tlvData,
                        offset,
                        tlvData.length >= offset + MAX_TAG_BYTES
                                ? MAX_TAG_BYTES : tlvData.length - offset);
        int index = 0;
        final byte[] tagBytes;
        byte tagByte = tlvBytesHex[index];
        int tagLength = 1;
        if ((tagByte & 0x1F) == 0x1F) {
            tagLength++;
            tagByte = tlvBytesHex[index + 1];
            while (/* tagLength < MAX_TAG_BYTES && */(tagByte & 0x80) == 0x80) {
                tagLength++;
                tagByte = tlvBytesHex[index + tagLength - 1];
            }
            tagBytes = new byte[tagLength];
            System.arraycopy(tlvBytesHex, index, tagBytes, 0, tagBytes.length);
        } else {
            tagBytes = new byte[]{tagByte};
        }
        return new UnpackResult(tagBytes, tagInterpreter.getPackedLength(tagLength));
    }

    private UnpackResult unpackLength(final byte[] tlvData, final int offset) {
        byte[] tlvBytesHex =
                lengthInterpreter.uninterpret(
                        tlvData,
                        offset,
                        tlvData.length >= offset + MAX_LENGTH_BYTES
                                ? MAX_LENGTH_BYTES : tlvData.length - offset);
        final byte length = tlvBytesHex[0];
        final int lengthLength;
        final byte[] lengthBytes;
        if ((length & 0x80) == 0x80) {
            //Long Form
            int lengthOctetsCount = length & 0x7F;
            lengthLength = lengthOctetsCount + 1;
            lengthBytes = new byte[lengthOctetsCount];
            System.arraycopy(tlvBytesHex, 1, lengthBytes, 0, lengthOctetsCount);
        } else {
            //Short Form
            lengthLength = 1;
            lengthBytes = new byte[]{length};
        }
        return new UnpackResult(lengthBytes, lengthInterpreter.getPackedLength(lengthLength));
    }

    protected byte[] packValue(String tagNameHex, final ISOComponent c) throws ISOException,
            UnknownTagNumberException {
        final int tagNumber = Integer.parseInt(tagNameHex, 16);
        final TLVDataFormat dataFormat = getTagFormatMapper().getFormat(tagNumber);
        String tagValue;
        byte[] packedValue;

        if (c.getComposite() == null) {
            if (c.getValue() instanceof String) {
                tagValue = (String) c.getValue();
                EMVStandardTagType tagType;
                int length;
                if (EMVStandardTagType.isProprietaryTag(Integer.parseInt(tagNameHex, 16))) {
                    length = tagValue.length();
                } else {
                    tagType = EMVStandardTagType.forHexCode(tagNameHex);
                    length = Math.max(tagValue.length(), tagType.getDataLength().getMinLength());
                }
                switch (dataFormat) {
                    case COMPRESSED_NUMERIC:
                        packedValue = new byte[bcdInterpreterRightPaddedF.getPackedLength(length)];
                        bcdInterpreterRightPaddedF.interpret(tagValue, packedValue, 0);
                        break;
                    case PACKED_NUMERIC:
                    case PACKED_NUMERIC_DATE_YYMMDD:
                    case PACKED_NUMERIC_TIME_HHMMSS:
                        packedValue = new byte[bcdInterpreterLeftPaddedZero.getPackedLength(length)];
                        bcdInterpreterLeftPaddedZero.interpret(ISOUtil.zeropad(tagValue, length), packedValue, 0);
                        break;
                    case ASCII_NUMERIC:
                    case ASCII_ALPHA:
                    case ASCII_ALPHA_NUMERIC:
                    case ASCII_ALPHA_NUMERIC_SPACE:
                    case ASCII_ALPHA_NUMERIC_SPECIAL:
                        packedValue = new byte[asciiInterpreter.getPackedLength(tagValue.length())];
                        asciiInterpreter.interpret(tagValue, packedValue, 0);
                        break;
                    case BINARY:
                    case PROPRIETARY:
                        packedValue = new byte[literalInterpreter.getPackedLength(tagValue.length())];
                        literalInterpreter.interpret(tagValue, packedValue, 0);
                        break;
                    case CONSTRUCTED:
                        throw new IllegalArgumentException("CONSTRUCTED tag value should be a composite ISOComponent");
                        //packedValue = new byte[literalInterpreter.getPackedLength(tagValue.length())];
                        //literalInterpreter.interpret(tagValue, packedValue, 0);
                        //break;
                    default:
                        throw new IllegalArgumentException("Unknown TLVDataFormat: " + dataFormat);
                }
            } else {
                packedValue = c.getBytes();
            }
        } else {
            if (TLVDataFormat.CONSTRUCTED.equals(dataFormat) || TLVDataFormat.PROPRIETARY.equals(dataFormat)) {
                packedValue = pack(c, true, 0, c.getMaxField());
            } else {
                throw new IllegalArgumentException("Composite ISOComponent should be used only for CONSTRUCTED data type");
            }
        }
        return packedValue;
    }

    private ISOComponent unpackValue(String tagNameHex, final byte[] tlvData,
                                     int subFieldNumber, int dataLength) throws ISOException, UnknownTagNumberException {
        final int tagNumber = Integer.parseInt(tagNameHex, 16);
        final TLVDataFormat dataFormat = getTagFormatMapper().getFormat(tagNumber);
        ISOComponent value;
        String unpackedValue;
        int uninterpretLength;
        switch (dataFormat) {
            case COMPRESSED_NUMERIC:
                uninterpretLength = getUninterpretLength(dataLength, bcdInterpreterRightPaddedF);
                unpackedValue = bcdInterpreterRightPaddedF.uninterpret(tlvData, 0, uninterpretLength);
                value = new ISOField(subFieldNumber, ISOUtil.unPadRight(unpackedValue, 'F'));
                break;
            case PACKED_NUMERIC:
                uninterpretLength = getUninterpretLength(dataLength, bcdInterpreterLeftPaddedZero);
                unpackedValue = bcdInterpreterLeftPaddedZero.uninterpret(tlvData, 0, uninterpretLength);
                value = new ISOField(subFieldNumber, ISOUtil.unPadLeft(unpackedValue, '0'));
                break;
            case PACKED_NUMERIC_DATE_YYMMDD:
            case PACKED_NUMERIC_TIME_HHMMSS:
                uninterpretLength = getUninterpretLength(dataLength, bcdInterpreterLeftPaddedZero);
                unpackedValue = bcdInterpreterLeftPaddedZero.uninterpret(tlvData, 0, uninterpretLength);
                value = new ISOField(subFieldNumber, unpackedValue);
                break;
            case ASCII_NUMERIC:
            case ASCII_ALPHA:
            case ASCII_ALPHA_NUMERIC:
            case ASCII_ALPHA_NUMERIC_SPACE:
            case ASCII_ALPHA_NUMERIC_SPECIAL:
                uninterpretLength = getUninterpretLength(dataLength, asciiInterpreter);
                unpackedValue = asciiInterpreter.uninterpret(tlvData, 0, uninterpretLength);
                value = new ISOField(subFieldNumber, unpackedValue);
                break;
            case BINARY:
            case PROPRIETARY:
                value = new ISOBinaryField(subFieldNumber, tlvData);
                break;
            case CONSTRUCTED:
                value = new ISOMsg(subFieldNumber);
                unpack(value, tlvData, true);
                break;
            default:
                throw new IllegalArgumentException("Unknown TLVDataFormat: " + dataFormat);
        }
        return value;
    }

    @Override
    public void unpack(ISOComponent m, InputStream in) throws IOException, ISOException {
        throw new IllegalStateException(
                "Call to unpack(ISOComponent m, InputStream in) was not expected.");
    }

    private int getUninterpretLength(int length, BinaryInterpreter interpreter) {
        if (length > 0) {
            int lengthAdjusted = length + length % 2;
            return length * (lengthAdjusted / interpreter.getPackedLength(lengthAdjusted));
        }
        return 0;
    }

    private int getUninterpretLength(int length, Interpreter interpreter) {
        if (length > 0) {
            int lengthAdjusted = length + length % 2;
            return length * (lengthAdjusted / interpreter.getPackedLength(lengthAdjusted));
        }
        return 0;
    }

    private class UnpackResult {

        private final byte[] value;
        private final int consumed;

        private UnpackResult(final byte[] value, final int consumed) {
            this.value = value;
            this.consumed = consumed;
        }
    }

}

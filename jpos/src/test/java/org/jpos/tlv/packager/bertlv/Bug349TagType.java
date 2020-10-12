package org.jpos.tlv.packager.bertlv;

import org.jpos.emv.EMVProprietaryTagType;
import org.jpos.emv.EMVTag;
import org.jpos.emv.EMVTagType;
import org.jpos.emv.ProprietaryFormatException;
import org.jpos.emv.UnknownTagNumberException;
import org.jpos.iso.ISOUtil;
import org.jpos.tlv.TLVDataFormat;

import java.util.HashMap;
import java.util.Map;

public enum Bug349TagType implements EMVProprietaryTagType {

    BMP55_SF14(14, "Terminal Capabilities",
            "Indicates the card data input, CVM, and security capabilities of the terminal",
            DataSource.TERMINAL, TLVDataFormat.BINARY, new FixedDataLength(3), 0x00, new FixedByteLength(3)),

    BMP55_SF99(99, "Message Control field",
            "Message control field for terminal configuration", EMVTagType.DataSource.TERMINAL,
            TLVDataFormat.ASCII_NUMERIC, new EMVTagType.FixedDataLength(2), 0x00, new FixedByteLength(2));

    private final int tagNumber;
    private final String tagShortDescription;
    private final String tagDescription;
    private final EMVTagType.DataSource source;
    private final TLVDataFormat format;
    private final Integer template;
    private final EMVTagType.DataLength dataLength;
    private final EMVTagType.ByteLength byteLength;

    Bug349TagType(final int tagNumber, final String tagName, final String tagDescription,
                  final EMVTagType.DataSource source, final TLVDataFormat format, final EMVTagType.DataLength dataLength,
                  final Integer template, final EMVTagType.ByteLength byteLength) {
        this.tagNumber = tagNumber;
        this.tagShortDescription = tagName;
        this.tagDescription = tagDescription;
        this.source = source;
        this.format = format;
        this.template = template;
        this.dataLength = dataLength;
        this.byteLength = byteLength;
        if (!(0 == tagNumber)) {
            if (Bug349TagType.MapHolder.tagCodeMap.containsKey(tagNumber)) {
                throw new IllegalStateException(
                        "Illegal attempt to add duplicate EMVTagType with tagNumber: " + tagNumber +
                                ". Enum: " + this.name());
            }
            Bug349TagType.MapHolder.tagCodeMap.put(tagNumber, this);
        }
    }

    public static boolean isProprietaryTag(int code) {
        EMVTagType tagType = Bug349TagType.MapHolder.tagCodeMap.get(code);
        return tagType == null;
    }

    public static Bug349TagType forCode(int code) throws UnknownTagNumberException {
        Bug349TagType tagType = Bug349TagType.MapHolder.tagCodeMap.get(code);
        if (tagType == null) {
            throw new UnknownTagNumberException(String.valueOf(code));
        }
        return tagType;
    }

    public static Bug349TagType forHexCode(String hexString) throws UnknownTagNumberException {
        return forCode(Integer.parseInt(hexString, 16));
    }

    @Override
    public int getTagNumber() {
        return tagNumber;
    }

    public boolean isProprietaryTag() {
        return false;
    }

    public int getTagNumberLength() {
        return tagNumber > 0xFF ? 2 : 1;
    }

    public String getTagNumberHex() {
        return Integer.toHexString(tagNumber).toUpperCase();
    }

    public byte[] getTagNumberBytes() {
        return ISOUtil.int2byte(tagNumber);
    }

    @Override
    public String getTagShortDescription() {
        return tagShortDescription;
    }

    @Override
    public String getTagDescription() {
        return tagDescription;
    }

    @Override
    public EMVTagType.DataSource getSource() {
        return source;
    }

    @Override
    public TLVDataFormat getFormat() {
        return format;
    }

    public boolean isProprietaryFormat() {
        return TLVDataFormat.PROPRIETARY.equals(format);
    }

    /**
     * @return The template or null if no template
     */
    public EMVTagType getTemplate() {
        return Bug349TagType.MapHolder.tagCodeMap.get(this.template);
    }

    @Override
    public EMVTagType.DataLength getDataLength() {
        return dataLength;
    }

    @Override
    public EMVTagType.ByteLength getByteLength() {
        return byteLength;
    }

    public Class<?> getDataType() throws ProprietaryFormatException {
        switch (format) {
            case PROPRIETARY:
                throw new ProprietaryFormatException(tagShortDescription);
            case BINARY:
                return byte[].class;
            case CONSTRUCTED:
                return EMVTag[].class;
            default:
                return String.class;
        }
    }

    private static class MapHolder {

        private static Map<Integer, Bug349TagType> tagCodeMap =
                new HashMap<Integer, Bug349TagType>();
    }

    public static class ProprietaryFixedDataLength extends EMVTagType.DataLength {

        public static final EMVTagType.DataLength INSTANCE = new Bug349TagType.ProprietaryFixedDataLength(-1);


        public ProprietaryFixedDataLength(int length) {
            super(length, length);
        }

        @Override
        public boolean isFixedLength() {
            return true;
        }
    }

    public static class ProprietaryVariableDataLength extends EMVTagType.DataLength {

        public static final EMVTagType.DataLength INSTANCE = new Bug349TagType.ProprietaryVariableDataLength(-1, -1);


        public ProprietaryVariableDataLength(int minLength, int maxLength) {
            super(minLength, maxLength);
        }

        @Override
        public boolean isFixedLength() {
            return false;
        }
    }

    public static class ProprietaryVariableDiscreteDataLength extends EMVTagType.DataLength {

        public static final EMVTagType.DataLength INSTANCE = new Bug349TagType.ProprietaryVariableDiscreteDataLength(-1, -1);


        public ProprietaryVariableDiscreteDataLength(int minLength, int maxLength) {
            super(minLength, maxLength);
        }

        @Override
        public boolean isFixedLength() {
            return true;
        }
    }

    public static class ProprietaryVariableDiscreteByteLength extends EMVTagType.ByteLength {

        public static final EMVTagType.ByteLength INSTANCE = new Bug349TagType.ProprietaryVariableDiscreteByteLength(-1, -1);


        public ProprietaryVariableDiscreteByteLength(int minLength, int maxLength) {
            super(minLength, maxLength);
        }

        @Override
        public boolean isFixedLength() {
            return true;
        }
    }

    public static class ProprietaryFixedByteLength extends EMVTagType.ByteLength {

        public static final EMVTagType.ByteLength INSTANCE = new Bug349TagType.ProprietaryFixedByteLength(-1);


        public ProprietaryFixedByteLength(int length) {
            super(length, length);
        }

        @Override
        public boolean isFixedLength() {
            return true;
        }
    }

    public static class ProprietaryVariableByteLength extends EMVTagType.ByteLength {

        public static final EMVTagType.ByteLength INSTANCE = new Bug349TagType.ProprietaryVariableByteLength(-1, -1);


        public ProprietaryVariableByteLength(int minLength, int maxLength) {
            super(minLength, maxLength);
        }

        @Override
        public boolean isFixedLength() {
            return false;
        }
    }
}

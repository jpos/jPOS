package org.jpos.emv;


import org.jpos.tlv.TLVDataFormat;
import org.jpos.tlv.TagValue;

import java.io.Serializable;


/**
 * @author Vishnu Pillai
 */
public abstract class EMVTag<T> implements TagValue<T>, Serializable {

    private static final long serialVersionUID = 4674858246785118615L;
    private final EMVTagType tagType;
    private final TLVDataFormat dataFormat;
    private final Integer tagNumber;
    private final T value;


    public EMVTag(final EMVStandardTagType tagType, final T value) throws IllegalArgumentException, NoTagNumberForProprietaryTagException {
        if (tagType == null) {
            throw new IllegalArgumentException("tagType cannot be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("value cannot be null");
        }
        if (tagType.isProprietaryTag()) {
            throw new IllegalArgumentException("Tag Number must be specified for proprietary tag");
        }
        if (tagType.isProprietaryFormat()) {
            throw new IllegalArgumentException(
                    "Tag TLVDataFormat must be specified for tag with proprietary format");
        } else {
            try {
                if (!tagType.getDataType().isAssignableFrom(value.getClass())) {
                    throw new IllegalArgumentException("Tag: " + tagType.getTagNumberHex() +
                            ". Value should be of type: " + tagType.getDataType() + ". Received " +
                            value.getClass());
                }
            } catch (ProprietaryFormatException e) {
                throw new IllegalStateException(e);
            } catch (NoTagNumberForProprietaryTagException e) {
                throw new IllegalStateException(e);
            }
        }
        this.tagType = tagType;
        this.value = value;
        try {
            this.tagNumber = tagType.getTagNumber();
        } catch (NoTagNumberForProprietaryTagException e) {
            throw new IllegalStateException(e);
        }
        this.dataFormat = tagType.getFormat();
    }


    public EMVTag(final EMVProprietaryTagType tagType, Integer tagNumber, final T value)
            throws IllegalArgumentException {
        if (tagType == null) {
            throw new IllegalArgumentException("tagType cannot be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("value cannot be null");
        }
        if (!EMVStandardTagType.isProprietaryTag(tagNumber)) {
            throw new IllegalArgumentException(
                    "Proprietary tag number conflicts with standard tag list");
        }
        if (tagType.isProprietaryFormat()) {
            throw new IllegalArgumentException(
                    "Tag TLVDataFormat must be specified for proprietary tag");
        } else {
            try {
                if (!getDataType(tagType.getFormat()).isAssignableFrom(value.getClass())) {
                    throw new IllegalArgumentException("Tag: " + tagType.getTagNumberHex() +
                            ". Value should be of type: " + tagType.getDataType() + ". Received " +
                            value.getClass());
                }
            } catch (ProprietaryFormatException e) {
                throw new IllegalStateException(e);
            } catch (NoTagNumberForProprietaryTagException e) {
                throw new IllegalStateException(e);
            }
        }
        this.tagType = tagType;
        this.value = value;
        this.tagNumber = tagNumber;
        this.dataFormat = tagType.getFormat();
    }


    public EMVTag(final EMVProprietaryTagType tagType, Integer tagNumber,
                  TLVDataFormat dataFormat, final T value) throws IllegalArgumentException {
        if (tagType == null) {
            throw new IllegalArgumentException("tagType cannot be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("value cannot be null");
        }
        if (!EMVStandardTagType.isProprietaryTag(tagNumber)) {
            throw new IllegalArgumentException(
                    "Proprietary tag number conflicts with standard tag list");
        }
        if (!getDataType(dataFormat).isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException("Tag: " + tagNumber + " : " +
                    tagType.getTagShortDescription() + ". Value should be of type: " +
                    getDataType(dataFormat) + ". Received " + value.getClass());
        }
        this.tagType = tagType;
        this.value = value;
        this.tagNumber = tagNumber;
        this.dataFormat = dataFormat;
    }


    private static Class<?> getDataType(TLVDataFormat dataFormat)
            throws IllegalArgumentException {
        switch (dataFormat) {
            case BINARY:
            case CONSTRUCTED:
            case PROPRIETARY:
                return byte[].class;
            default:
                return String.class;
        }
    }

    @Override
    public String getTag() {
        return getTagNumberHex();
    }

    @Override
    public boolean isComposite() {
        return false;
    }

    public TLVDataFormat getDataFormat() {
        return dataFormat;
    }

    public Integer getTagNumber() {
        return tagNumber;
    }

    public EMVTagType getTagType() {
        return tagType;
    }

    public T getValue() {
        return value;
    }

    public String getTagNumberHex() {
        return Integer.toHexString(tagNumber).toUpperCase();
    }
}

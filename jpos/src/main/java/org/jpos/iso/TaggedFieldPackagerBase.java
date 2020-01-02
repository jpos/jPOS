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

import org.jpos.iso.packager.TagMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutput;

/**
 * Base class and template for handling tagged fields.
 * <p/>
 * This should support both fixed length and variable length tags.
 */
public abstract class TaggedFieldPackagerBase extends ISOFieldPackager {

    private TagMapper tagMapper;
    private ISOFieldPackager delegate;
    private int parentFieldNumber;
    private boolean packingLenient = false;
    private boolean unpackingLenient = false;

    public TaggedFieldPackagerBase() {
        super();
    }

    /**
     * @param len         -
     *                    field len
     * @param description symbolic description
     */
    public TaggedFieldPackagerBase(int len, String description) {
        super(len, description);
        this.delegate = getDelegate(len, description);
    }

    /**
     * @param c -
     *          a component
     * @return packed component
     * @throws org.jpos.iso.ISOException
     */
    @Override
    public byte[] pack(ISOComponent c) throws ISOException {
        byte[] packed;
        if (c.getValue() == null) {
            packed = new byte[0];
        } else {
            String tag = getTagMapper().getTagForField(getParentFieldNumber(), (Integer) c.getKey());
            if (tag == null) {
                if (!isPackingLenient()) {
                    throw new ISOException("No tag mapping found for field: " + parentFieldNumber + "." + c.getKey());
                }
                packed = new byte[0];
            } else {
                byte[] tagBytes = tag.getBytes(ISOUtil.CHARSET);
                byte[] message = getDelegate().pack(c);
                packed = new byte[tagBytes.length + message.length];
                System.arraycopy(tagBytes, 0, packed, 0, tagBytes.length);
                System.arraycopy(message, 0, packed, tagBytes.length, message.length);
            }
        }
        return packed;
    }

    @Override
    public void pack(ISOComponent c, ObjectOutput out) throws IOException, ISOException {
        if (c.getValue() != null) {
            super.pack(c, out);
        }
    }

    /**
     * @param c      -
     *               the Component to unpack
     * @param b      -
     *               binary image
     * @param offset -
     *               starting offset within the binary image
     * @return consumed bytes
     * @throws ISOException
     */
    @Override
    public int unpack(ISOComponent c, byte[] b, int offset) throws ISOException {
        int consumed;
        byte[] tagBytes = new byte[getTagNameLength()];
        System.arraycopy(b, offset, tagBytes, 0, getTagNameLength());
        String tag = new String(tagBytes, ISOUtil.CHARSET);
        if (!(c instanceof ISOField) && !(c instanceof ISOBinaryField))
            throw new ISOException(c.getClass().getName()
                    + " is not an ISOField");
        Integer fieldNumber = getTagMapper().getFieldNumberForTag(getParentFieldNumber(), tag);
        if (fieldNumber == null || fieldNumber < 0) {
            if (!isUnpackingLenient()) {
                throw new ISOException("No field mapping found for tag: " + parentFieldNumber + "." + tag);
            }
            consumed = 0;
        } else {
            if (c.getKey().equals(fieldNumber)) {
                consumed = getTagNameLength() + getDelegate().unpack(c, b, offset + tagBytes.length);
            } else {
                consumed = 0;
            }
        }
        return consumed;
    }

    @Override
    public void unpack(ISOComponent c, InputStream in) throws IOException,
            ISOException {
        if (!in.markSupported()) {
            throw new ISOException("InputStream should support marking");
        }
        if (!(c instanceof ISOField))
            throw new ISOException(c.getClass().getName()
                    + " is not an ISOField");
        in.mark(getTagNameLength() + 1);
        Integer fieldNumber;
        String tag;
        tag = new String(readBytes(in, getTagNameLength()), ISOUtil.CHARSET);
        fieldNumber = getTagMapper().getFieldNumberForTag(getParentFieldNumber(), tag);
        if (fieldNumber == null || fieldNumber < 0) {
            if (!isUnpackingLenient()) {
                throw new ISOException("No field mapping found for tag: " + parentFieldNumber + "." + tag);
            }
            in.reset();
        } else {
            if (c.getKey().equals(fieldNumber)) {
                getDelegate().unpack(c, in);
            } else {
                in.reset();
            }
        }
    }

    private synchronized ISOFieldPackager getDelegate() {
        if (delegate == null) {
            delegate = getDelegate(getLength(), getDescription());
        }
        return delegate;
    }

    protected abstract ISOFieldPackager getDelegate(int len, String description);

    protected abstract int getTagNameLength();

    /**
     * @return A boolean value for or against lenient packing
     */
    protected boolean isPackingLenient() {
        return packingLenient;
    }

    /**
     * @return A boolean value for or against lenient unpacking
     */
    protected boolean isUnpackingLenient() {
        return unpackingLenient;
    }

    public void setPackingLenient(boolean packingLenient) {
        this.packingLenient = packingLenient;
    }

    public void setUnpackingLenient(boolean unpackingLenient) {
        this.unpackingLenient = unpackingLenient;
    }

    @Override
    public int getMaxPackedLength() {
        return getTagNameLength() + getDelegate().getMaxPackedLength();
    }

    public int getParentFieldNumber() {
        return parentFieldNumber;
    }

    public void setParentFieldNumber(int parentFieldNumber) {
        this.parentFieldNumber = parentFieldNumber;
    }

    public void setTagMapper(TagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }

    protected TagMapper getTagMapper() {
        return tagMapper;
    }

}

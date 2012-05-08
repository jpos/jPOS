package org.jpos.iso;

import org.jpos.iso.packager.TagMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutput;
import java.io.UnsupportedEncodingException;

/**
 * @author Vishnu Pillai
 */
public abstract class TaggedFieldPackagerBase extends ISOFieldPackager {

    private TagMapper tagMapper;
    private ISOFieldPackager delegate;
    private int parentFieldNumber;

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
    }

    /**
     * @param c -
     *          a component
     * @return packed component
     * @throws org.jpos.iso.ISOException
     */
    public byte[] pack(ISOComponent c) throws ISOException {
        if (c.getValue() == null) {
            return new byte[0];
        }
        byte[] tagBytes;
        try {
            tagBytes = getTagMapper().getTagForField(getParentFieldNumber(), (Integer) c.getKey()).getBytes(ISOUtil.ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new ISOException(e);
        }
        byte[] message = getDelegate().pack(c);
        byte[] packed = new byte[tagBytes.length + message.length];
        System.arraycopy(tagBytes, 0, packed, 0, tagBytes.length);
        System.arraycopy(message, 0, packed, tagBytes.length, message.length);
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
    public int unpack(ISOComponent c, byte[] b, int offset) throws ISOException {
        try {
            byte[] tagNameBytes = new byte[getTagNameLength()];
            System.arraycopy(b, offset, tagNameBytes, 0, getTagNameLength());
            String tagName = new String(tagNameBytes, ISOUtil.ENCODING);
            if (!(c instanceof ISOField))
                throw new ISOException(c.getClass().getName()
                        + " is not an ISOField");
            Integer fieldNumber = getTagMapper().getFieldNumberForTag(getParentFieldNumber(), tagName);
            if (fieldNumber != c.getKey()) {
                return 0;
            }
            return getTagNameLength() + getDelegate().unpack(c, b, offset + tagNameBytes.length);
        } catch (UnsupportedEncodingException e) {
            throw new ISOException(e);
        }
    }

    public void unpack(ISOComponent c, InputStream in) throws IOException,
            ISOException {
        if (!in.markSupported()) {
            throw new ISOException("InputStream should support marking");
        }
        if (!(c instanceof ISOField))
            throw new ISOException(c.getClass().getName()
                    + " is not an ISOField");
        in.mark(getTagNameLength() + 1);
        String tagName = new String(readBytes(in, getTagNameLength()), ISOUtil.ENCODING);
        Integer fieldNumber = getTagMapper().getFieldNumberForTag(getParentFieldNumber(), tagName);
        if (fieldNumber != c.getKey()) {
            in.reset();
            return;
        }
        getDelegate().unpack(c, in);
    }

    private ISOFieldPackager getDelegate() {
        if (delegate == null) {
            synchronized (this) {
                if (delegate == null) {
                    delegate = getDelegate(getLength(), getDescription());
                }
            }
        }
        return delegate;
    }

    protected abstract ISOFieldPackager getDelegate(int len, String description);

    protected abstract int getTagNameLength();

    public int getMaxPackedLength() {
        return getTagNameLength() + getDelegate().getMaxPackedLength();
    }

    @Override
    public ISOComponent createComponent(int fieldNumber) {
        return new ISOField(fieldNumber);
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

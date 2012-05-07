package org.jpos.iso;

import org.xml.sax.Attributes;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * @author Vishnu Pillai
 */
public class TLVFieldPackager extends ISOFieldPackager implements ConfigurablePackager {

    private String tagName = null;
    private int fieldId = -1;
    private int lengthFieldLength = 2;


    public TLVFieldPackager() {
        super();
    }

    /**
     * @param len         -
     *                    field len
     * @param description symbolic descrption
     */
    public TLVFieldPackager(int len, String description) {
        super(len, description);
    }

    /**
     * @param c -
     *          a component
     * @return packed component
     * @throws ISOException
     */
    public byte[] pack(ISOComponent c) throws ISOException {
        int len;

        if (!(c instanceof TLVField))
            throw new ISOException(c.getClass().getName()
                    + " is not a TLVField");
        String s = (String) c.getValue();

        if ((len = s.length()) > getLength()) // paranoia settings
            throw new ISOException("invalid len " + len
                    + " packing TVLSubTagSubField field " + (Integer) c.getKey());

        return (tagName + ISOUtil.zeropad(Integer.toString(len), lengthFieldLength) + s).getBytes();
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
            byte[] tagNameBytes = new byte[lengthFieldLength];
            System.arraycopy(b, offset, tagNameBytes, 0, lengthFieldLength);
            String tagName = new String(tagNameBytes, ISOUtil.ENCODING);
            if (!this.tagName.equals(tagName)) {
                return 0;
            }
            int len = Integer.parseInt(new String(b, offset + getTagNameLength(), lengthFieldLength));
            if (!(c instanceof TLVField))
                throw new ISOException(c.getClass().getName()
                        + " is not a TLVField");
            ((TLVField) c).setTagName(tagName);
            c.setFieldNumber(fieldId);
            c.setValue(new String(b, offset + getTagNameLength() + lengthFieldLength, len, ISOUtil.ENCODING));
            return getTagNameLength() + lengthFieldLength + len;
        } catch (UnsupportedEncodingException e) {
            throw new ISOException(e);
        }
    }

    /*
     * @return int length of tagName
     */
    private int getTagNameLength() {
        return tagName.length();
    }

    public void unpack(ISOComponent c, InputStream in) throws IOException,
            ISOException {

        if (!in.markSupported()) {
            throw new ISOException("InputStream does not support marking");
        }

        if (!(c instanceof TLVField))
            throw new ISOException(c.getClass().getName()
                    + " is not a TLVField");

        in.mark(getTagNameLength() + 1);
        String tagName = new String(readBytes(in, getTagNameLength()), ISOUtil.ENCODING);
        if (!this.tagName.equals(tagName)) {
            in.reset();
            return;
        }

        int len = Integer.parseInt(new String(readBytes(in, lengthFieldLength), ISOUtil.ENCODING));
        ((TLVField) c).setTagName(tagName);
        c.setFieldNumber(fieldId);
        c.setValue(new String(readBytes(in, len)));
    }

    public int getMaxPackedLength() {
        return getTagNameLength() + getLength() + lengthFieldLength;
    }

    @Override
    public void setGenericPackagerParams(Attributes atts) throws ISOException {
        this.tagName = atts.getValue("tagName");
        this.fieldId = Integer.parseInt(atts.getValue("id"));
        if (atts.getValue("lengthFieldLength") != null) {
            this.lengthFieldLength = Integer.parseInt(atts.getValue("lengthFieldLength"));
        }
    }

    @Override
    public ISOComponent createComponent(int fieldNumber) {
        return new TLVField(fieldNumber, tagName);
    }

}

package org.jpos.tlv.packager;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOFieldPackager;
import org.jpos.iso.ISOUtil;
import org.xml.sax.Attributes;

/**
 * @author Vishnu Pillai
 */
public class BinaryHexTaggedSequencePackager extends TaggedSequencePackager {

    public BinaryHexTaggedSequencePackager() throws ISOException {
        super();
    }

    @Override
    protected void setGenericPackagerParams(Attributes atts) {
        super.setGenericPackagerParams(atts);
    }

    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        super.setConfiguration(cfg);
    }

    @Override
    protected ISOFieldPackager getTagPackager() {
        TagPackager tagPackager = new TagPackager(this.tag.length(), "Tag");
        return tagPackager;
    }

    public static class TagPackager extends ISOFieldPackager {

        public TagPackager(int len, String description) {
            super(len, description);
        }


        @Override
        public int getMaxPackedLength() {
            return getLength() / 2;
        }

        @Override
        public byte[] pack(ISOComponent c) throws ISOException {
            byte[] tagBytes;
            String tag = c.getValue().toString();
            tagBytes = ISOUtil.hex2byte(tag);
            if (tagBytes.length != getMaxPackedLength()) {
                byte[] b = new byte[getMaxPackedLength()];
                System.arraycopy(tagBytes, 0, b, b.length - tagBytes.length, tagBytes.length);
                tagBytes = b;
            }
            return tagBytes;
        }

        @Override
        public int unpack(ISOComponent c, byte[] b, int offset) throws ISOException {
            byte[] tagBytes = new byte[getMaxPackedLength()];
            System.arraycopy(b, offset, tagBytes, 0, tagBytes.length);
            c.setValue(ISOUtil.byte2hex(tagBytes));
            return tagBytes.length;
        }
    }
}

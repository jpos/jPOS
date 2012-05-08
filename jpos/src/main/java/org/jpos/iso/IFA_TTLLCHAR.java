package org.jpos.iso;

/**
 * @author Vishnu Pillai
 */
public class IFA_TTLLCHAR extends TaggedFieldPackagerBase {

    @Override
    protected int getTagNameLength() {
        return 2;
    }

    protected ISOFieldPackager getDelegate(int length, String description) {
        return new IFA_LLCHAR(length, description);
    }
}

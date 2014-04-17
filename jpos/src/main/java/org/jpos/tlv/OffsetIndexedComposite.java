package org.jpos.tlv;

/**
 * @author Vishnu Pillai
 *         Date: 1/24/14
 */
public interface OffsetIndexedComposite {

    public void incOffset();

    public void setOffset(int offset);

    public void resetOffset();

    public int getOffset();
}

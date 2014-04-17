package org.jpos.tlv;

import org.jpos.iso.ISOException;

/**
 * @author Vishnu Pillai
 */
public interface TagValue<T> {

    String getTag();

    T getValue() throws ISOException;

    boolean isComposite();

}

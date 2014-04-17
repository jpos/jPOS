package org.jpos.tlv;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

import java.util.List;
import java.util.Map;

/**
 * @author Vishnu Pillai
 *         Date: 4/11/14
 */
public interface TagSequence<T> extends TagValue<T> {

    Map<String, List<TagValue<T>>> getChildren();

    void add(TagValue<T> tagValue);

    boolean hasTag(String tag);

    TagValue<T> getFirst(String tag);

    List<TagValue<T>> get(String tag);

    Map<String, List<TagValue<T>>> getAll();

    void writeTo(ISOMsg isoMsg) throws ISOException;

    void readFrom(ISOMsg isoMsg) throws ISOException;
}

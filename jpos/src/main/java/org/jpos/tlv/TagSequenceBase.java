package org.jpos.tlv;

import org.jpos.iso.ISOBinaryField;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOMsg;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Vishnu Pillai
 */
public abstract class TagSequenceBase implements TagSequence {

    private final String tag;
    private final TreeMap<String, List<TagValue>> tagMap = new TreeMap();

    public TagSequenceBase() {
        this.tag = "Root";
    }

    protected TagSequenceBase(String tag) {
        this.tag = tag;
    }

    @Override
    public boolean isComposite() {
        return true;
    }

    @Override
    public Map<String, List<TagValue>> getChildren() {
        return tagMap;
    }

    @Override
    public synchronized void add(TagValue tagValue) {
        String tag = tagValue.getTag();
        LinkedList<TagValue> values = (LinkedList<TagValue>) tagMap.get(tag);
        if (values == null) {
            values = new LinkedList();
            tagMap.put(tag, values);
        }
        values.add(tagValue);
    }

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public synchronized boolean hasTag(String tag) {
        return tagMap.containsKey(tag);
    }

    @Override
    public Object getValue() throws ISOException {
        return null;
    }

    @Override
    public synchronized TagValue getFirst(String tag) {
        TagValue tagValue = null;
        LinkedList<TagValue> values = (LinkedList<TagValue>) tagMap.get(tag);
        if (values != null) {
            tagValue = values.peekFirst();
        }
        return tagValue;
    }

    @Override
    public synchronized List<TagValue> get(String tag) {
        LinkedList<TagValue> values = (LinkedList<TagValue>) tagMap.get(tag);
        return values;
    }

    @Override
    public synchronized Map<String, List<TagValue>> getAll() {
        return tagMap;
    }

    @Override
    public synchronized void writeTo(ISOMsg isoMsg) throws ISOException {
        int maxField = isoMsg.getMaxField();
        Set<Map.Entry<String, List<TagValue>>> tagValues = getAll().entrySet();
        int fieldNumber = 0;
        for (Map.Entry<String, List<TagValue>> tagValueEntry : tagValues) {
            List<TagValue> tagValueList = tagValueEntry.getValue();
            for (TagValue tagValue : tagValueList) {
                Object value = tagValue.getValue();
                if (value != null) {
                    ISOComponent subField;
                    if (value instanceof byte[]) {
                        subField = new ISOBinaryField(fieldNumber + maxField + 1, (byte[]) value);
                    } else if (value instanceof String) {
                        subField = new ISOField(fieldNumber + maxField + 1, (String) value);
                    } else if (value instanceof TagSequence) {
                        TagSequence subSequence = (TagSequence) tagValue;
                        subField = new ISOMsg(fieldNumber + maxField + 1);
                        subSequence.writeTo((ISOMsg) subField);
                    } else if (value instanceof ISOMsg) {
                        ISOMsgTagValue subSequence = (ISOMsgTagValue) tagValue;
                        subField = subSequence.getValue();
                        subField.setFieldNumber(fieldNumber + maxField + 1);
                    } else {
                        throw new ISOException("Unknown TagValue subclass: " + tagValue.getClass());
                    }
                    isoMsg.set(new ISOTaggedField(tagValue.getTag(), subField));
                }
                fieldNumber++;
            }
        }
    }

    @Override
    public synchronized void readFrom(ISOMsg isoMsg) throws ISOException {
        int maxField = isoMsg.getMaxField();
        int minField = -1;
        for (int i = 0; i <= maxField; i++) {
            ISOComponent child = isoMsg.getComponent(i);
            if (child instanceof ISOTaggedField) {
                minField = i;
                break;
            }
        }
        if (minField == -1) {
            //No TaggedFields to read
            return;
        }
        for (int i = minField; i <= maxField; i++) {
            ISOComponent child = isoMsg.getComponent(i);
            if (child != null) {
                if (child instanceof ISOTaggedField) {
                    TagValue tagValue;
                    ISOTaggedField taggedSubField = (ISOTaggedField) child;
                    ISOComponent delegate = taggedSubField.getDelegate();
                    if (delegate instanceof ISOMsg) {
                        Map subChildren = delegate.getChildren();
                        boolean allTaggedValue = true;
                        for (Object subChild : subChildren.values()) {
                            if (!(subChild instanceof ISOTaggedField)) {
                                allTaggedValue = false;
                                break;
                            }
                        }
                        if (allTaggedValue) {
                            tagValue = createTagValueSequence(taggedSubField.getTag());
                            ((TagSequence) tagValue).readFrom((ISOMsg) delegate);
                        } else {
                            tagValue = new ISOMsgTagValue(getTag(), isoMsg);
                        }
                    } else if (delegate instanceof ISOBinaryField) {
                        tagValue = createBinaryTagValuePair(taggedSubField.getTag(), taggedSubField.getBytes());
                    } else if (delegate instanceof ISOField) {
                        tagValue = createLiteralTagValuePair(taggedSubField.getTag(), taggedSubField.getValue().toString());
                    } else {
                        throw new ISOException("Unknown ISOComponent subclass in ISOTaggedField: " + delegate.getClass());
                    }
                    this.add(tagValue);
                } else {
                    throw new ISOException("Children after first ISOTaggedField should be instance of ISOTaggedField." +
                            " Field " + i + " is not an ISOTaggedField");
                }
            }
        }

    }

    protected abstract TagSequence createTagValueSequence(String tag);

    protected abstract TagValue createLiteralTagValuePair(String tag, String value) throws ISOException;

    protected abstract TagValue createBinaryTagValuePair(String tag, byte[] value) throws ISOException;

}

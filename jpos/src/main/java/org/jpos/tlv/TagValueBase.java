package org.jpos.tlv;

/**
 * @author Vishnu Pillai
 */
public abstract class TagValueBase<T> implements TagValue<T> {

    private final String tag;
    private final T value;

    public TagValueBase(String tag, T value) {
        this.tag = tag;
        this.value = value;
    }

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public boolean isComposite() {
        return false;
    }

}

package org.jpos.iso.packager;

/**
 * @author Vishnu Pillai
 */
public interface TagMapper {

    public String getTagForField(int fieldNumber, int subFieldNumber);

    public int getFieldNumberForTag(int fieldNumber, String tag);

}

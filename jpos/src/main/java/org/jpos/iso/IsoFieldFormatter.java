package org.jpos.iso;

public class IsoFieldFormatter {

    private int tagIndex;
    private int lengthIndex;
    private int totalLength;

    public IsoFieldFormatter(int tagIndex, int lengthIndex, int totalLength) {
        this.lengthIndex = lengthIndex;
        this.tagIndex = tagIndex;
        this.totalLength = totalLength;
    }

    public int getTagIndex() {
        return this.tagIndex;
    }
    public int getLengthIndex() {
        return this.lengthIndex;
    }

    public int getLengthSize() {
        return getLength(lengthIndex, tagIndex);
    }

    public int getTagSize() {
        return getLength(tagIndex, lengthIndex);
    }

    public int getTotalLength() {
        return totalLength;
    }


    private int getLength(int mineIndex, int otherIndex) {
        if (mineIndex == 0 ) {
            return totalLength - 1 - otherIndex;
        } else {
            return totalLength - mineIndex;
        }
    }


    public void format(byte[] tagData, byte[] lengthData, byte[] data) {
        if (tagData!= null && lengthData!= null && data != null) {
            System.arraycopy(tagData, 0, data, getTagIndex(), tagData.length);
            System.arraycopy(lengthData, 0, data, getLengthIndex(), lengthData.length);
        }
    }

    public static IsoFieldFormatter LLTTvvvvv   = new IsoFieldFormatter(3, 0, 4);
    public static IsoFieldFormatter LLLTTvvvvv  = new IsoFieldFormatter(4, 0, 5);
    public static IsoFieldFormatter LLLLTTvvvvv = new IsoFieldFormatter(5, 0, 6);
    public static IsoFieldFormatter TTLLvvvvv   = new IsoFieldFormatter(0, 3, 4);
    public static IsoFieldFormatter TTLLLvvvvv  = new IsoFieldFormatter(0, 3, 5);
    public static IsoFieldFormatter TTLLLLvvvvv = new IsoFieldFormatter(0, 3, 6);

}

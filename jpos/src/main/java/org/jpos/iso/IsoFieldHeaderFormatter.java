package org.jpos.iso;

public class IsoFieldHeaderFormatter {

    private boolean tagFirst;

    public IsoFieldHeaderFormatter(boolean tagFirst) {
        this.tagFirst = tagFirst;
    }


    public boolean isTagFirst() {
        return tagFirst;
    }

    /**
     *
     * @param tagPrefixer the tag part prefixer
     * @param lengthPrefixer the length part prefixer
     * @param tagData byte array containing the tag value bytes
     * @param lengthData byte array containing the length value bytes
     * @return byte array containing the header (tag and length), size of returned bytes is just the header length
     */
    public byte[] format(final Prefixer tagPrefixer, final Prefixer lengthPrefixer, final byte[] tagData, final byte[] lengthData) {
        if (tagPrefixer != null && lengthPrefixer != null && tagData != null && lengthData != null) {
            final byte[] destinationData = new byte[tagPrefixer.getPackedLength() + lengthPrefixer.getPackedLength()];
            format(tagPrefixer, lengthPrefixer, tagData, lengthData, destinationData);
            return destinationData;
        }
        return null;
    }

    public void format(final Prefixer tagPrefixer, final Prefixer lengthPrefixer, final byte[] tagData, final byte[] lengthData, final byte[] destinationData) {
        if (tagPrefixer != null && lengthPrefixer != null && tagData != null && lengthData != null && destinationData != null) {
            System.arraycopy(tagData, 0, destinationData, tagFirst ? 0 : lengthPrefixer.getPackedLength(), tagPrefixer.getPackedLength());
            System.arraycopy(lengthData, 0, destinationData, tagFirst ? tagPrefixer.getPackedLength() : 0, lengthPrefixer.getPackedLength());
        }
    }

    public int getTagIndex(Prefixer lengthPrefixer) {
        return tagFirst ? 0 : lengthPrefixer.getPackedLength();
    }

    public int getLengthIndex(Prefixer tagPrefixer) {
        return tagFirst ? tagPrefixer.getPackedLength() : 0;
    }

    public static IsoFieldHeaderFormatter TAG_FIRST = new IsoFieldHeaderFormatter(true);
    public static IsoFieldHeaderFormatter LENGTH_FIRST = new IsoFieldHeaderFormatter(false);

    public int getTotalLength(final Prefixer tagPrefixer, final Prefixer prefixer) {
        if (tagPrefixer == null || prefixer == null) {
            throw new IllegalArgumentException("Neither tag or length prefixer was provided.");
        }
        return tagPrefixer.getPackedLength() + prefixer.getPackedLength();
    }
}

/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.iso;

/** IsoFieldHeaderFormatter implementation. */

/** IsoFieldHeaderFormatter implementation. */

/** IsoFieldHeaderFormatter implementation. */

/** IsoFieldHeaderFormatter implementation. */

/** IsoFieldHeaderFormatter implementation. */

/** IsoFieldHeaderFormatter implementation. */

/** IsoFieldHeaderFormatter implementation. */

/** IsoFieldHeaderFormatter implementation. */

/** IsoFieldHeaderFormatter implementation. */

/** IsoFieldHeaderFormatter implementation. */

/** IsoFieldHeaderFormatter implementation. */

/** IsoFieldHeaderFormatter implementation. */

/** IsoFieldHeaderFormatter implementation. */

/** IsoFieldHeaderFormatter implementation. */

/** IsoFieldHeaderFormatter implementation. */

public class IsoFieldHeaderFormatter {

    private boolean tagFirst;

    /** Constructs a formatter.
     * @param tagFirst if true, the tag field comes before the length
     */
    public IsoFieldHeaderFormatter(boolean tagFirst) {
        this.tagFirst = tagFirst;
    }


    /** Returns whether tag comes before length.
     * @return true if tag-first ordering
     */
    public boolean isTagFirst() {
        return tagFirst;
    }

    /**
     *
     * Formats tag/length fields according to the configured ordering.
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

    /** Formats the tag and length prefixers into the output.
     */
    public void format(final Prefixer tagPrefixer, final Prefixer lengthPrefixer, final byte[] tagData, final byte[] lengthData, final byte[] destinationData) {
        if (tagPrefixer != null && lengthPrefixer != null && tagData != null && lengthData != null && destinationData != null) {
            System.arraycopy(tagData, 0, destinationData, tagFirst ? 0 : lengthPrefixer.getPackedLength(), tagPrefixer.getPackedLength());
            System.arraycopy(lengthData, 0, destinationData, tagFirst ? tagPrefixer.getPackedLength() : 0, lengthPrefixer.getPackedLength());
        }
    }

    /** Returns the index of the tag prefixer relative to length.
     * @param lengthPrefixer the length prefixer
     * @return the tag index
     */
    public int getTagIndex(Prefixer lengthPrefixer) {
        return tagFirst ? 0 : lengthPrefixer.getPackedLength();
    }

    /** Returns the index of the length prefixer relative to tag.
     * @param tagPrefixer the tag prefixer
     * @return the length index
     */
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

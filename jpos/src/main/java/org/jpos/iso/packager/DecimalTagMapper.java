/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
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

package org.jpos.iso.packager;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;

/**
 * {@code TagMapper} provides mappings between decimal tags and subfields.
 *
 * @author Michał Wiercioch
 */
public class DecimalTagMapper implements TagMapper {

    static final int RADIX_DECIMAL                      = 10;

    /**
     * The number of characters used to represent numbers in a positional
     * numeral system.
     * <p>
     * The following ASCII characters are used as digits:
     * <pre>{@code
     *   0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ
     * }</pre>
     * What are the decimal digits and 26 Latin letters.
     *
     * @see Integer.toString(n, 36).toUpperCase()
     * @see Integer.parseint(n, 36)
     */
    static final int RADIX_ALFANUM                      = 36;

    protected final int tagSize;

    /**
     * Maximum value of decimal tag id.
     */
    private   final int tagMaxValue;

    /**
     * Maximum value of upper leter tag id.
     */
    private   final int tagMaxValueUpperLetter;

    protected DecimalTagMapper(int tagSize) {
        this.tagSize = tagSize;
        int tmv = 1;
        for (int i = 0; i < tagSize; i++)
            tmv *= RADIX_DECIMAL;

        tagMaxValue = tmv - 1;

        tmv = 1;
        for (int i = 0; i < tagSize; i++)
            tmv *= RADIX_ALFANUM;

        tagMaxValueUpperLetter = tmv + tagMaxValue;
    }

    /**
     * Convert {@code subFieldNumber} to tag.
     *
     * @param fieldNumber field number (used only in exception message)
     * @param subFieldNumber sufield number to convert
     * @return tag name for passed {@code subFieldNumber}
     */
    @Override
    public String getTagForField(int fieldNumber, int subFieldNumber) {
        if (subFieldNumber < 0 || subFieldNumber > tagMaxValueUpperLetter)
            throw new IllegalArgumentException(
                String.format(
                      "The subtag id: %d of DE%d out of range [0, %d]"
                    , subFieldNumber, fieldNumber, tagMaxValueUpperLetter
                )
            );

        int radix = 10;
        int fn = subFieldNumber;
        if (fn > tagMaxValue) {
            radix = RADIX_ALFANUM;
            fn -= tagMaxValue + 1;
        }

        String sfn = Integer.toString(fn, radix).toUpperCase();
        String ret = leftZeroPad(sfn, tagSize);
        if (radix == RADIX_ALFANUM && ISOUtil.isNumeric(sfn, 10))
            throw new IllegalArgumentException(
                String.format(
                      "The subtag id: %d of DE%d is illegal"
                    , subFieldNumber, fieldNumber
                )
            );

        return ret;
    }

    /**
     * Convert passed {@code tag} to subfield number.
     *
     * @param fieldNumber field number (used only in exception message)
     * @param tag tag to convert
     * @return subfield number
     */
    @Override
    public Integer getFieldNumberForTag(int fieldNumber, String tag) {
        if (tag == null)
            throw new IllegalArgumentException(
                String.format("The subtag id of DE%d cannot be null", fieldNumber)
            );

        if (tag.length() != tagSize)
            throw new IllegalArgumentException(
                String.format(
                      "The subtag id: '%s' length of DE%d must be %d"
                    , tag, fieldNumber, tagSize
                )
            );

        int radix = RADIX_DECIMAL;
        if (containsUpperLatinAlpha(tag) && !containsLowerLatinAlpha(tag))
            // change radix only for upper latin letters and numbers
            radix = RADIX_ALFANUM;

        int res;
        try {
            res = Integer.valueOf(tag, radix);
        } catch (NumberFormatException ex) {
            throw new NumberFormatException(
                String.format(
                      "The subtag '%s' of DE%d cannot be converted to integer value"
                    , tag, fieldNumber
                )
            );
        }
        if (res < 0)
            throw new IllegalArgumentException(
                String.format(
                      "The subtag id: %s of DE%d cannot be negative"
                    , tag, fieldNumber
                )
            );

        if (radix == RADIX_ALFANUM)
            // shift result of upper letters encoding
            res += tagMaxValue + 1;

        return res;
    }

    private static String leftZeroPad(String s, int len) {
        try {
            return ISOUtil.zeropad(s, len);
        } catch (ISOException ex) {
            // it never happens
            return s;
        }
    }

    private static boolean containsLowerLatinAlpha(String s) {
        int i = 0;
        for (char c : s.toCharArray())
            if (c >= 'a' && c <= 'z')
                i++;

        return i > 0;
    }

    private static boolean containsUpperLatinAlpha(String s) {
        int i = 0;
        for (char c : s.toCharArray())
            if (c >= 'A' && c <= 'Z')
                i++;

        return i > 0;
    }

}

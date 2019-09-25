/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2019 jPOS Software SRL
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

    protected final int tagSize;

    /**
     * Maximum value of decimal tag id.
     */
    private   final int tagMaxValue;

    protected DecimalTagMapper(int tagSize) {
        this.tagSize = tagSize;
        int tmv = 1;
        for (int i = 0; i < tagSize; i++)
            tmv *= RADIX_DECIMAL;

        tagMaxValue = tmv - 1;
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
        if (subFieldNumber < 0 || subFieldNumber > tagMaxValue)
            throw new IllegalArgumentException(
                String.format(
                      "The subtag id: %d of DE%d out of range [0, %d]"
                    , subFieldNumber, fieldNumber, tagMaxValue
                )
            );

        String sfn = Integer.toString(subFieldNumber);
        String ret = leftZeroPad(sfn, tagSize);
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

        int res;
        try {
            res = Integer.valueOf(tag);
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

}

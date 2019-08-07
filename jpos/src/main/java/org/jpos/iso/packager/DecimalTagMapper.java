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
/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2014 Alejandro P. Revilla
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

import org.jpos.iso.ISOUtil;

/**
 * TagMapper provides mappings between decimal tags and subfields.
 *
 * @author Michał Wiercioch
 */
public class DecimalTagMapper implements TagMapper {

  protected int tagLen;
  private   int tagMaxValue;

  protected DecimalTagMapper(int tagLen) {
    tagMaxValue = 1;
    this.tagLen = tagLen;
    for(int i = 0; i < tagLen; i++)
      tagMaxValue *= 10;
  }

  /**
   * Convert {@code subFieldNumber} to tag
   *
   * @param fieldNumber field number (used only in exception message)
   * @param subFieldNumber sufield number to convert
   * @return tag name for passed {@code subFieldNumber}
   */
  @Override
  public String getTagForField(int fieldNumber, int subFieldNumber) {
    if(subFieldNumber < 0 || subFieldNumber >= tagMaxValue)
      throw new IllegalArgumentException(String.format(
        "Subfield number %d out of range [0, %d] for field %d"
              , subFieldNumber, tagMaxValue - 1, fieldNumber));

    try {
      return ISOUtil.zeropad(String.valueOf(subFieldNumber), tagLen);
    } catch (ISOException ex) {
      return null; // should not happen
    }
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
    try {
      Integer res = Integer.valueOf(tag);
      if (res < 0 || res >= tagMaxValue)
        throw new IllegalArgumentException(String.format(
          "Tag '%s' out of range [0, %d] for field %d"
                , tag, tagMaxValue - 1, fieldNumber));
      return res;
    } catch (NumberFormatException ex) {
      throw new NumberFormatException(String.format(
              "Tag '%s' cannot be converted to int for field %d", tag, fieldNumber));
    }
  }
}
